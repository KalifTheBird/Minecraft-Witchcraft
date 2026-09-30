package net.toxicteddie.witchcraft.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.entity.WitchesOvenBlockEntity;
import net.toxicteddie.witchcraft.menu.WitchesOvenMenu;

public class WitchesOvenScreen extends AbstractContainerScreen<WitchesOvenMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/container/witches_oven.png");
    private static final ResourceLocation VANILLA_FURNACE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private static final int WATER_X = 15;
    private static final int WATER_Y = 36;
    private static final int WATER_WIDTH = 20;
    private static final int WATER_HEIGHT = 36;

    public WitchesOvenScreen(WitchesOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 74;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        renderWater(graphics, x, y);
        renderFlame(graphics, x, y);
        renderProgressArrow(graphics, x, y);
    }

    private void renderWater(GuiGraphics graphics, int x, int y) {
        int waterLevel = Math.max(0, Math.min(menu.getWaterLevel(), WitchesOvenBlockEntity.MAX_WATER));
        if (waterLevel == 0) return;


        int waterHeight = Math.min(
                WATER_HEIGHT,
                (waterLevel * WATER_HEIGHT + WitchesOvenBlockEntity.MAX_WATER - 1)
                        / WitchesOvenBlockEntity.MAX_WATER
        );

        int waterTop = y + WATER_Y + WATER_HEIGHT - waterHeight;
        graphics.fill(
                x + WATER_X,
                waterTop,
                x + WATER_X + WATER_WIDTH,
                y + WATER_Y + WATER_HEIGHT,
                0xFF3F76E4
        );


        graphics.fill(
                x + WATER_X,
                waterTop,
                x + WATER_X + WATER_WIDTH,
                waterTop + 1,
                0xFF5D9CFF
        );
    }

    private void renderFlame(GuiGraphics graphics, int x, int y) {
        int flameHeight = menu.getScaledBurnTime(13);
        if (flameHeight <= 0) return;

        graphics.blit(
                VANILLA_FURNACE,
                x + 42,
                y + 39 + 12 - flameHeight,
                176,
                12 - flameHeight,
                14,
                flameHeight + 1,
                256,
                256
        );
    }

    private void renderProgressArrow(GuiGraphics graphics, int x, int y) {
        final int arrowWidth = 41;
        final int arrowHeight = 16;
        int filledWidth = menu.getScaledBrewProgress(arrowWidth);
        if (filledWidth <= 0) return;

        graphics.blit(
                BACKGROUND,
                x + 67,
                y + 33,
                215,
                240,
                filledWidth,
                arrowHeight,
                256,
                256
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
