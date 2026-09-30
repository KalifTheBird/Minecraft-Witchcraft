package net.toxicteddie.witchcraft.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.command.PathCommands;
import net.toxicteddie.witchcraft.command.MagicCommands;
import net.toxicteddie.witchcraft.command.ElementPowerCommands;
import net.toxicteddie.witchcraft.command.KnowledgeCommands;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeRegistry;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.MagicState;
import net.toxicteddie.witchcraft.effect.MagicalActEffects;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.power.ElementPowerConstants;
import net.toxicteddie.witchcraft.power.PlayerElementPowerManager;
import net.toxicteddie.witchcraft.power.telekinesis.TelekinesisManager;
import net.toxicteddie.witchcraft.power.fire.FirePowerManager;
import net.toxicteddie.witchcraft.wand.AltarDrawManager;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SyncConsecratedAreaPacket;
import net.toxicteddie.witchcraft.ritual.CalledCornersSavedData;
import net.toxicteddie.witchcraft.entity.ModEntities;
import net.toxicteddie.witchcraft.entity.custom.DemonEntity;
import net.toxicteddie.witchcraft.entity.custom.ImpEntity;
import net.toxicteddie.witchcraft.entity.custom.MandrakeEntity;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.custom.WitchesCauldronBlock;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.block.entity.ChalkGlyphBlockEntity;
import net.minecraft.core.BlockPos;

public final class ModEvents {

    private ModEvents() {
    }

    @Mod.EventBusSubscriber(
            modid = Witchcraft.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static final class ModBusEvents {

        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void registerAttributes(
                EntityAttributeCreationEvent event
        ) {
            event.put(
                    ModEntities.MANDRAKE.get(),
                    MandrakeEntity.createAttributes().build()
            );

            event.put(
                    ModEntities.DEMON.get(),
                    DemonEntity.createAttributes().build()
            );

            event.put(
                    ModEntities.SHADOW_MAN.get(),
                    net.toxicteddie.witchcraft.entity.custom.ShadowManEntity.createAttributes().build()
            );

            event.put(
                    ModEntities.IMP.get(),
                    ImpEntity.createAttributes().build()
            );
        }
    }

    @Mod.EventBusSubscriber(
            modid = Witchcraft.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static final class ForgeBusEvents {

        private ForgeBusEvents() {
        }

        @SubscribeEvent
        public static void registerCommands(
                RegisterCommandsEvent event
        ) {
            PathCommands.register(event.getDispatcher());
            MagicCommands.register(event.getDispatcher());
            ElementPowerCommands.register(event.getDispatcher());
            KnowledgeCommands.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void addReloadListeners(AddReloadListenerEvent event) {
            event.addListener(BookKnowledgeRegistry.INSTANCE);
        }


        @SubscribeEvent
        public static void onWoodsFavorBellRung(PlayerInteractEvent.RightClickBlock event) {
            if (!(event.getLevel() instanceof ServerLevel server)
                    || !(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
                return;
            }
            if (!server.getBlockState(event.getPos()).is(Blocks.BELL)) return;
            if (!PlayerPathManager.isWitch(player)) return;

            ChalkGlyphBlockEntity nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            BlockPos bell = event.getPos();

            BlockPos min = bell.offset(-10, -3, -10);
            BlockPos max = bell.offset(10, 3, 10);
            for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
                if (!(server.getBlockEntity(cursor) instanceof ChalkGlyphBlockEntity glyph)) continue;
                if (!glyph.isHeartGlyph()) continue;

                double distance = cursor.distSqr(bell);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = glyph;
                }
            }

            if (nearest != null) {

                nearest.tryActivateWoodsFavor(player);
            }
        }

        @SubscribeEvent
        public static void onCornerCandleInteraction(PlayerInteractEvent.RightClickBlock event) {
            if (!(event.getLevel() instanceof ServerLevel server)
                    || !(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
            BlockState state = server.getBlockState(event.getPos());
            if (!(state.getBlock() instanceof net.minecraft.world.level.block.CandleBlock)) return;

            BlockPos clicked = event.getPos();
            BlockPos min = clicked.offset(-9, -1, -9);
            BlockPos max = clicked.offset(9, 1, 9);
            for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
                if (!(server.getBlockEntity(cursor) instanceof ChalkGlyphBlockEntity glyph)) continue;
                if (glyph.handleCornerCandleInteraction(player, clicked, event.getHand())) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onDarkUnctionCauldronConversion(PlayerInteractEvent.RightClickBlock event) {
            ItemStack heldStack = event.getItemStack();
            if (!heldStack.is(ModItems.DARK_UNCTION.get())) return;

            BlockState oldState = event.getLevel().getBlockState(event.getPos());
            boolean emptyCauldron = oldState.is(Blocks.CAULDRON);
            boolean waterCauldron = oldState.is(Blocks.WATER_CAULDRON);
            if (!emptyCauldron && !waterCauldron) return;

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));

            if (!PlayerPathManager.isWitch(event.getEntity())) {
                if (!event.getLevel().isClientSide) {
                    event.getEntity().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    "message.witchcraft.cauldron.transformation_witches_only"
                            ),
                            true
                    );
                    event.getLevel().playSound(
                            null, event.getPos(), SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS, 0.45F, 0.65F
                    );
                }
                return;
            }

            if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

            int waterLevel = waterCauldron
                    ? oldState.getValue(LayeredCauldronBlock.LEVEL)
                    : 0;

            BlockState transformedState = ModBlocks.WITCHES_CAULDRON.get()
                    .defaultBlockState()
                    .setValue(WitchesCauldronBlock.LEVEL, waterLevel);
            serverLevel.setBlock(event.getPos(), transformedState, 3);

            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                MagicalActEffects.impact(serverPlayer, event.getPos());
            }

            if (!event.getEntity().getAbilities().instabuild) {
                heldStack.shrink(1);
                ItemStack emptyBottle = new ItemStack(ModItems.TINCTURE_BOTTLE.get());
                if (!event.getEntity().getInventory().add(emptyBottle)) {
                    event.getEntity().drop(emptyBottle, false);
                }
            }

            double x = event.getPos().getX() + 0.5D;
            double y = event.getPos().getY() + 0.65D;
            double z = event.getPos().getZ() + 0.5D;


            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    x, y + 0.20D, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D
            );
            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    x, y + 0.18D, z,
                    7, 0.24D, 0.13D, 0.24D, 0.025D
            );
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y + 0.22D, z,
                    15, 0.30D, 0.24D, 0.30D, 0.035D
            );
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x, y + 0.26D, z,
                    9, 0.28D, 0.18D, 0.28D, 0.055D
            );


            serverLevel.playSound(
                    null, event.getPos(), SoundEvents.SNOW_BREAK,
                    SoundSource.BLOCKS, 0.82F, 0.72F
            );
            serverLevel.playSound(
                    null, event.getPos(), SoundEvents.AMETHYST_CLUSTER_HIT,
                    SoundSource.BLOCKS, 0.48F, 0.78F
            );
            serverLevel.playSound(
                    null, event.getPos(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.30F, 1.18F
            );
        }

        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            PlayerMagicManager.sync(event.getEntity());
            PlayerElementPowerManager.sync(event.getEntity());
            syncConsecratedArea(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            PlayerMagicManager.sync(event.getEntity());
            PlayerElementPowerManager.sync(event.getEntity());
            syncConsecratedArea(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            PlayerMagicManager.sync(event.getEntity());
            PlayerElementPowerManager.sync(event.getEntity());
            syncConsecratedArea(event.getEntity());
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {


            if (event.getEntity() instanceof Sheep sheep
                    && sheep.isBaby()
                    && sheep.level() instanceof ServerLevel server) {

                if (event.getSource().getEntity()
                        instanceof ServerPlayer caster
                        && PlayerPathManager.isWitch(caster)) {

                Vec3 sacrificePosition =
                        sheep.position();

                BlockPos sacrificeBlock =
                        sheep.blockPosition();


                BlockPos min =
                        sacrificeBlock.offset(
                                -9,
                                -3,
                                -9
                        );

                BlockPos max =
                        sacrificeBlock.offset(
                                9,
                                3,
                                9
                        );

                java.util.List<ChalkGlyphBlockEntity> candidates =
                        new java.util.ArrayList<>();

                for (BlockPos cursor :
                        BlockPos.betweenClosed(
                                min,
                                max
                        )) {

                    if (server.getBlockEntity(cursor)
                            instanceof ChalkGlyphBlockEntity glyph
                            && glyph.isHeartGlyph()
                            && glyph.isInsideHollowingSacrificeBoundary(
                                    sacrificePosition
                            )) {

                        candidates.add(glyph);
                    }
                }

                candidates.sort(
                        java.util.Comparator.comparingDouble(
                                glyph -> glyph.getBlockPos()
                                        .distSqr(sacrificeBlock)
                        )
                );


                for (ChalkGlyphBlockEntity glyph :
                        candidates) {

                    if (glyph.tryActivateHollowing(
                            caster,
                            sacrificePosition
                    )) {
                        break;
                    }
                }

                if (candidates.isEmpty()) {
                    caster.displayClientMessage(
                            Component.literal(
                                    "Hollowing failed: no Heart Glyph with the lamb inside its outer candle boundary was found."
                            ),
                            false
                    );
                }
                }
            }


            if (!(event.getEntity()
                    instanceof ServerPlayer player)) {
                return;
            }

            if (!PlayerPathManager.isWitch(player)) {
                return;
            }

            if (PlayerMagicManager.getState(player)
                    == MagicState.SUPERCRITICAL) {
                PlayerMagicManager.beginSupercriticalDeathExhaust(
                        player
                );
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
            if (event.player.tickCount % MagicConstants.REGEN_INTERVAL_TICKS == 0) {
                PlayerMagicManager.regenerateNaturally(event.player);
            }
            if (event.player.tickCount % ElementPowerConstants.REGEN_INTERVAL_TICKS == 0) {
                PlayerElementPowerManager.regenerateNaturally(event.player);
            }
            if (event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                TelekinesisManager.tick(serverPlayer);
                FirePowerManager.tick(serverPlayer);
                AltarDrawManager.tick(serverPlayer);
            }
            if (event.player.tickCount % 10 == 0) {
                syncConsecratedArea(event.player);
            }
        }

        private static void syncConsecratedArea(net.minecraft.world.entity.player.Player player) {
            if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                    || !(serverPlayer.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            boolean inside = CalledCornersSavedData.get(serverLevel)
                    .isInside(serverLevel, serverPlayer.blockPosition());
            ModNetworking.sendToPlayer(new SyncConsecratedAreaPacket(inside), serverPlayer);
        }
    }
}
