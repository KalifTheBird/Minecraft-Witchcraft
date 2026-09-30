package net.toxicteddie.witchcraft.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.client.PowerHotbarManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(Gui.class)
public abstract class GuiHotbarMixin {

    @Redirect(
            method = "renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/GuiGraphics;blit(" +
                            "Lnet/minecraft/resources/ResourceLocation;" +
                            "IIIIII" +
                            ")V"
            )
    )
    private void witchcraft$conditionallyDrawHotbarSprite(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        boolean vanillaSelector =
                u == 0
                && v == 22
                && width == 24
                && height == 22;

        if (PowerHotbarManager.isPowerMode() && vanillaSelector) {
            return;
        }

        graphics.blit(texture, x, y, u, v, width, height);
    }
}
