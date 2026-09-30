package net.toxicteddie.witchcraft.client.power;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.client.ClientElementPowerData;
import net.toxicteddie.witchcraft.client.ClientMagicData;
import net.toxicteddie.witchcraft.client.PowerHotbarManager;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.AirPowerActionPacket;
import net.toxicteddie.witchcraft.network.packet.TelekinesisReleasePacket;
import net.toxicteddie.witchcraft.network.packet.TelekinesisStartPacket;
import net.toxicteddie.witchcraft.power.ElementPower;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TelekinesisInputHandler {
    private static final int MAX_BURST_CHARGE_TICKS = 30;
    private static boolean requestedHold;
    private static int heldTargetId = -1;
    private static boolean chargingSecondary;
    private static int secondaryChargeTicks;

    private TelekinesisInputHandler() {}

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.screen instanceof AbstractContainerScreen<?>) return;
        if (!airSelected()) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == GLFW.GLFW_PRESS) {
            ModNetworking.CHANNEL.sendToServer(new AirPowerActionPacket(AirPowerActionPacket.Action.PULL));
            event.setCanceled(true);
            return;
        }

        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        boolean shift = minecraft.player.isShiftKeyDown();

        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (shift) {
                chargingSecondary = true;
                secondaryChargeTicks = 0;
                ModNetworking.CHANNEL.sendToServer(new AirPowerActionPacket(AirPowerActionPacket.Action.SECONDARY_PRESS));
                event.setCanceled(true);
            } else if (ClientMagicData.getMagic() >= 5
                    && ClientElementPowerData.get(ElementPower.AIR) >= 2) {
                Entity target = findClientTelekinesisTarget(minecraft, 8.0D);
                if (target != null) {
                    requestedHold = true;
                    heldTargetId = target.getId();
                    ModNetworking.CHANNEL.sendToServer(new TelekinesisStartPacket());
                    event.setCanceled(true);
                }
            }
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (chargingSecondary) {
                chargingSecondary = false;
                ModNetworking.CHANNEL.sendToServer(new AirPowerActionPacket(AirPowerActionPacket.Action.SECONDARY_RELEASE));
                event.setCanceled(true);
            } else if (requestedHold) {
                requestedHold = false;
                heldTargetId = -1;
                ModNetworking.CHANNEL.sendToServer(new TelekinesisReleasePacket(true));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (requestedHold) {
            Entity held = getHeldTarget();
            boolean rightMouseDown = minecraft.getWindow().getWindow() != 0L
                    && GLFW.glfwGetMouseButton(
                    minecraft.getWindow().getWindow(),
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT
            ) == GLFW.GLFW_PRESS;

            if (minecraft.player == null
                    || !airSelected()
                    || !rightMouseDown
                    || held == null
                    || !held.isAlive()) {
                requestedHold = false;
                heldTargetId = -1;
                ModNetworking.CHANNEL.sendToServer(new TelekinesisReleasePacket(false));
            }
        }

        if (!chargingSecondary) return;

        if (minecraft.player == null || !airSelected() || !minecraft.player.isShiftKeyDown()) {
            chargingSecondary = false;
            ModNetworking.CHANNEL.sendToServer(
                    new AirPowerActionPacket(AirPowerActionPacket.Action.SECONDARY_RELEASE)
            );
            return;
        }

        secondaryChargeTicks = Math.min(
                MAX_BURST_CHARGE_TICKS,
                secondaryChargeTicks + 1
        );
    }

    @SubscribeEvent
    public static void renderChargeRing(RenderGuiOverlayEvent.Post event) {
        if (!chargingSecondary) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int cx = minecraft.getWindow().getGuiScaledWidth() / 2;
        int cy = minecraft.getWindow().getGuiScaledHeight() / 2;
        float progress = Math.min(1.0F, secondaryChargeTicks / (float) MAX_BURST_CHARGE_TICKS);
        drawRing(graphics, cx, cy, 11, progress);
    }

    private static void drawRing(GuiGraphics graphics, int cx, int cy, int radius, float progress) {
        int segments = 48;
        int filled = Math.round(segments * progress);
        for (int i = 0; i < segments; i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / segments;
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int y = cy + (int) Math.round(Math.sin(angle) * radius);
            int color = i < filled ? 0xE8B7D9FF : 0x553B5268;
            graphics.fill(x, y, x + 1, y + 1, color);
            if (progress >= 1.0F && (i & 1) == 0) {
                int ox = cx + (int) Math.round(Math.cos(angle) * (radius + 1));
                int oy = cy + (int) Math.round(Math.sin(angle) * (radius + 1));
                graphics.fill(ox, oy, ox + 1, oy + 1, 0xBEE8F6FF);
            }
        }
    }

    public static boolean isMainTelekinesisActive() {
        return requestedHold && getHeldTarget() != null && airSelected();
    }

    public static Entity getHeldTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || heldTargetId < 0) return null;
        return minecraft.level.getEntity(heldTargetId);
    }

    public static Vec3 getHeldTargetCenter(float partialTick) {
        Entity entity = getHeldTarget();
        if (entity == null) return null;

        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;

        return new Vec3(
                x,
                y + entity.getBbHeight() * 0.5D,
                z
        );
    }


    private static Entity findClientTelekinesisTarget(
            Minecraft minecraft,
            double range
    ) {
        if (minecraft.player == null || minecraft.level == null) return null;

        Vec3 start = minecraft.player.getEyePosition();
        Vec3 look = minecraft.player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));

        AABB search = minecraft.player.getBoundingBox()
                .expandTowards(look.scale(range))
                .inflate(1.0D);

        Entity best = null;
        double bestDistance = range * range;

        for (Entity entity : minecraft.level.getEntities(
                minecraft.player,
                search,
                TelekinesisInputHandler::isValidClientTarget
        )) {
            var hit = entity.getBoundingBox().inflate(0.3D).clip(start, end);
            if (hit.isEmpty()) continue;

            double distance = start.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }

        return best;
    }

    private static boolean isValidClientTarget(Entity entity) {
        if (!entity.isAlive()
                || entity.isPassenger()
                || !entity.getPassengers().isEmpty()
                || entity instanceof Player) {
            return false;
        }

        return entity instanceof LivingEntity || entity instanceof ItemEntity;
    }

    private static boolean airSelected() {
        return ClientMagicData.isWitch()
                && PowerHotbarManager.isPowerMode()
                && PowerHotbarManager.getSelectedElement() == ElementPower.AIR;
    }
}
