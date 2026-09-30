package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.item.ModItems;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;


public enum WandWoodType {
    OAK("oak", "Oak"),
    SPRUCE("spruce", "Spruce"),
    BIRCH("birch", "Birch"),
    JUNGLE("jungle", "Jungle"),
    ACACIA("acacia", "Acacia"),
    DARK_OAK("dark_oak", "Dark Oak"),
    MANGROVE("mangrove", "Mangrove"),
    CHERRY("cherry", "Cherry"),
    BAMBOO("bamboo", "Bamboo"),
    CRIMSON("crimson", "Crimson"),
    WARPED("warped", "Warped"),
    ROWAN("rowan", "Rowan"),
    ELDER("elder", "Elder");

    private final String id;
    private final String displayName;

    WandWoodType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public boolean matchesGrowthToken(ItemStack stack) {
        return switch (this) {
            case OAK -> stack.is(Items.OAK_SAPLING);
            case SPRUCE -> stack.is(Items.SPRUCE_SAPLING);
            case BIRCH -> stack.is(Items.BIRCH_SAPLING);
            case JUNGLE -> stack.is(Items.JUNGLE_SAPLING);
            case ACACIA -> stack.is(Items.ACACIA_SAPLING);
            case DARK_OAK -> stack.is(Items.DARK_OAK_SAPLING);
            case MANGROVE -> stack.is(Items.MANGROVE_PROPAGULE);
            case CHERRY -> stack.is(Items.CHERRY_SAPLING);


            case BAMBOO -> stack.is(Items.BAMBOO);
            case CRIMSON -> stack.is(Items.CRIMSON_FUNGUS);
            case WARPED -> stack.is(Items.WARPED_FUNGUS);
            case ROWAN -> stack.is(ModBlocks.ROWAN_SAPLING.get().asItem());
            case ELDER -> stack.is(ModBlocks.ELDER_SAPLING.get().asItem());
        };
    }

    public Item coreItem() {
        return coreRegistry().get();
    }

    public Item wandItem() {
        return wandRegistry().get();
    }

    private RegistryObject<Item> coreRegistry() {
        return switch (this) {
            case OAK -> ModItems.OAK_WAND_CORE;
            case SPRUCE -> ModItems.SPRUCE_WAND_CORE;
            case BIRCH -> ModItems.BIRCH_WAND_CORE;
            case JUNGLE -> ModItems.JUNGLE_WAND_CORE;
            case ACACIA -> ModItems.ACACIA_WAND_CORE;
            case DARK_OAK -> ModItems.DARK_OAK_WAND_CORE;
            case MANGROVE -> ModItems.MANGROVE_WAND_CORE;
            case CHERRY -> ModItems.CHERRY_WAND_CORE;
            case BAMBOO -> ModItems.BAMBOO_WAND_CORE;
            case CRIMSON -> ModItems.CRIMSON_WAND_CORE;
            case WARPED -> ModItems.WARPED_WAND_CORE;
            case ROWAN -> ModItems.ROWAN_WAND_CORE;
            case ELDER -> ModItems.ELDER_WAND_CORE;
        };
    }

    private RegistryObject<Item> wandRegistry() {
        return switch (this) {
            case OAK -> ModItems.OAK_WAND;
            case SPRUCE -> ModItems.SPRUCE_WAND;
            case BIRCH -> ModItems.BIRCH_WAND;
            case JUNGLE -> ModItems.JUNGLE_WAND;
            case ACACIA -> ModItems.ACACIA_WAND;
            case DARK_OAK -> ModItems.DARK_OAK_WAND;
            case MANGROVE -> ModItems.MANGROVE_WAND;
            case CHERRY -> ModItems.CHERRY_WAND;
            case BAMBOO -> ModItems.BAMBOO_WAND;
            case CRIMSON -> ModItems.CRIMSON_WAND;
            case WARPED -> ModItems.WARPED_WAND;
            case ROWAN -> ModItems.ROWAN_WAND;
            case ELDER -> ModItems.ELDER_WAND;
        };
    }

    @Nullable
    public static WandWoodType fromGrowthToken(ItemStack stack) {
        for (WandWoodType type : values()) {
            if (type.matchesGrowthToken(stack)) return type;
        }
        return null;
    }

    @Nullable
    public static WandWoodType fromCore(ItemStack stack) {
        for (WandWoodType type : values()) {
            if (stack.is(type.coreItem())) return type;
        }
        return null;
    }

    @Nullable
    public static WandWoodType fromId(String id) {
        if (id == null) return null;
        for (WandWoodType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }

    public Component coreDisplayName() {
        return Component.literal(displayName + " Wand Core");
    }

    public Component wandDisplayName() {
        return Component.literal(displayName + " Wand");
    }
}
