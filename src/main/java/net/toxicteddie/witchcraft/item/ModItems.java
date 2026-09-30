package net.toxicteddie.witchcraft.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.custom.ChalkType;
import net.toxicteddie.witchcraft.entity.ModEntities;
import net.toxicteddie.witchcraft.item.custom.RitualChalkItem;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.item.custom.BoneNeedleItem;
import net.toxicteddie.witchcraft.item.custom.SaltItem;
import net.toxicteddie.witchcraft.item.custom.WandWoodType;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.item.custom.WandCoreItem;
import net.toxicteddie.witchcraft.item.custom.WitchesHatItem;
import net.toxicteddie.witchcraft.item.custom.SatchelItem;

import static net.toxicteddie.witchcraft.block.ModBlocks.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Witchcraft.MOD_ID);

    public static final RegistryObject<Item> ATHAME = ITEMS.register("athame",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WITCHES_HAT = ITEMS.register("witches_hat",
            () -> new WitchesHatItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SATCHEL = ITEMS.register("satchel",
            () -> new SatchelItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BELLADONNA_FLOWER = ITEMS.register("belladonna_flower",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BELLADONNA_SEEDS = ITEMS.register("belladonna_seeds",
            () -> new ItemNameBlockItem(BELLAD0NNA_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> BLACKENED_HEART = ITEMS.register("blackened_heart",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BONE_NEEDLE = ITEMS.register("bone_needle",
            () -> new BoneNeedleItem(new Item.Properties().durability(64)));
    public static final RegistryObject<Item> BOTTLE_OF_BLOOD = ITEMS.register("bottle_of_blood",
            () -> new BloodBottleItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOOK_OF_SHADOWS = ITEMS.register("book_of_shadows",
            () -> new BlockItem(net.toxicteddie.witchcraft.block.ModBlocks.BOOK_OF_SHADOWS.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_ESSENCE = ITEMS.register("dark_essence",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELDER_BERRIES = ITEMS.register("elder_berries",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLIMMERSTONE_DUST = ITEMS.register("glimmerstone_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLIMMERSTONE_SHARD = ITEMS.register("glimmerstone_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOOMSTONE_DUST = ITEMS.register("gloomstone_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOOMSTONE_SHARD = ITEMS.register("gloomstone_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEART_OF_A_DEMON = ITEMS.register("heart_of_a_demon",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEART_OF_A_MORTAL = ITEMS.register("heart_of_a_mortal",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEMLOCK_FLOWER = ITEMS.register("hemlock_flower",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEMLOCK_ROOT = ITEMS.register("hemlock_root",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEMLOCK_SEEDS = ITEMS.register("hemlock_seeds",
            () -> new ItemNameBlockItem(HEMLOCK_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> MANDRAKE_POWDER = ITEMS.register("mandrake_powder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MANDRAKE_ROOT = ITEMS.register("mandrake_root",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MANDRAKE_SEEDS = ITEMS.register("mandrake_seeds",
            () -> new ItemNameBlockItem(MANDRAKE_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> PENTACLE = ITEMS.register("pentacle",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> POPPET = ITEMS.register("poppet",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PURE_ESSENCE = ITEMS.register("pure_essence",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_SALT = ITEMS.register("raw_salt",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROWAN_BERRIES = ITEMS.register("rowan_berries",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SAGE_LEAF = ITEMS.register("sage_leaf",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SAGE_SEEDS = ITEMS.register("sage_seeds",
            () -> new ItemNameBlockItem(SAGE_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt",
            () -> new SaltItem(new Item.Properties()));
    public static final RegistryObject<Item> TONGUE_OF_DOG = ITEMS.register("tongue_of_dog",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TORN_SHADOWY_PAGE = ITEMS.register("torn_shadowy_page",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WILD_ESSENCE = ITEMS.register("wild_essence",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOLFSBANE_FLOWER = ITEMS.register("wolfsbane_flower",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOLFSBANE_SEEDS = ITEMS.register("wolfsbane_seeds",
            () -> new ItemNameBlockItem(WOLFSBANE_CROP.get(), new Item.Properties()));
    public static final RegistryObject<Item> WOOD_ASH = ITEMS.register("wood_ash",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOOL_OF_BAT = ITEMS.register("wool_of_bat",
            () -> new Item(new Item.Properties()));


    public static final RegistryObject<Item> OAK_WAND_CORE = ITEMS.register("oak_wand_core",
            () -> new WandCoreItem(WandWoodType.OAK, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OAK_WAND = ITEMS.register("oak_wand",
            () -> new WandItem(WandWoodType.OAK, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPRUCE_WAND_CORE = ITEMS.register("spruce_wand_core",
            () -> new WandCoreItem(WandWoodType.SPRUCE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SPRUCE_WAND = ITEMS.register("spruce_wand",
            () -> new WandItem(WandWoodType.SPRUCE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BIRCH_WAND_CORE = ITEMS.register("birch_wand_core",
            () -> new WandCoreItem(WandWoodType.BIRCH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BIRCH_WAND = ITEMS.register("birch_wand",
            () -> new WandItem(WandWoodType.BIRCH, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUNGLE_WAND_CORE = ITEMS.register("jungle_wand_core",
            () -> new WandCoreItem(WandWoodType.JUNGLE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUNGLE_WAND = ITEMS.register("jungle_wand",
            () -> new WandItem(WandWoodType.JUNGLE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACACIA_WAND_CORE = ITEMS.register("acacia_wand_core",
            () -> new WandCoreItem(WandWoodType.ACACIA, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACACIA_WAND = ITEMS.register("acacia_wand",
            () -> new WandItem(WandWoodType.ACACIA, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_OAK_WAND_CORE = ITEMS.register("dark_oak_wand_core",
            () -> new WandCoreItem(WandWoodType.DARK_OAK, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_OAK_WAND = ITEMS.register("dark_oak_wand",
            () -> new WandItem(WandWoodType.DARK_OAK, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANGROVE_WAND_CORE = ITEMS.register("mangrove_wand_core",
            () -> new WandCoreItem(WandWoodType.MANGROVE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANGROVE_WAND = ITEMS.register("mangrove_wand",
            () -> new WandItem(WandWoodType.MANGROVE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHERRY_WAND_CORE = ITEMS.register("cherry_wand_core",
            () -> new WandCoreItem(WandWoodType.CHERRY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHERRY_WAND = ITEMS.register("cherry_wand",
            () -> new WandItem(WandWoodType.CHERRY, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BAMBOO_WAND_CORE = ITEMS.register("bamboo_wand_core",
            () -> new WandCoreItem(WandWoodType.BAMBOO, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BAMBOO_WAND = ITEMS.register("bamboo_wand",
            () -> new WandItem(WandWoodType.BAMBOO, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CRIMSON_WAND_CORE = ITEMS.register("crimson_wand_core",
            () -> new WandCoreItem(WandWoodType.CRIMSON, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CRIMSON_WAND = ITEMS.register("crimson_wand",
            () -> new WandItem(WandWoodType.CRIMSON, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WARPED_WAND_CORE = ITEMS.register("warped_wand_core",
            () -> new WandCoreItem(WandWoodType.WARPED, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WARPED_WAND = ITEMS.register("warped_wand",
            () -> new WandItem(WandWoodType.WARPED, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROWAN_WAND_CORE = ITEMS.register("rowan_wand_core",
            () -> new WandCoreItem(WandWoodType.ROWAN, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ROWAN_WAND = ITEMS.register("rowan_wand",
            () -> new WandItem(WandWoodType.ROWAN, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELDER_WAND_CORE = ITEMS.register("elder_wand_core",
            () -> new WandCoreItem(WandWoodType.ELDER, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ELDER_WAND = ITEMS.register("elder_wand",
            () -> new WandItem(WandWoodType.ELDER, new Item.Properties().stacksTo(1)));


    public static final RegistryObject<Item> TINCTURE_BOTTLE = ITEMS.register("tincture_bottle",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLOOD_OF_THE_DAMNED = ITEMS.register("blood_of_the_damned",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLESSING_OF_THE_GODDESS = ITEMS.register("blessing_of_the_goddess",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DARK_UNCTION = ITEMS.register("dark_unction",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DAWNS_FIRST_LIGHT = ITEMS.register("dawns_first_light",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEW_OF_THE_ETERNAL = ITEMS.register("dew_of_the_eternal",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HOWL_OF_THE_WILD = ITEMS.register("howl_of_the_wild",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MILK_OF_THE_MOTHER = ITEMS.register("milk_of_the_mother",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MURMUR_OF_THE_SPIRITS = ITEMS.register("murmur_of_the_spirits",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PALE_MOONLIGHT = ITEMS.register("pale_moonlight",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PULSE_OF_MORTALITY = ITEMS.register("pulse_of_mortality",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REMNANT_OF_THE_VEIL = ITEMS.register("remnant_of_the_veil",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCREAM_OF_THE_PIT = ITEMS.register("scream_of_the_pit",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPIT_OF_THE_CRONE = ITEMS.register("spit_of_the_crone",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SWEAT_OF_THE_OLD_ONE = ITEMS.register("sweat_of_the_old_one",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TEAR_OF_THE_MAIDEN = ITEMS.register("tear_of_the_maiden",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> THREAD_OF_THE_WYRD = ITEMS.register("thread_of_the_wyrd",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WAIL_OF_THE_HUNT = ITEMS.register("wail_of_the_hunt",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WHISTLE_OF_THE_WIND = ITEMS.register("whistle_of_the_wind",
            () -> new Item(new Item.Properties()));


    public static final RegistryObject<Item> BLACK_RITUAL_CHALK = ITEMS.register("black_ritual_chalk",
            () -> new RitualChalkItem(ChalkType.BLACK, new Item.Properties().durability(64)));

    public static final RegistryObject<Item> ENDER_RITUAL_CHALK = ITEMS.register("ender_ritual_chalk",
            () -> new RitualChalkItem(ChalkType.ENDER, new Item.Properties().durability(64)));

    public static final RegistryObject<Item> GOLDEN_RITUAL_CHALK = ITEMS.register("golden_ritual_chalk",
            () -> new RitualChalkItem(ChalkType.GOLD, new Item.Properties().durability(64)));

    public static final RegistryObject<Item> INFERNAL_RITUAL_CHALK = ITEMS.register("infernal_ritual_chalk",
            () -> new RitualChalkItem(ChalkType.INFERNAL, new Item.Properties().durability(64)));

    public static final RegistryObject<Item> WHITE_RITUAL_CHALK = ITEMS.register("white_ritual_chalk",
            () -> new RitualChalkItem(ChalkType.WHITE, new Item.Properties().durability(64)));

    public static final RegistryObject<Item> MANDRAKE_SPAWN_EGG = ITEMS.register("mandrake_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MANDRAKE, 0x40312E, 0x312127,
                    new Item.Properties()));

    public static final RegistryObject<Item> DEMON_SPAWN_EGG = ITEMS.register("demon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DEMON, 0x40312E, 0x312127,
                    new Item.Properties()));

    public static final RegistryObject<Item> SHADOW_MAN_SPAWN_EGG = ITEMS.register("shadow_man_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SHADOW_MAN, 0x0A0A0D, 0x6E1D24,
                    new Item.Properties()));

    public static final RegistryObject<Item> IMP_SPAWN_EGG = ITEMS.register("imp_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.IMP, 0x40312E, 0x312127,
                    new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
