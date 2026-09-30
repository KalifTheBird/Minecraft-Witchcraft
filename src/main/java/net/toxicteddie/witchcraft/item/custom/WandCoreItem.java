package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class WandCoreItem extends Item {
    private final WandWoodType woodType;

    public WandCoreItem(WandWoodType woodType, Properties properties) {
        super(properties);
        this.woodType = woodType;
    }

    public WandWoodType woodType() {
        return woodType;
    }

    @Override
    public Component getName(ItemStack stack) {
        return woodType.coreDisplayName();
    }
}
