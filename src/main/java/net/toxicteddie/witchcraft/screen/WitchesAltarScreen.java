package net.toxicteddie.witchcraft.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.toxicteddie.witchcraft.menu.WitchesAltarMenu;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.RemoveAltarBindingPacket;

import java.util.List;
import java.util.UUID;

public class WitchesAltarScreen extends AbstractContainerScreen<WitchesAltarMenu> {

    private static final int PANEL_WIDTH = 230;
    private static final int PANEL_HEIGHT = 184;
    private static final int LIST_TOP = 96;
    private static final int ROW_HEIGHT = 18;
    private static final int MAX_VISIBLE = 4;

    public WitchesAltarScreen(WitchesAltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildBindingButtons();
    }

    private void rebuildBindingButtons() {
        this.clearWidgets();
        if (!this.menu.isOwnerView()) {
            return;
        }

        List<WitchesAltarMenu.BoundWitch> witches = this.menu.getBoundWitches();
        int shown = Math.min(MAX_VISIBLE, witches.size());
        for (int i = 0; i < shown; i++) {
            WitchesAltarMenu.BoundWitch witch = witches.get(i);
            int buttonX = this.leftPos + PANEL_WIDTH - 33;
            int buttonY = this.topPos + LIST_TOP + i * ROW_HEIGHT - 2;
            this.addRenderableWidget(
                    Button.builder(Component.literal("×"), button -> this.removeBinding(witch.uuid()))
                            .bounds(buttonX, buttonY, 16, 16)
                            .build()
            );
        }
    }

    private void removeBinding(UUID witchUuid) {
        ModNetworking.CHANNEL.sendToServer(
                new RemoveAltarBindingPacket(this.menu.getAltarPos(), witchUuid)
        );
        this.menu.removeClientBoundWitch(witchUuid);
        this.rebuildBindingButtons();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;


        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF1B1214);
        graphics.fill(left + 3, top + 3, left + PANEL_WIDTH - 3, top + PANEL_HEIGHT - 3, 0xFF4A3028);
        graphics.fill(left + 7, top + 7, left + PANEL_WIDTH - 7, top + PANEL_HEIGHT - 7, 0xFFE1C99D);
        graphics.fill(left + 10, top + 10, left + PANEL_WIDTH - 10, top + PANEL_HEIGHT - 10, 0xFFD4B887);


        graphics.fill(left + 16, top + 34, left + PANEL_WIDTH - 16, top + 36, 0xFF5B342C);
        graphics.fill(left + 18, top + 50, left + PANEL_WIDTH - 18, top + 64, 0xFF2A1A20);

        int filledWidth = Math.round((PANEL_WIDTH - 40) * this.menu.getPowerFraction());
        graphics.fill(left + 20, top + 52, left + 20 + filledWidth, top + 62, 0xFF76517E);


        graphics.fill(left + 14, top + 82, left + PANEL_WIDTH - 14, top + PANEL_HEIGHT - 14, 0x663B211D);
        graphics.fill(left + 17, top + 85, left + PANEL_WIDTH - 17, top + PANEL_HEIGHT - 17, 0x55F2D9A7);

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int y = top + LIST_TOP + i * ROW_HEIGHT + 14;
            graphics.fill(left + 22, y, left + PANEL_WIDTH - 22, y + 1, 0x665B342C);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, 16, 0x3A201D);

        String owner = this.menu.getOwnerName().isBlank() ? "Unclaimed" : this.menu.getOwnerName();
        graphics.drawCenteredString(
                this.font,
                Component.literal("Bound to " + owner),
                this.imageWidth / 2,
                27,
                0x704032
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getStoredPower() + " / " + this.menu.getCapacity()),
                this.imageWidth / 2,
                68,
                0x3A201D
        );

        graphics.drawString(this.font, "Blood-Bound Witches", 22, 87, 0x4A2823, false);

        List<WitchesAltarMenu.BoundWitch> witches = this.menu.getBoundWitches();
        if (witches.isEmpty()) {
            graphics.drawString(this.font, "No other witches are bound.", 24, 103, 0x805E4A, false);
        } else {
            int shown = Math.min(MAX_VISIBLE, witches.size());
            for (int i = 0; i < shown; i++) {
                String name = witches.get(i).name();
                if (this.font.width(name) > 160) {
                    name = this.font.plainSubstrByWidth(name, 157) + "…";
                }
                graphics.drawString(this.font, name, 24, LIST_TOP + i * ROW_HEIGHT + 2, 0x3A201D, false);
            }
            if (witches.size() > MAX_VISIBLE) {
                graphics.drawString(
                        this.font,
                        "+ " + (witches.size() - MAX_VISIBLE) + " more blood bindings",
                        24,
                        PANEL_HEIGHT - 27,
                        0x704032,
                        false
                );
            }
        }

        if (!this.menu.isOwnerView()) {
            graphics.drawString(this.font, "Only the owner may sever bindings.", 24, PANEL_HEIGHT - 27, 0x704032, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
