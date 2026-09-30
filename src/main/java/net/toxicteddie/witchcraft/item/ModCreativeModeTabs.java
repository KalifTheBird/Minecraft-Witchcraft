package net.toxicteddie.witchcraft.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.ModBlocks;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Witchcraft.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WITCHCRAFT_TAB = CREATIVE_MODE_TABS.register("witchcraft_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PENTACLE.get()))
                    .title(Component.translatable("creativetab.witchcraft_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.BOOK_OF_SHADOWS.get());
                        pOutput.accept(ModItems.TORN_SHADOWY_PAGE.get());
                        pOutput.accept(ModItems.WHITE_RITUAL_CHALK.get());
                        pOutput.accept(ModItems.BLACK_RITUAL_CHALK.get());
                        pOutput.accept(ModItems.INFERNAL_RITUAL_CHALK.get());
                        pOutput.accept(ModItems.ENDER_RITUAL_CHALK.get());
                        pOutput.accept(ModItems.GOLDEN_RITUAL_CHALK.get());
                        pOutput.accept(ModItems.BELLADONNA_FLOWER.get());
                        pOutput.accept(ModItems.HEMLOCK_FLOWER.get());
                        pOutput.accept(ModItems.WOLFSBANE_FLOWER.get());
                        pOutput.accept(ModItems.SAGE_LEAF.get());
                        pOutput.accept(ModItems.MANDRAKE_ROOT.get());
                        pOutput.accept(ModItems.HEMLOCK_ROOT.get());
                        pOutput.accept(ModItems.ROWAN_BERRIES.get());
                        pOutput.accept(ModItems.ELDER_BERRIES.get());
                        pOutput.accept(ModItems.BELLADONNA_SEEDS.get());
                        pOutput.accept(ModItems.HEMLOCK_SEEDS.get());
                        pOutput.accept(ModItems.WOLFSBANE_SEEDS.get());
                        pOutput.accept(ModItems.SAGE_SEEDS.get());
                        pOutput.accept(ModItems.MANDRAKE_SEEDS.get());
                        pOutput.accept(ModItems.RAW_SALT.get());
                        pOutput.accept(ModItems.GLIMMERSTONE_SHARD.get());
                        pOutput.accept(ModItems.GLOOMSTONE_SHARD.get());
                        pOutput.accept(ModItems.ATHAME.get());
                        pOutput.accept(ModItems.WITCHES_HAT.get());
                        pOutput.accept(ModItems.SATCHEL.get());
                        pOutput.accept(ModItems.BONE_NEEDLE.get());
                        pOutput.accept(ModItems.BOTTLE_OF_BLOOD.get());
                        pOutput.accept(ModItems.PENTACLE.get());
                        pOutput.accept(ModItems.POPPET.get());
                        pOutput.accept(ModItems.WOOD_ASH.get());
                        pOutput.accept(ModItems.MANDRAKE_POWDER.get());
                        pOutput.accept(ModItems.SALT.get());
                        pOutput.accept(ModItems.GLIMMERSTONE_DUST.get());
                        pOutput.accept(ModItems.GLOOMSTONE_DUST.get());
                        pOutput.accept(ModItems.PURE_ESSENCE.get());
                        pOutput.accept(ModItems.DARK_ESSENCE.get());
                        pOutput.accept(ModItems.WILD_ESSENCE.get());
                        pOutput.accept(ModItems.HEART_OF_A_MORTAL.get());
                        pOutput.accept(ModItems.BLACKENED_HEART.get());
                        pOutput.accept(ModItems.HEART_OF_A_DEMON.get());
                        pOutput.accept(ModItems.WOOL_OF_BAT.get());
                        pOutput.accept(ModItems.TONGUE_OF_DOG.get());


                        pOutput.accept(ModItems.OAK_WAND_CORE.get());
                        pOutput.accept(ModItems.OAK_WAND.get());
                        pOutput.accept(ModItems.SPRUCE_WAND_CORE.get());
                        pOutput.accept(ModItems.SPRUCE_WAND.get());
                        pOutput.accept(ModItems.BIRCH_WAND_CORE.get());
                        pOutput.accept(ModItems.BIRCH_WAND.get());
                        pOutput.accept(ModItems.JUNGLE_WAND_CORE.get());
                        pOutput.accept(ModItems.JUNGLE_WAND.get());
                        pOutput.accept(ModItems.ACACIA_WAND_CORE.get());
                        pOutput.accept(ModItems.ACACIA_WAND.get());
                        pOutput.accept(ModItems.DARK_OAK_WAND_CORE.get());
                        pOutput.accept(ModItems.DARK_OAK_WAND.get());
                        pOutput.accept(ModItems.MANGROVE_WAND_CORE.get());
                        pOutput.accept(ModItems.MANGROVE_WAND.get());
                        pOutput.accept(ModItems.CHERRY_WAND_CORE.get());
                        pOutput.accept(ModItems.CHERRY_WAND.get());
                        pOutput.accept(ModItems.BAMBOO_WAND_CORE.get());
                        pOutput.accept(ModItems.BAMBOO_WAND.get());
                        pOutput.accept(ModItems.CRIMSON_WAND_CORE.get());
                        pOutput.accept(ModItems.CRIMSON_WAND.get());
                        pOutput.accept(ModItems.WARPED_WAND_CORE.get());
                        pOutput.accept(ModItems.WARPED_WAND.get());
                        pOutput.accept(ModItems.ROWAN_WAND_CORE.get());
                        pOutput.accept(ModItems.ROWAN_WAND.get());
                        pOutput.accept(ModItems.ELDER_WAND_CORE.get());
                        pOutput.accept(ModItems.ELDER_WAND.get());


                        pOutput.accept(ModItems.TINCTURE_BOTTLE.get());
                        pOutput.accept(ModItems.BLOOD_OF_THE_DAMNED.get());
                        pOutput.accept(ModItems.BLESSING_OF_THE_GODDESS.get());
                        pOutput.accept(ModItems.DARK_UNCTION.get());
                        pOutput.accept(ModItems.DAWNS_FIRST_LIGHT.get());
                        pOutput.accept(ModItems.DEW_OF_THE_ETERNAL.get());
                        pOutput.accept(ModItems.HOWL_OF_THE_WILD.get());
                        pOutput.accept(ModItems.MILK_OF_THE_MOTHER.get());
                        pOutput.accept(ModItems.MURMUR_OF_THE_SPIRITS.get());
                        pOutput.accept(ModItems.PALE_MOONLIGHT.get());
                        pOutput.accept(ModItems.PULSE_OF_MORTALITY.get());
                        pOutput.accept(ModItems.REMNANT_OF_THE_VEIL.get());
                        pOutput.accept(ModItems.SCREAM_OF_THE_PIT.get());
                        pOutput.accept(ModItems.SPIT_OF_THE_CRONE.get());
                        pOutput.accept(ModItems.SWEAT_OF_THE_OLD_ONE.get());
                        pOutput.accept(ModItems.TEAR_OF_THE_MAIDEN.get());
                        pOutput.accept(ModItems.THREAD_OF_THE_WYRD.get());
                        pOutput.accept(ModItems.WAIL_OF_THE_HUNT.get());
                        pOutput.accept(ModItems.WHISTLE_OF_THE_WIND.get());

                        pOutput.accept(ModBlocks.WITCHES_CAULDRON.get());
                        pOutput.accept(ModBlocks.WITCHES_OVEN.get());
                        pOutput.accept(ModBlocks.MORTAR_AND_PESTLE.get());

                        pOutput.accept(ModBlocks.OAK_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.SPRUCE_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.BIRCH_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.JUNGLE_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.ACACIA_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.DARK_OAK_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.MANGROVE_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.CHERRY_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.BAMBOO_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.CRIMSON_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.WARPED_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.ROWAN_WITCHES_ALTAR.get());
                        pOutput.accept(ModBlocks.ELDER_WITCHES_ALTAR.get());

                        pOutput.accept(ModBlocks.ROWAN_LOG.get());
                        pOutput.accept(ModBlocks.ROWAN_WOOD.get());
                        pOutput.accept(ModBlocks.STRIPPED_ROWAN_LOG.get());
                        pOutput.accept(ModBlocks.STRIPPED_ROWAN_WOOD.get());
                        pOutput.accept(ModBlocks.ROWAN_PLANKS.get());
                        pOutput.accept(ModBlocks.ROWAN_STAIRS.get());
                        pOutput.accept(ModBlocks.ROWAN_SLAB.get());
                        pOutput.accept(ModBlocks.ROWAN_FENCE.get());
                        pOutput.accept(ModBlocks.ROWAN_FENCE_GATE.get());
                        pOutput.accept(ModBlocks.ROWAN_PRESSURE_PLATE.get());
                        pOutput.accept(ModBlocks.ROWAN_BUTTON.get());
                        pOutput.accept(ModBlocks.ROWAN_LEAVES.get());
                        pOutput.accept(ModBlocks.ROWAN_BERRY_LEAVES.get());
                        pOutput.accept(ModBlocks.ROWAN_SAPLING.get());

                        pOutput.accept(ModBlocks.ELDER_LOG.get());
                        pOutput.accept(ModBlocks.ELDER_WOOD.get());
                        pOutput.accept(ModBlocks.STRIPPED_ELDER_LOG.get());
                        pOutput.accept(ModBlocks.STRIPPED_ELDER_WOOD.get());
                        pOutput.accept(ModBlocks.ELDER_PLANKS.get());
                        pOutput.accept(ModBlocks.ELDER_STAIRS.get());
                        pOutput.accept(ModBlocks.ELDER_SLAB.get());
                        pOutput.accept(ModBlocks.ELDER_FENCE.get());
                        pOutput.accept(ModBlocks.ELDER_FENCE_GATE.get());
                        pOutput.accept(ModBlocks.ELDER_PRESSURE_PLATE.get());
                        pOutput.accept(ModBlocks.ELDER_BUTTON.get());
                        pOutput.accept(ModBlocks.ELDER_LEAVES.get());
                        pOutput.accept(ModBlocks.ELDER_BERRY_LEAVES.get());
                        pOutput.accept(ModBlocks.ELDER_SAPLING.get());

                        pOutput.accept(ModItems.MANDRAKE_SPAWN_EGG.get());
                        pOutput.accept(ModItems.DEMON_SPAWN_EGG.get());
                        pOutput.accept(ModItems.IMP_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SHADOW_MAN_SPAWN_EGG.get());

                        pOutput.accept(ModBlocks.GLIMMERSTONE_CLUSTER.get());
                        pOutput.accept(ModBlocks.GLOOMSTONE_CLUSTER.get());


                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
