package net.toxicteddie.witchcraft.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.toxicteddie.witchcraft.Witchcraft;

public final class ModTags {

    private ModTags() {
    }

    public static final class Blocks {

        public static final TagKey<Block> ALTAR_CARPETS =
                create("altar_carpets");

        public static final TagKey<Block> ALTAR_POWER_SMALL =
                create("altar_power_small");

        public static final TagKey<Block> ALTAR_POWER_MEDIUM =
                create("altar_power_medium");

        public static final TagKey<Block> ALTAR_POWER_LARGE =
                create("altar_power_large");

        private static TagKey<Block> create(String name) {
            return TagKey.create(
                    Registries.BLOCK,
                    new ResourceLocation(
                            Witchcraft.MOD_ID,
                            name
                    )
            );
        }

        private Blocks() {
        }
    }

    public static final class EntityTypes {


        public static final TagKey<EntityType<?>> DEMONS =
                TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Witchcraft.MOD_ID, "demons"));

        private EntityTypes() {
        }
    }

    public static final class Items {

        public static final TagKey<Item> ALTAR_CANDLES =
                create("altar_candles");

        public static final TagKey<Item> ALTAR_POWER_MODIFIERS =
                create("altar_power_modifiers");

        public static final TagKey<Item> ALTAR_CAPACITY_MODIFIERS =
                create("altar_capacity_modifiers");

        private static TagKey<Item> create(String name) {
            return TagKey.create(
                    Registries.ITEM,
                    new ResourceLocation(
                            Witchcraft.MOD_ID,
                            name
                    )
            );
        }

        private Items() {
        }
    }
}
