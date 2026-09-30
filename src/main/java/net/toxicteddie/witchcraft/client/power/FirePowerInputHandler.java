package net.toxicteddie.witchcraft.client.power;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.client.ClientMagicData;
import net.toxicteddie.witchcraft.client.PowerHotbarManager;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.FirePowerActionPacket;
import net.toxicteddie.witchcraft.power.ElementPower;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FirePowerInputHandler {
    private static final int MAX_EXTINGUISH_CHARGE_TICKS = 25;
    private static final int MAX_PRIMARY_CHARGE_TICKS = 25;

    private static boolean primaryHeld;
    private static int primaryChargeTicks;
    private static boolean secondaryHeld;
    private static boolean radialIgniteHeld;
    private static int secondaryChargeTicks;
    private static int radialIgniteChargeTicks;

    private FirePowerInputHandler() {}

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.screen instanceof AbstractContainerScreen<?>) return;
        if (!fireSelected()) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == GLFW.GLFW_PRESS) {
            ModNetworking.CHANNEL.sendToServer(
                    new FirePowerActionPacket(FirePowerActionPacket.Action.STORM_LIGHTNING));
            event.setCanceled(true);
            return;
        }

        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        boolean shift = minecraft.player.isShiftKeyDown();

        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (fireRadialModifierDown(minecraft)) {
                radialIgniteHeld = true;
                radialIgniteChargeTicks = 0;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.RADIAL_IGNITE_PRESS));
            } else if (shift) {
                secondaryHeld = true;
                secondaryChargeTicks = 0;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.SECONDARY_PRESS));
            } else {
                primaryHeld = true;
                primaryChargeTicks = 0;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.PRIMARY_PRESS));
            }
            event.setCanceled(true);
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (radialIgniteHeld) {
                radialIgniteHeld = false;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.RADIAL_IGNITE_RELEASE));
                event.setCanceled(true);
            } else if (secondaryHeld) {
                secondaryHeld = false;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.SECONDARY_RELEASE));
                event.setCanceled(true);
            } else if (primaryHeld) {
                primaryHeld = false;
                primaryChargeTicks = 0;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.PRIMARY_RELEASE));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();

        boolean rightMouseDown = minecraft.getWindow().getWindow() != 0L
                && GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (primaryHeld && (minecraft.player == null || !fireSelected()
                || !rightMouseDown || minecraft.player.isShiftKeyDown()
                || fireRadialModifierDown(minecraft))) {
            primaryHeld = false;
            primaryChargeTicks = 0;
            ModNetworking.CHANNEL.sendToServer(
                    new FirePowerActionPacket(FirePowerActionPacket.Action.PRIMARY_RELEASE));
        } else if (primaryHeld) {
            primaryChargeTicks = Math.min(
                    MAX_PRIMARY_CHARGE_TICKS,
                    primaryChargeTicks + 1
            );
        }

        if (secondaryHeld) {
            if (minecraft.player == null || !fireSelected()
                    || !rightMouseDown || !minecraft.player.isShiftKeyDown()) {
                secondaryHeld = false;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.SECONDARY_RELEASE));
            } else {
                secondaryChargeTicks = Math.min(MAX_EXTINGUISH_CHARGE_TICKS, secondaryChargeTicks + 1);
            }
        }

        if (radialIgniteHeld) {
            if (minecraft.player == null || !fireSelected()
                    || !rightMouseDown || !fireRadialModifierDown(minecraft)) {
                radialIgniteHeld = false;
                ModNetworking.CHANNEL.sendToServer(
                        new FirePowerActionPacket(FirePowerActionPacket.Action.RADIAL_IGNITE_RELEASE));
            } else {
                radialIgniteChargeTicks = Math.min(
                        MAX_EXTINGUISH_CHARGE_TICKS,
                        radialIgniteChargeTicks + 1
                );
            }
        }
    }

    @SubscribeEvent
    public static void renderChargeRing(RenderGuiOverlayEvent.Post event) {
        if (!primaryHeld && !secondaryHeld && !radialIgniteHeld) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int cx = minecraft.getWindow().getGuiScaledWidth() / 2;
        int cy = minecraft.getWindow().getGuiScaledHeight() / 2;

        float progress;
        boolean ignitionStyle;

        if (radialIgniteHeld) {
            progress = Math.min(
                    1.0F,
                    radialIgniteChargeTicks / (float) MAX_EXTINGUISH_CHARGE_TICKS
            );
            ignitionStyle = true;
        } else if (secondaryHeld) {
            progress = Math.min(
                    1.0F,
                    secondaryChargeTicks / (float) MAX_EXTINGUISH_CHARGE_TICKS
            );
            ignitionStyle = false;
        } else {
            progress = Math.min(
                    1.0F,
                    primaryChargeTicks / (float) MAX_PRIMARY_CHARGE_TICKS
            );
            ignitionStyle = true;
        }

        drawRing(graphics, cx, cy, 11, progress, ignitionStyle);
    }

    private static void drawRing(
            GuiGraphics graphics,
            int cx,
            int cy,
            int radius,
            float progress,
            boolean ignition
    ) {
        int segments = 48;
        int filled = Math.round(segments * progress);
        for (int i = 0; i < segments; i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / segments;
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int y = cy + (int) Math.round(Math.sin(angle) * radius);
            int color = i < filled
                    ? (ignition ? 0xEEFF6A24 : 0xEEFF9A55)
                    : 0x553F302A;
            graphics.fill(x, y, x + 1, y + 1, color);
            if (progress >= 1.0F && (i & 1) == 0) {
                int ox = cx + (int) Math.round(Math.cos(angle) * (radius + 1));
                int oy = cy + (int) Math.round(Math.sin(angle) * (radius + 1));
                graphics.fill(ox, oy, ox + 1, oy + 1, 0xCCFFD4A3);
            }
        }
    }

    private static boolean fireRadialModifierDown(Minecraft minecraft) {
        long window = minecraft.getWindow().getWindow();
        if (window == 0L) return false;

        boolean mac = Util.getPlatform() == Util.OS.OSX;
        if (mac) {
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SUPER) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SUPER) == GLFW.GLFW_PRESS;
        }

        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public static boolean isPrimaryFlameBlastActive() {
        return primaryHeld
                && primaryChargeTicks >= MAX_PRIMARY_CHARGE_TICKS
                && fireSelected();
    }

    private static boolean fireSelected() {
        return ClientMagicData.isWitch()
                && PowerHotbarManager.isPowerMode()
                && PowerHotbarManager.getSelectedElement() == ElementPower.FIRE;
    }
}
