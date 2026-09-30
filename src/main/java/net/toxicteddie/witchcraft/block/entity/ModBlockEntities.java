package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.ModBlocks;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>>
            BLOCK_ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.BLOCK_ENTITY_TYPES,
                    Witchcraft.MOD_ID
            );

    public static final RegistryObject<BlockEntityType<BookOfShadowsBlockEntity>> BOOK_OF_SHADOWS_BE =
            BLOCK_ENTITIES.register(
                    "book_of_shadows",
                    () -> BlockEntityType.Builder.of(
                            BookOfShadowsBlockEntity::new,
                            ModBlocks.BOOK_OF_SHADOWS.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<ChalkGlyphBlockEntity>> CHALK_GLYPH_BE =
            BLOCK_ENTITIES.register(
                    "chalk_glyph",
                    () -> BlockEntityType.Builder.of(
                            ChalkGlyphBlockEntity::new,
                            ModBlocks.CHALK_GLYPH.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<WitchesOvenBlockEntity>> WITCHES_OVEN_BE =
            BLOCK_ENTITIES.register(
                    "witches_oven",
                    () -> BlockEntityType.Builder.of(
                            WitchesOvenBlockEntity::new,
                            ModBlocks.WITCHES_OVEN.get()
                    ).build(null)
            );

    public static final RegistryObject<
            BlockEntityType<WitchesCauldronBlockEntity>
            > WITCHES_CAULDRON_BE =
            BLOCK_ENTITIES.register(
                    "witches_cauldron",
                    () -> BlockEntityType.Builder.of(
                            WitchesCauldronBlockEntity::new,
                            ModBlocks.WITCHES_CAULDRON.get()
                    ).build(null)
            );

    public static final RegistryObject<
            BlockEntityType<MortarAndPestleBlockEntity>
            > MORTAR_AND_PESTLE_BE =
            BLOCK_ENTITIES.register(
                    "mortar_and_pestle",
                    () -> BlockEntityType.Builder.of(
                            MortarAndPestleBlockEntity::new,
                            ModBlocks.MORTAR_AND_PESTLE.get()
                    ).build(null)
            );

    public static final RegistryObject<
            BlockEntityType<WitchesAltarBlockEntity>
            > WITCHES_ALTAR_BE =
            BLOCK_ENTITIES.register(
                    "witches_altar",
                    () -> BlockEntityType.Builder.of(
                            WitchesAltarBlockEntity::new,

                            ModBlocks.OAK_WITCHES_ALTAR.get(),
                            ModBlocks.SPRUCE_WITCHES_ALTAR.get(),
                            ModBlocks.BIRCH_WITCHES_ALTAR.get(),
                            ModBlocks.JUNGLE_WITCHES_ALTAR.get(),
                            ModBlocks.ACACIA_WITCHES_ALTAR.get(),
                            ModBlocks.DARK_OAK_WITCHES_ALTAR.get(),
                            ModBlocks.MANGROVE_WITCHES_ALTAR.get(),
                            ModBlocks.CHERRY_WITCHES_ALTAR.get(),
                            ModBlocks.BAMBOO_WITCHES_ALTAR.get(),
                            ModBlocks.CRIMSON_WITCHES_ALTAR.get(),
                            ModBlocks.WARPED_WITCHES_ALTAR.get(),
                            ModBlocks.ROWAN_WITCHES_ALTAR.get(),
                            ModBlocks.ELDER_WITCHES_ALTAR.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
