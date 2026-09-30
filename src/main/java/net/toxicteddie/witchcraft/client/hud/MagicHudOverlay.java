package net.toxicteddie.witchcraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.toxicteddie.witchcraft.client.ClientMagicData;
import net.toxicteddie.witchcraft.client.ClientElementPowerData;
import net.toxicteddie.witchcraft.client.PowerHotbarManager;
import net.toxicteddie.witchcraft.client.ClientConsecratedAreaData;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.MagicState;
import net.toxicteddie.witchcraft.power.ElementPower;

public final class MagicHudOverlay {
    private static final ResourceLocation CRYSTAL_FRAME =
            new ResourceLocation("witchcraft", "textures/gui/magic_crystal_frame.png");
    private static final ResourceLocation POWER_HOTBAR_BACK =
            new ResourceLocation("witchcraft", "textures/gui/power_hotbar_back.png");
    private static final ResourceLocation POWER_HOTBAR_FRAME =
            new ResourceLocation("witchcraft", "textures/gui/power_hotbar_frame.png");

    private static final ResourceLocation VANILLA_WIDGETS =
            new ResourceLocation("minecraft", "textures/gui/widgets.png");


    private static final int CRYSTAL_W = 51;
    private static final int CRYSTAL_H = 51;
    private static final int CRYSTAL_X = 8;
    private static final int CRYSTAL_Y = 8;


    private static final float CRYSTAL_SCALE = 1.25F;


    private static final int CRYSTAL_CENTER_X = 26;
    private static final int CRYSTAL_CENTER_Y = 26;
    private static final int CRYSTAL_RADIUS_X = 19;
    private static final int CRYSTAL_RADIUS_Y = 19;

    private static final int POWER_BAR_W = 82;
    private static final int POWER_BAR_H = 22;
    private static final int SLOT_COUNT = 4;
    private static final int SLOT_STEP = 20;


    private static final int LIQUID_X_IN_SLOT = 2;
    private static final int LIQUID_Y = 3;
    private static final int LIQUID_W = 16;
    private static final int LIQUID_H = 16;

    private static final int LIQUID_MAX_FILL_H = 14;
    private static final int HOTBAR_GAP = 6;
    private static final ElementPower[] ELEMENT_ORDER = {
            ElementPower.AIR, ElementPower.FIRE, ElementPower.WATER, ElementPower.EARTH
    };


    private static final int[] ELEMENT_TOP_COLORS = {
            0xE09A7628,
            0xE0480908,
            0xE0092445,
            0xE00B3215
    };
    private static final int[] ELEMENT_BOTTOM_COLORS = {
            0xF02A210A,
            0xF00E0101,
            0xF0010715,
            0xF0011004
    };

    private MagicHudOverlay() {
    }

    public static final IGuiOverlay HUD_MAGIC =
            (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player == null
                        || minecraft.options.hideGui
                        || !ClientMagicData.isWitch()) {
                    return;
                }

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                float time = minecraft.player.tickCount + partialTick;
                renderCrystal(
                        graphics,
                        CRYSTAL_X,
                        CRYSTAL_Y,
                        ClientMagicData.getMagic(),
                        ClientMagicData.getState(),
                        time
                );

                int vanillaHotbarRight = screenWidth / 2 + 91;
                int x = vanillaHotbarRight + HOTBAR_GAP;
                int y = screenHeight - 22;
                int totalWidth = POWER_BAR_W;

                x = Math.min(x, screenWidth - totalWidth - 2);
                renderElementSlots(graphics, x, y, time);

                RenderSystem.disableBlend();
            };


    private static void renderCrystal(
            GuiGraphics graphics,
            int x,
            int y,
            int magic,
            MagicState state,
            float time
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(CRYSTAL_SCALE, CRYSTAL_SCALE, 1.0F);

        float pct = Mth.clamp(
                magic / (float) MagicConstants.ABSOLUTE_LIMIT,
                0.0F,
                1.0F
        );


        int cx = CRYSTAL_CENTER_X;
        int cy = CRYSTAL_CENTER_Y;
        int radiusX = CRYSTAL_RADIUS_X;
        int radiusY = CRYSTAL_RADIUS_Y;
        int bottom = cy + radiusY;
        int filled = Math.round(radiusY * 2.0F * pct);
        int baseTop = bottom - filled;

        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.12F);

        int top = state == MagicState.FESTERING
                ? lerpArgb(0xF0120A12, 0xF0260509, pulse)
                : state == MagicState.SUPERCRITICAL
                  ? lerpArgb(0xF31A0204, 0xF43B0306, pulse)
                  : lerpArgb(0xF00D0E12, 0xF0181A20, pulse);

        int mid = state == MagicState.FESTERING
                ? lerpArgb(0xF4080509, 0xF4120204, pulse)
                : state == MagicState.SUPERCRITICAL
                  ? lerpArgb(0xF5090102, 0xF51A0103, pulse)
                  : lerpArgb(0xF5050609, 0xF50A0B0F, pulse);

        int bottomColor = 0xFA010102;

        int surface = state == MagicState.FESTERING
                ? lerpArgb(0xD7331A35, 0xE0501018, pulse)
                : state == MagicState.SUPERCRITICAL
                  ? lerpArgb(0xE05C1017, 0xF0801820, pulse)
                  : lerpArgb(0xC52A2D35, 0xDA4A4F5A, pulse);


        for (int py = cy - radiusY; py <= bottom; py++) {
            float normalizedY = (py - cy) / (float) radiusY;
            float widthFactor = (float) Math.sqrt(
                    Math.max(0.0F, 1.0F - normalizedY * normalizedY)
            );
            int half = Math.max(0, Math.round(radiusX * widthFactor));

            if (half <= 0) {
                continue;
            }

            for (int px = cx - half; px <= cx + half; px++) {
                int surfaceY = baseTop
                        + Math.round(Mth.sin(px * 0.52F + time * 0.09F) * 1.2F)
                        + Math.round(Mth.sin(px * 0.21F - time * 0.055F) * 0.7F);

                if (py < surfaceY) {
                    continue;
                }

                float depth = filled <= 1
                        ? 1.0F
                        : Mth.clamp((py - surfaceY) / (float) filled, 0.0F, 1.0F);

                int color = depth < 0.28F
                        ? lerpArgb(top, mid, depth / 0.28F)
                        : lerpArgb(mid, bottomColor, (depth - 0.28F) / 0.72F);

                graphics.fill(px, py, px + 1, py + 1, color);
            }
        }

        if (filled > 0) {
            for (int px = cx - radiusX + 2; px <= cx + radiusX - 2; px++) {
                int surfaceY = baseTop
                        + Math.round(Mth.sin(px * 0.52F + time * 0.09F) * 1.2F)
                        + Math.round(Mth.sin(px * 0.21F - time * 0.055F) * 0.7F);

                if (Math.floorMod(px + (int) time / 3, 5) != 0) {
                    graphics.fill(px, surfaceY, px + 1, surfaceY + 1, surface);
                }
            }
        }

        if (filled >= 7) {
            int fleck = state == MagicState.SUPERCRITICAL
                    ? 0xB06B1A20
                    : 0x9A565A64;

            drawFleck(graphics, cx - 7, bottom - 5, baseTop, fleck);
            drawFleck(graphics, cx + 4, bottom - 8, baseTop, fleck);
            drawFleck(graphics, cx - 1, bottom - 12, baseTop, fleck);
        }


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(
                CRYSTAL_FRAME,
                0,
                0,
                0,
                0,
                CRYSTAL_W,
                CRYSTAL_H,
                CRYSTAL_W,
                CRYSTAL_H
        );

        graphics.pose().popPose();
    }

    private static void drawFleck(
            GuiGraphics graphics,
            int x,
            int y,
            int liquidTop,
            int color
    ) {
        if (y >= liquidTop) {
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void renderElementSlots(GuiGraphics graphics, int x, int y, float time) {
        boolean consecrated = ClientConsecratedAreaData.isInside();


        graphics.blit(POWER_HOTBAR_BACK, x, y, 0, 0, POWER_BAR_W, POWER_BAR_H, POWER_BAR_W, POWER_BAR_H);


        for (int i = 0; i < SLOT_COUNT; i++) {
            float percent = Mth.clamp(ClientElementPowerData.percent(ELEMENT_ORDER[i]), 0.0F, 1.0F);
            if (percent <= 0.0F) continue;

            int slotX = x + 1 + i * SLOT_STEP;
            int bottom = y + LIQUID_Y + LIQUID_H;
            int filled = Math.max(1, Math.round(LIQUID_MAX_FILL_H * percent));
            int liquidTop = bottom - filled;

            for (int px = 0; px < LIQUID_W; px++) {
                int wave = Math.round(Mth.sin((px + i * 3) * 0.75F + time * 0.12F) * 0.7F);
                int surfaceY = Mth.clamp(liquidTop + wave, y + LIQUID_Y, bottom - 1);

                for (int py = surfaceY; py < bottom; py++) {
                    float depth = filled <= 1 ? 1.0F : Mth.clamp((py - surfaceY) / (float) filled, 0.0F, 1.0F);
                    float consecratedGlow = consecrated ? consecratedMagicalGlow(time, i) : 0.0F;
                    int topColor = pulseElementColor(ELEMENT_TOP_COLORS[i], i, consecratedGlow);
                    int bottomColor = pulseElementColor(ELEMENT_BOTTOM_COLORS[i], i, consecratedGlow * 0.48F);
                    int color = lerpArgb(topColor, bottomColor, depth);
                    graphics.fill(slotX + LIQUID_X_IN_SLOT + px, py,
                            slotX + LIQUID_X_IN_SLOT + px + 1, py + 1, color);
                }
            }

            float consecratedGlow = consecrated ? consecratedMagicalGlow(time, i) : 0.0F;
            int pulsedTop = pulseElementColor(ELEMENT_TOP_COLORS[i], i, consecratedGlow);
            int shineAlpha = Mth.lerpInt(consecratedGlow, 0x9A, 0xD8);
            int shine = (pulsedTop & 0x00FFFFFF) | (shineAlpha << 24);
            for (int px = 1; px < LIQUID_W - 1; px += 3) {
                int wave = Math.round(Mth.sin((px + i * 3) * 0.75F + time * 0.12F) * 0.7F);
                int surfaceY = Mth.clamp(liquidTop + wave, y + LIQUID_Y, bottom - 1);
                graphics.fill(slotX + LIQUID_X_IN_SLOT + px, surfaceY,
                        slotX + LIQUID_X_IN_SLOT + px + 1, surfaceY + 1, shine);
            }
        }

        if (consecrated) {
            renderConsecratedSheen(graphics, x, y, time);
        }


        graphics.blit(POWER_HOTBAR_FRAME, x, y, 0, 0, POWER_BAR_W, POWER_BAR_H, POWER_BAR_W, POWER_BAR_H);

        if (PowerHotbarManager.isPowerMode()) {
            renderElementSelector(graphics, x, y, PowerHotbarManager.getSelectedPowerSlot());
        }
    }


    private static void renderElementSelector(
            GuiGraphics graphics,
            int x,
            int y,
            int selectedIndex
    ) {
        int slotLeft =
                x + 1
                + Mth.clamp(selectedIndex, 0, SLOT_COUNT - 1) * SLOT_STEP;


        int selectorX = slotLeft - 2;
        int selectorY = y - 1;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(
                VANILLA_WIDGETS,
                selectorX,
                selectorY,
                0,
                22,
                24,
                22,
                256,
                256
        );
    }


    private static float consecratedMagicalGlow(float time, int elementIndex) {
        final float riseTicks = 16.0F;
        final float brightHoldTicks = 10.0F;
        final float fallTicks = 18.0F;
        final float normalHoldTicks = 36.0F;
        final float cycleTicks = riseTicks + brightHoldTicks + fallTicks + normalHoldTicks;

        float phase = Mth.positiveModulo(time, cycleTicks);
        final float peakGlow = 0.68F;

        if (phase < riseTicks) {
            return peakGlow * smootherStep(phase / riseTicks);
        }

        phase -= riseTicks;
        if (phase < brightHoldTicks) {
            return peakGlow;
        }

        phase -= brightHoldTicks;
        if (phase < fallTicks) {
            return peakGlow * (1.0F - smootherStep(phase / fallTicks));
        }

        return 0.0F;
    }

    private static float smootherStep(float value) {
        float x = Mth.clamp(value, 0.0F, 1.0F);
        return x * x * x * (x * (x * 6.0F - 15.0F) + 10.0F);
    }


    private static void renderConsecratedSheen(GuiGraphics graphics, int x, int y, float time) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            float percent = Mth.clamp(ClientElementPowerData.percent(ELEMENT_ORDER[i]), 0.0F, 1.0F);
            if (percent <= 0.04F) continue;

            float glow = consecratedMagicalGlow(time, i);
            if (glow < 0.34F) continue;

            int slotX = x + 1 + i * SLOT_STEP;
            int bottom = y + LIQUID_Y + LIQUID_H;
            int filled = Math.max(1, Math.round(LIQUID_MAX_FILL_H * percent));
            int liquidTop = bottom - filled;


            float sweep = Mth.positiveModulo(time * 0.11F - i * 0.17F, 1.0F);
            float center = -2.0F + sweep * (LIQUID_W + 4.0F);
            float halfLength = 3.6F;

            int base = pulseElementColor(ELEMENT_TOP_COLORS[i], i, glow);

            for (int px = 0; px < LIQUID_W; px++) {
                float distance = Math.abs(px - center);
                if (distance > halfLength) continue;

                float taper = 1.0F - distance / halfLength;
                taper = taper * taper * (3.0F - 2.0F * taper);

                int wave = Math.round(
                        Mth.sin((px + i * 3) * 0.75F + time * 0.12F) * 0.7F
                );
                int surfaceY = Mth.clamp(
                        liquidTop + wave,
                        y + LIQUID_Y,
                        bottom - 1
                );

                int alpha = Mth.clamp(
                        Math.round((0x18 + glow * 0x72) * taper),
                        0,
                        0x80
                );
                int sheen = (alpha << 24) | (base & 0x00FFFFFF);


                graphics.fill(
                        slotX + LIQUID_X_IN_SLOT + px,
                        surfaceY,
                        slotX + LIQUID_X_IN_SLOT + px + 1,
                        surfaceY + 1,
                        sheen
                );


                if (surfaceY + 1 < bottom && taper > 0.45F) {
                    int echoAlpha = Math.max(0, alpha / 3);
                    int echo = (echoAlpha << 24) | (base & 0x00FFFFFF);
                    graphics.fill(
                            slotX + LIQUID_X_IN_SLOT + px,
                            surfaceY + 1,
                            slotX + LIQUID_X_IN_SLOT + px + 1,
                            surfaceY + 2,
                            echo
                    );
                }
            }
        }
    }

    private static int pulseElementColor(int baseColor, int elementIndex, float pulse) {
        if (pulse <= 0.0F) return baseColor;
        int[] brightTargets = {
                0xFFE3B85A,
                0xFFB92E20,
                0xFF245D9B,
                0xFF286E3B
        };
        return lerpArgb(baseColor, brightTargets[elementIndex], pulse * 0.52F);
    }

    private static int lerpArgb(int from, int to, float amount) {
        amount = Mth.clamp(amount, 0.0F, 1.0F);

        int a = Mth.lerpInt(amount, (from >>> 24) & 0xFF, (to >>> 24) & 0xFF);
        int r = Mth.lerpInt(amount, (from >>> 16) & 0xFF, (to >>> 16) & 0xFF);
        int g = Mth.lerpInt(amount, (from >>> 8) & 0xFF, (to >>> 8) & 0xFF);
        int b = Mth.lerpInt(amount, from & 0xFF, to & 0xFF);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
