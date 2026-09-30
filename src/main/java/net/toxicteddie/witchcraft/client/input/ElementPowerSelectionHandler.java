package net.toxicteddie.witchcraft.client.input;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.client.ClientMagicData;
import net.toxicteddie.witchcraft.client.PowerHotbarManager;


@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElementPowerSelectionHandler {
    private ElementPowerSelectionHandler() {}

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen instanceof AbstractContainerScreen<?>) return;
        if (!ClientMagicData.isWitch()) return;

        double delta = event.getScrollDelta();
        if (delta == 0.0D) return;


        int direction = delta < 0.0D ? 1 : -1;
        if (PowerHotbarManager.handleScroll(minecraft, direction)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        int key = event.getKey();
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            PowerHotbarManager.handleVanillaHotbarKey();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !minecraft.player.isAlive()
                || !ClientMagicData.isWitch()) {
            PowerHotbarManager.reset();
        }
    }
}
