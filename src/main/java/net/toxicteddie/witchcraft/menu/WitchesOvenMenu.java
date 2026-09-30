package net.toxicteddie.witchcraft.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.entity.WitchesOvenBlockEntity;
import net.toxicteddie.witchcraft.item.ModItems;

public class WitchesOvenMenu extends AbstractContainerMenu {
    private final WitchesOvenBlockEntity oven;
    private final ContainerData data;

    public WitchesOvenMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory,
                (WitchesOvenBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()),
                new SimpleContainerData(5));
    }

    public WitchesOvenMenu(int id, Inventory inventory, WitchesOvenBlockEntity oven, ContainerData data) {
        super(ModMenuTypes.WITCHES_OVEN_MENU.get(), id);
        this.oven = oven;
        this.data = data;

        var handler = oven.getItemHandler();


        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.WATER_BUCKET_SLOT, 17, 18));
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.BOTTLE_SLOT, 69, 56));
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.REAGENT_SLOT, 41, 20));
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.FUEL_SLOT, 41, 56));
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.OUTPUT_SLOT, 122, 24) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.BYPRODUCT_1_SLOT, 99, 56) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.BYPRODUCT_2_SLOT, 122, 56) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new SlotItemHandler(handler, WitchesOvenBlockEntity.BYPRODUCT_3_SLOT, 145, 56) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });


        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public int getWaterLevel() { return data.get(0); }
    public int getBrewProgress() { return data.get(1); }
    public int getBurnTime() { return data.get(2); }
    public int getMaxBurnTime() { return data.get(3); }
    public int getCurrentBrewTime() { return data.get(4); }

    public int getScaledBrewProgress(int width) {
        return getCurrentBrewTime() <= 0 ? 0 : getBrewProgress() * width / getCurrentBrewTime();
    }

    public int getScaledBurnTime(int height) {
        return getMaxBurnTime() <= 0 ? 0 : getBurnTime() * height / getMaxBurnTime();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(oven.getLevel(), oven.getBlockPos()), player, ModBlocks.WITCHES_OVEN.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index < 8) {
            if (!moveItemStackTo(source, 8, 44, true)) return ItemStack.EMPTY;
        } else if (source.is(Items.WATER_BUCKET)) {
            if (!moveItemStackTo(source, 0, 1, false)) return ItemStack.EMPTY;
        } else if (source.is(ModItems.TINCTURE_BOTTLE.get())) {
            if (!moveItemStackTo(source, 1, 2, false)) return ItemStack.EMPTY;
        } else if (ForgeHooks.getBurnTime(source, RecipeType.SMELTING) > 0) {
            if (!moveItemStackTo(source, 3, 4, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, 2, 3, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
