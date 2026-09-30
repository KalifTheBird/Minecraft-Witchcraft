package net.toxicteddie.witchcraft;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.client.renderer.MortarAndPestleRenderer;
import net.toxicteddie.witchcraft.block.client.renderer.BookOfShadowsRenderer;
import net.toxicteddie.witchcraft.block.client.renderer.ChalkGlyphRenderer;
import net.toxicteddie.witchcraft.block.client.renderer.WitchesAltarClothRenderer;
import net.toxicteddie.witchcraft.block.custom.ChalkGlyphBlock;
import net.toxicteddie.witchcraft.block.entity.ModBlockEntities;
import net.toxicteddie.witchcraft.block.client.renderer.WitchesCauldronRenderer;
import net.toxicteddie.witchcraft.entity.ModEntities;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.client.model.DemonModel;
import net.toxicteddie.witchcraft.entity.client.model.ImpModel;
import net.toxicteddie.witchcraft.entity.client.model.MandrakeModel;
import net.toxicteddie.witchcraft.entity.client.model.ShadowManModel;
import net.toxicteddie.witchcraft.entity.client.model.PestleModel;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import net.toxicteddie.witchcraft.entity.client.renderer.DemonRenderer;
import net.toxicteddie.witchcraft.entity.client.renderer.BookOfShadowsFollowingRenderer;
import net.toxicteddie.witchcraft.entity.client.renderer.ImpRenderer;
import net.toxicteddie.witchcraft.entity.client.renderer.MandrakeRenderer;
import net.toxicteddie.witchcraft.entity.client.renderer.ShadowManRenderer;
import net.toxicteddie.witchcraft.item.ModCreativeModeTabs;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.item.client.model.WitchesHatModel;
import net.toxicteddie.witchcraft.item.client.model.SatchelModel;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.menu.ModMenuTypes;
import net.toxicteddie.witchcraft.recipe.ModRecipes;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.particle.ModParticles;
import net.toxicteddie.witchcraft.particle.client.RitualFlameParticle;
import net.toxicteddie.witchcraft.client.hud.MagicHudOverlay;
import net.toxicteddie.witchcraft.client.wand.AltarDrawClientState;
import net.toxicteddie.witchcraft.screen.WitchesAltarScreen;
import net.toxicteddie.witchcraft.screen.WitchesOvenScreen;
import net.toxicteddie.witchcraft.sound.ModSounds;
import org.slf4j.Logger;


@Mod(Witchcraft.MOD_ID)
public class Witchcraft
{

    public static final String MOD_ID = "witchcraft";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Witchcraft(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModEntities.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);


        modEventBus.addListener(this::commonSetup);


        MinecraftForge.EVENT_BUS.register(this);


        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModNetworking::register);
    }


    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ATHAME);
            event.accept(ModItems.WITCHES_HAT);
            event.accept(ModItems.SATCHEL);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BELLADONNA_FLOWER);
        }
        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.BELLADONNA_SEEDS);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BLACKENED_HEART);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BONE_NEEDLE);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BLACK_RITUAL_CHALK);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BOOK_OF_SHADOWS);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.DARK_ESSENCE);
        }
        if(event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.ELDER_BERRIES);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.ENDER_RITUAL_CHALK);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GLIMMERSTONE_DUST);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GLIMMERSTONE_SHARD);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GLOOMSTONE_DUST);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GLOOMSTONE_SHARD);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.GOLDEN_RITUAL_CHALK);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.HEART_OF_A_DEMON);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.HEART_OF_A_MORTAL);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.HEMLOCK_FLOWER);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.HEMLOCK_ROOT);
        }
        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.HEMLOCK_SEEDS);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.INFERNAL_RITUAL_CHALK);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.MANDRAKE_POWDER);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.MANDRAKE_ROOT);
        }
        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.MANDRAKE_SEEDS);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.PENTACLE);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.POPPET);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.PURE_ESSENCE);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RAW_SALT);
        }
        if(event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.ROWAN_BERRIES);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SAGE_LEAF);
        }
        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.SAGE_SEEDS);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SALT);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TONGUE_OF_DOG);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TORN_SHADOWY_PAGE);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.WHITE_RITUAL_CHALK);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WILD_ESSENCE);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WOLFSBANE_FLOWER);
        }
        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.WOLFSBANE_SEEDS);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WOOD_ASH);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WOOL_OF_BAT);
        }
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }


    @Mod.EventBusSubscriber(
            modid = MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    ModParticles.RITUAL_FLAME.get(),
                    RitualFlameParticle.Provider::new
            );
        }


        @SubscribeEvent
        public static void registerEntityRenderers(
                EntityRenderersEvent.RegisterRenderers event
        ) {
            event.registerEntityRenderer(
                    ModEntities.BOOK_OF_SHADOWS_FOLLOWING.get(),
                    BookOfShadowsFollowingRenderer::new
            );

            event.registerEntityRenderer(
                    ModEntities.MANDRAKE.get(),
                    MandrakeRenderer::new
            );

            event.registerEntityRenderer(
                    ModEntities.DEMON.get(),
                    DemonRenderer::new
            );

            event.registerEntityRenderer(
                    ModEntities.SHADOW_MAN.get(),
                    ShadowManRenderer::new
            );

            event.registerEntityRenderer(
                    ModEntities.IMP.get(),
                    ImpRenderer::new
            );
        }


        @SubscribeEvent
        public static void registerLayerDefinitions(
                EntityRenderersEvent.RegisterLayerDefinitions event
        ) {
            event.registerLayerDefinition(
                    ModModelLayers.BOOK_OF_SHADOWS_LAYER,
                    BookOfShadowsModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.BOOK_OF_SHADOWS_PAPER_LAYER,
                    BookOfShadowsModel::createPaperLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.BOOK_OF_SHADOWS_METAL_LAYER,
                    BookOfShadowsModel::createMetalLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.MANDRAKE_LAYER,
                    MandrakeModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.DEMON_LAYER,
                    DemonModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.SHADOW_MAN_LAYER,
                    ShadowManModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.IMP_LAYER,
                    ImpModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.PESTLE_LAYER,
                    PestleModel::createBodyLayer
            );

            event.registerLayerDefinition(
                    ModModelLayers.WITCHES_HAT_LAYER,
                    WitchesHatModel::createBodyLayer
            );
            event.registerLayerDefinition(
                    ModModelLayers.SATCHEL_LAYER,
                    SatchelModel::createBodyLayer
            );
        }


        @SubscribeEvent
        public static void registerAdditionalModels(
                ModelEvent.RegisterAdditional event
        ) {
            event.register(
                    new ResourceLocation(
                            MOD_ID,
                            "block/witches_altar_cloth"
                    )
            );
        }


        @SubscribeEvent
        public static void onClientSetup(
                FMLClientSetupEvent event
        ) {
            MenuScreens.register(
                    ModMenuTypes.WITCHES_OVEN_MENU.get(),
                    WitchesOvenScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.WITCHES_ALTAR_MENU.get(),
                    WitchesAltarScreen::new
            );

            event.enqueueWork(() -> {
                registerBlockEntityRenderers();
                registerBlockRenderLayers();
                registerWandCastingProperties();
            });

        }


        private static void registerWandCastingProperties() {
            ResourceLocation casting =
                    new ResourceLocation(MOD_ID, "casting");

            net.minecraft.world.item.Item[] wands = {
                    ModItems.OAK_WAND.get(),
                    ModItems.SPRUCE_WAND.get(),
                    ModItems.BIRCH_WAND.get(),
                    ModItems.JUNGLE_WAND.get(),
                    ModItems.ACACIA_WAND.get(),
                    ModItems.DARK_OAK_WAND.get(),
                    ModItems.MANGROVE_WAND.get(),
                    ModItems.CHERRY_WAND.get(),
                    ModItems.BAMBOO_WAND.get(),
                    ModItems.CRIMSON_WAND.get(),
                    ModItems.WARPED_WAND.get(),
                    ModItems.ROWAN_WAND.get(),
                    ModItems.ELDER_WAND.get()
            };

            for (net.minecraft.world.item.Item wand : wands) {
                ItemProperties.register(
                        wand,
                        casting,
                        (stack, level, entity, seed) -> {
                            Minecraft minecraft = Minecraft.getInstance();

                            if (!AltarDrawClientState.active()
                                    || minecraft.player == null
                                    || entity != minecraft.player
                                    || !(stack.getItem() instanceof WandItem)) {
                                return 0.0F;
                            }

                            boolean held =
                                    minecraft.player.getMainHandItem() == stack
                                    || minecraft.player.getOffhandItem() == stack;

                            return held ? 1.0F : 0.0F;
                        }
                );
            }
        }

        private static void registerBlockEntityRenderers() {
            BlockEntityRenderers.register(
                    ModBlockEntities.BOOK_OF_SHADOWS_BE.get(),
                    BookOfShadowsRenderer::new
            );

            BlockEntityRenderers.register(
                    ModBlockEntities.CHALK_GLYPH_BE.get(),
                    ChalkGlyphRenderer::new
            );

            BlockEntityRenderers.register(
                    ModBlockEntities.WITCHES_CAULDRON_BE.get(),
                    WitchesCauldronRenderer::new
            );

            BlockEntityRenderers.register(
                    ModBlockEntities.MORTAR_AND_PESTLE_BE.get(),
                    MortarAndPestleRenderer::new
            );

            BlockEntityRenderers.register(
                    ModBlockEntities.WITCHES_ALTAR_BE.get(),
                    WitchesAltarClothRenderer::new
            );
        }

        private static void registerBlockRenderLayers() {


            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.CHALK_GLYPH.get(),
                    RenderType.cutout()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.SALT_LINE.get(),
                    RenderType.cutout()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ROWAN_SAPLING.get(),
                    RenderType.cutout()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ELDER_SAPLING.get(),
                    RenderType.cutout()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.GLIMMERSTONE_CLUSTER.get(),
                    RenderType.cutout()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.GLOOMSTONE_CLUSTER.get(),
                    RenderType.cutout()
            );


            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ROWAN_LEAVES.get(),
                    RenderType.cutoutMipped()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ROWAN_BERRY_LEAVES.get(),
                    RenderType.cutoutMipped()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ELDER_LEAVES.get(),
                    RenderType.cutoutMipped()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ELDER_BERRY_LEAVES.get(),
                    RenderType.cutoutMipped()
            );
        }


        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("magic_bar", MagicHudOverlay.HUD_MAGIC);
        }

        @SubscribeEvent
        public static void registerBlockColors(
                RegisterColorHandlersEvent.Block event
        ) {
            event.register(
                    (state, level, pos, tintIndex) -> {
                        if (state.hasProperty(ChalkGlyphBlock.TYPE)) {
                            return state
                                    .getValue(ChalkGlyphBlock.TYPE)
                                    .getTintColor();
                        }

                        return 0xFFFFFF;
                    },
                    ModBlocks.CHALK_GLYPH.get()
            );
        }
    }
}
