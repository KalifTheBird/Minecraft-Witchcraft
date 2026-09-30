package net.toxicteddie.witchcraft.block;

import net.toxicteddie.witchcraft.block.custom.MagicalCrystalClusterBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.custom.*;
import net.toxicteddie.witchcraft.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Witchcraft.MOD_ID);


    public static final RegistryObject<Block> BOOK_OF_SHADOWS = BLOCKS.register(
            "book_of_shadows",
            () -> new BookOfShadowsBlock(
                    BlockBehaviour.Properties.copy(Blocks.LECTERN)
                            .strength(0.5F)
                            .noOcclusion()
            )
    );

    public static final RegistryObject<Block> MORTAR_AND_PESTLE =
            registerBlock(
                    "mortar_and_pestle",
                    () -> new MortarAndPestleBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.STONE)
                                    .strength(1.5F)
                                    .noOcclusion()
                    )
            );
    public static final RegistryObject<Block> WITCHES_CAULDRON = registerBlock("witches_cauldron",
            () -> new WitchesCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON).noOcclusion()));
    public static final RegistryObject<Block> WITCHES_OVEN =
            registerBlock(
                    "witches_oven",
                    () -> new WitchesOvenBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.IRON_BLOCK)
                                    .lightLevel(state -> state.getValue(WitchesOvenBlock.LIT) ? 13 : 0)
                                    .noOcclusion()
                    )
            );


    public static final RegistryObject<Block> GLIMMERSTONE_CLUSTER =
            registerBlock(
                    "glimmerstone_cluster",
                    () -> new MagicalCrystalClusterBlock(
                            7, 3,
                            BlockBehaviour.Properties.copy(Blocks.AMETHYST_CLUSTER)
                                    .lightLevel(state -> 10)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> GLOOMSTONE_CLUSTER =
            registerBlock(
                    "gloomstone_cluster",
                    () -> new MagicalCrystalClusterBlock(
                            7, 3,
                            BlockBehaviour.Properties.copy(Blocks.AMETHYST_CLUSTER)
                                    .lightLevel(state -> 5)
                                    .noOcclusion()
                    )
            );


    public static final RegistryObject<Block> STRIPPED_ROWAN_LOG = registerBlock("stripped_rowan_log",
            () -> new FlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG), 5, 5));
    public static final RegistryObject<Block> STRIPPED_ROWAN_WOOD = registerBlock("stripped_rowan_wood",
            () -> new FlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD), 5, 5));
    public static final RegistryObject<Block> ROWAN_LOG = registerBlock("rowan_log",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG), STRIPPED_ROWAN_LOG));
    public static final RegistryObject<Block> ROWAN_WOOD = registerBlock("rowan_wood",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD), STRIPPED_ROWAN_WOOD));
    public static final RegistryObject<Block> ROWAN_PLANKS = registerBlock("rowan_planks",
            () -> new FlammableBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), 20, 5));
    public static final RegistryObject<Block> ROWAN_LEAVES = registerBlock("rowan_leaves",
            () -> new FlammableLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> ROWAN_BERRY_LEAVES = registerBlock("rowan_berry_leaves",
            () -> new FlammableLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> ROWAN_SAPLING = registerBlock("rowan_sapling",
            () -> new SaplingBlock(ModTreeGrower.ROWAN, BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> ROWAN_STAIRS = registerBlock("rowan_stairs",
            () -> new FlammableStairBlock(() -> ROWAN_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> ROWAN_SLAB = registerBlock("rowan_slab",
            () -> new FlammableSlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
    public static final RegistryObject<Block> ROWAN_FENCE = registerBlock("rowan_fence",
            () -> new FlammableFenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> ROWAN_FENCE_GATE = registerBlock("rowan_fence_gate",
            () -> new FlammableFenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE), WoodType.OAK));
    public static final RegistryObject<Block> ROWAN_PRESSURE_PLATE = registerBlock("rowan_pressure_plate",
            () -> new FlammablePressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE), BlockSetType.OAK));
    public static final RegistryObject<Block> ROWAN_BUTTON = registerBlock("rowan_button",
            () -> new FlammableButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true));


    public static final RegistryObject<Block> STRIPPED_ELDER_LOG = registerBlock("stripped_elder_log",
            () -> new FlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG), 5, 5));
    public static final RegistryObject<Block> STRIPPED_ELDER_WOOD = registerBlock("stripped_elder_wood",
            () -> new FlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD), 5, 5));
    public static final RegistryObject<Block> ELDER_LOG = registerBlock("elder_log",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG), STRIPPED_ELDER_LOG));
    public static final RegistryObject<Block> ELDER_WOOD = registerBlock("elder_wood",
            () -> new StrippableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD), STRIPPED_ELDER_WOOD));
    public static final RegistryObject<Block> ELDER_PLANKS = registerBlock("elder_planks",
            () -> new FlammableBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), 20, 5));
    public static final RegistryObject<Block> ELDER_LEAVES = registerBlock("elder_leaves",
            () -> new FlammableLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> ELDER_BERRY_LEAVES = registerBlock("elder_berry_leaves",
            () -> new FlammableLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final RegistryObject<Block> ELDER_SAPLING = registerBlock("elder_sapling",
            () -> new SaplingBlock(ModTreeGrower.ELDER, BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));
    public static final RegistryObject<Block> ELDER_STAIRS = registerBlock("elder_stairs",
            () -> new FlammableStairBlock(() -> ELDER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> ELDER_SLAB = registerBlock("elder_slab",
            () -> new FlammableSlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
    public static final RegistryObject<Block> ELDER_FENCE = registerBlock("elder_fence",
            () -> new FlammableFenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> ELDER_FENCE_GATE = registerBlock("elder_fence_gate",
            () -> new FlammableFenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE), WoodType.OAK));
    public static final RegistryObject<Block> ELDER_PRESSURE_PLATE = registerBlock("elder_pressure_plate",
            () -> new FlammablePressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,
                    BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE), BlockSetType.OAK));
    public static final RegistryObject<Block> ELDER_BUTTON = registerBlock("elder_button",
            () -> new FlammableButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true));


    public static final RegistryObject<Block> BELLAD0NNA_CROP = BLOCKS.register("belladonna_crop",
            () -> new BelladonnaCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));
    public static final RegistryObject<Block> HEMLOCK_CROP = BLOCKS.register("hemlock_crop",
            () -> new HemlockCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));
    public static final RegistryObject<Block> MANDRAKE_CROP = BLOCKS.register("mandrake_crop",
            () -> new MandrakeCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));
    public static final RegistryObject<Block> SAGE_CROP = BLOCKS.register("sage_crop",
            () -> new SageCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));
    public static final RegistryObject<Block> WOLFSBANE_CROP = BLOCKS.register("wolfsbane_crop",
            () -> new WolfsbaneCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));


    public static final RegistryObject<Block> CHALK_GLYPH = BLOCKS.register("chalk_glyph",
            () -> new ChalkGlyphBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET)
                    .noCollission().noOcclusion().instabreak()
                    .lightLevel(state -> state.getValue(ChalkGlyphBlock.RITUAL_LIT) ? 14 : 0)));
    public static final RegistryObject<Block> SALT_LINE = BLOCKS.register("salt_line",
            () -> new SaltLineBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)
                    .noCollission().noOcclusion().instabreak().sound(SoundType.SAND)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }


    public static final RegistryObject<Block> OAK_WITCHES_ALTAR =
            registerBlock(
                    "oak_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.OAK_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> SPRUCE_WITCHES_ALTAR =
            registerBlock(
                    "spruce_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.SPRUCE_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> BIRCH_WITCHES_ALTAR =
            registerBlock(
                    "birch_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.BIRCH_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> JUNGLE_WITCHES_ALTAR =
            registerBlock(
                    "jungle_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.JUNGLE_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> ACACIA_WITCHES_ALTAR =
            registerBlock(
                    "acacia_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.ACACIA_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> DARK_OAK_WITCHES_ALTAR =
            registerBlock(
                    "dark_oak_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.DARK_OAK_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> MANGROVE_WITCHES_ALTAR =
            registerBlock(
                    "mangrove_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.MANGROVE_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> CHERRY_WITCHES_ALTAR =
            registerBlock(
                    "cherry_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.CHERRY_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> BAMBOO_WITCHES_ALTAR =
            registerBlock(
                    "bamboo_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.BAMBOO_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> CRIMSON_WITCHES_ALTAR =
            registerBlock(
                    "crimson_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.CRIMSON_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> WARPED_WITCHES_ALTAR =
            registerBlock(
                    "warped_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.WARPED_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> ROWAN_WITCHES_ALTAR =
            registerBlock(
                    "rowan_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.OAK_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<Block> ELDER_WITCHES_ALTAR =
            registerBlock(
                    "elder_witches_altar",
                    () -> new WitchesAltarBlock(
                            BlockBehaviour.Properties
                                    .copy(Blocks.OAK_PLANKS)
                                    .strength(2.5F)
                                    .noOcclusion()
                    )
            );

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
