package net.toxicteddie.witchcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.custom.ChalkGlyph;
import net.toxicteddie.witchcraft.block.custom.ChalkGlyphBlock;
import net.toxicteddie.witchcraft.block.custom.ChalkType;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.effect.MagicalActEffects;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;
import net.toxicteddie.witchcraft.item.custom.WandWoodType;
import net.toxicteddie.witchcraft.magic.MagicDeedManager;
import net.toxicteddie.witchcraft.magic.MagicDeedResult;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.magic.MagicState;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.sound.ModSounds;
import net.toxicteddie.witchcraft.ritual.RitualPattern;
import net.toxicteddie.witchcraft.ritual.RitualPatterns;
import net.toxicteddie.witchcraft.ritual.CalledCornersSavedData;
import net.toxicteddie.witchcraft.ritual.CornerDirection;
import net.toxicteddie.witchcraft.ritual.RitualAugmentType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;


public class ChalkGlyphBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    public static final int FIRST_OFFERING_POOF_TICK = 105;
    public static final int SECOND_OFFERING_POOF_TICK = 145;
    public static final int[] TEMPERANCE_POOF_TICKS = {95, 125, 155, 185, 215};
    private static final int TEMPERANCE_CANDLE_RISE_START = 8;
    private static final int TEMPERANCE_CANDLE_RISE_END = 72;
    private static final int TEMPERANCE_EXTRACTION_START = 260;


    private static final int TEMPERANCE_BEAM_CONNECT_TICKS = 28;
    private static final int TEMPERANCE_EXTRACTION_END = 455;
    private static final int TEMPERANCE_CANDLE_FALL_START = 483;
    private static final int TEMPERANCE_CANDLE_FALL_END = 506;
    private static final int DEPARTURE_DURATION = 70;
    private static final int[] CORNER_OFFERING_POOF_TICKS = {105, 145};
    private static final int CORNER_READY_TICK = 160;
    private static final int CORNER_FLAME_GROW_TICKS = 12;
    private static final int CORNER_FLAME_SEQUENCE_DELAY = 10;
    private static final int CORNER_FLAME_SEQUENCE_SPACING = 14;
    private static final int CORNER_FINAL_BURN_TICKS = 38;
    private static final int CORNER_FLAME_FALL_TICKS = 23;
    private static final int CORNER_UNCALL_RESPONSE_DELAY = 14;
    private static final int CORNER_UNCALL_BURN_TICKS = 34;
    private static final int CORNER_UNCALL_EXTINGUISH_PAUSE_TICKS = 10;
    private static final int CORNER_UNCALL_EXTINGUISH_SPACING =
            CORNER_FLAME_FALL_TICKS + CORNER_UNCALL_EXTINGUISH_PAUSE_TICKS;

    private static final int[] EARTH_VEINS_OFFERING_POOF_TICKS = {72, 96, 120, 144};
    private static final int EARTH_VEINS_PICKAXE_ARRIVAL_START = 150;


    private static final int EARTH_VEINS_PICKAXE_GROW_END = 168;
    private static final int EARTH_VEINS_PICKAXE_HOVER_END = 198;
    private static final int EARTH_VEINS_SEARCH_START = EARTH_VEINS_PICKAXE_HOVER_END;
    private static final int EARTH_VEINS_EXTRACTION_START = 238;
    private static final int EARTH_VEINS_MAX_DURATION = 1800;
    private static final int EARTH_VEINS_FINISH_POOF_TICK = 24;
    private static final int EARTH_VEINS_FINISH_RETURN_TICK = 34;
    private static final int EARTH_VEINS_PICKAXE_SLOT = 4;

    private static final int[] TRANSMUTATION_POOF_TICKS = {70, 95, 120, 145, 170, 195};


    private static final int TRANSMUTATION_FIRST_IMPACT_TICK = 202;
    private static final int TRANSMUTATION_FLAME_GROW_START = 205;
    private static final int TRANSMUTATION_FLAME_FULL_TICK = 225;
    private static final int TRANSMUTATION_FLAME_SHRINK_START = 252;
    private static final int TRANSMUTATION_FLAME_NORMAL_TICK = 272;
    private static final int TRANSMUTATION_SECOND_IMPACT_TICK = 282;
    private static final int TRANSMUTATION_ESSENCE_START = 294;

    private static final int TRANSMUTATION_ESSENCE_TRAVEL_TICKS = 20;
    private static final int TRANSMUTATION_ESSENCE_HOVER_TICKS = 20;
    private static final int TRANSMUTATION_ESSENCE_DESCEND_TICKS = 12;
    private static final int TRANSMUTATION_ESSENCE_SPARKLE_TICKS = 18;
    private static final int TRANSMUTATION_ESSENCE_TOTAL_TICKS =
            TRANSMUTATION_ESSENCE_TRAVEL_TICKS
                    + TRANSMUTATION_ESSENCE_HOVER_TICKS
                    + TRANSMUTATION_ESSENCE_DESCEND_TICKS
                    + TRANSMUTATION_ESSENCE_SPARKLE_TICKS;

    private static final int TRANSMUTATION_FINAL_PAUSE_TICKS = 12;
    private static final int TRANSMUTATION_END_PAUSE_TICKS = 35;

    private static final int TRANSMUTATION_BASE_MAGIC_COST = 200;
    private static final int TRANSMUTATION_MAGIC_PER_CLUSTER = 15;

    private static final int[] WOODS_FAVOR_POOF_TICKS = {48, 76, 104, 132};
    private static final int WOODS_FAVOR_MANIFEST_TICK = 158;
    private static final int WOODS_FAVOR_END_TICK = 188;

    private static final int MAX_RITUAL_OFFERINGS = 8;
    private final NonNullList<ItemStack> offerings = NonNullList.withSize(MAX_RITUAL_OFFERINGS, ItemStack.EMPTY);
    private final double[] offeringStartX = new double[MAX_RITUAL_OFFERINGS];
    private final double[] offeringStartY = new double[MAX_RITUAL_OFFERINGS];
    private final double[] offeringStartZ = new double[MAX_RITUAL_OFFERINGS];

    private int ritualTicks;
    private boolean ritualActive;
    private boolean ritualSuccessful;
    private int departureTicks;
    private BlockPos whiteCandlePos;
    private BlockPos blackCandlePos;
    private final BlockPos[] grayCandlePositions = new BlockPos[4];
    private RitualKind ritualKind = RitualKind.NONE;
    private UUID temperanceTargetUuid;
    private UUID hollowingTargetUuid;
    private int hollowingStartingMagic;

    private static final int[] HOLLOWING_LINE_TICKS = {100, 180, 260, 330};
    private static final int HOLLOWING_CORRUPTION_TICK = 390;
    private static final int HOLLOWING_CANDLE_DYING_TICK = 405;
    private static final int HOLLOWING_FINAL_BREAK_TICK = 480;
    private UUID ritualCasterUuid;
    private int temperanceStartingMagic;
    private final BlockPos[] cornerCandlePositions = new BlockPos[4];
    private final int[] cornerFlameStartTicks = {-1, -1, -1, -1};
    private final int[] cornerFlameEndTicks = {-1, -1, -1, -1};
    private int cornerStep;
    private int cornerCompleteTick = -1;
    private int cornerFlameSequenceStartTick = -1;
    private boolean cornerAwaitingSequence;
    private UUID cornerCasterUuid;

    private EarthVeinTarget earthVeinTarget = EarthVeinTarget.NONE;
    private int earthVeinsExtracted;
    private int earthVeinsMaximum;
    private int earthVeinsOfferingCount;
    private int earthVeinsPulseInterval = 28;
    private int earthVeinsSearchRadius = 16;
    private int earthVeinsFinishTicks = -1;
    private final List<BlockPos> earthVeinCandidates = new ArrayList<>();

    private final List<BlockPos> transmutationClusters = new ArrayList<>();
    private String woodsFavorWoodId = "";


    private static final int EARTH_VEINS_CANDLE_MAGIC_COST = 35;
    private static final int EARTH_VEINS_POWER_AUGMENT_MAGIC_COST = 125;
    private static final int EARTH_VEINS_CRYSTAL_MAGIC_COST = 75;

    public ChalkGlyphBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHALK_GLYPH_BE.get(), pos, state);
    }


    public static void clientTick(Level level, BlockPos pos, BlockState state, ChalkGlyphBlockEntity be) {
        if (!be.ritualActive) return;

        if (be.ritualKind == RitualKind.CALL_CORNERS || be.ritualKind == RitualKind.UNCALL_CORNERS) {
            be.ritualTicks++;
            return;
        }
        int duration = be.ritualKind == RitualKind.HOLLOWING
                ? RitualPatterns.HOLLOWING.duration()
                : be.ritualKind == RitualKind.TEMPERANCE
                ? RitualPatterns.TEMPERANCE.duration()
                : be.ritualKind == RitualKind.EARTH_VEINS
                ? EARTH_VEINS_MAX_DURATION
                : be.ritualKind == RitualKind.TRANSMUTATION
                ? RitualPatterns.TRANSMUTATION.duration()
                : be.ritualKind == RitualKind.WOODS_FAVOR
                ? RitualPatterns.WOODS_FAVOR.duration()
                : RitualPatterns.NIGHT.duration();
        if (be.ritualTicks < duration) be.ritualTicks++;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChalkGlyphBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;


        if (!be.ritualActive) {
            if (be.departureTicks > 0) {
                be.tickDeparture(server);
            }
            return;
        }

        be.ritualTicks++;
        if (be.ritualKind == RitualKind.HOLLOWING) {
            be.tickHollowing(server);
        } else if (be.ritualKind == RitualKind.TEMPERANCE) {
            be.tickTemperance(server);
        } else if (be.ritualKind == RitualKind.EARTH_VEINS) {
            be.tickEarthVeins(server);
        } else if (be.ritualKind == RitualKind.TRANSMUTATION) {
            be.tickTransmutation(server);
        } else if (be.ritualKind == RitualKind.WOODS_FAVOR) {
            be.tickWoodsFavor(server);
        } else if (be.ritualKind == RitualKind.CALL_CORNERS || be.ritualKind == RitualKind.UNCALL_CORNERS) {
            be.tickCorners(server);
        } else {
            be.tickNight(server);
        }
    }


    private void tickCorners(ServerLevel server) {
        if (ritualTicks == CORNER_OFFERING_POOF_TICKS[0]) poofOffering(server, 0);
        if (ritualTicks == CORNER_OFFERING_POOF_TICKS[1]) poofOffering(server, 1);
        if (ritualTicks == CORNER_READY_TICK) {


            cornerAwaitingSequence = true;
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.42F, 0.86F);
            ServerPlayer caster = cornerCasterUuid == null
                    ? null : server.getServer().getPlayerList().getPlayer(cornerCasterUuid);
            if (caster != null) {
                caster.displayClientMessage(Component.literal(
                        ritualKind == RitualKind.CALL_CORNERS
                                ? "The offerings are accepted. Light East, South, West, then North."
                                : "The offerings are accepted. Extinguish North, West, South, then East."
                ), true);
            }
            sync();
        }

        if (ritualKind == RitualKind.CALL_CORNERS && cornerFlameSequenceStartTick >= 0) {
            for (int i = 0; i < 4; i++) {
                int startTick = cornerFlameSequenceStartTick + i * CORNER_FLAME_SEQUENCE_SPACING;
                if (ritualTicks == startTick) {
                    cornerFlameStartTicks[i] = ritualTicks;
                    cornerFlameEndTicks[i] = -1;
                    BlockPos candle = cornerCandlePositions[i];
                    if (candle != null) {
                        server.playSound(null, candle, SoundEvents.FIRECHARGE_USE,
                                SoundSource.BLOCKS, 0.82F, 0.78F + i * 0.035F);
                        server.sendParticles(ParticleTypes.EXPLOSION,
                                candle.getX() + 0.5D, candle.getY() + 0.43D, candle.getZ() + 0.5D,
                                1, 0, 0, 0, 0);
                    }
                    sync();
                }
            }

            int allFullTick = cornerFlameSequenceStartTick
                    + 3 * CORNER_FLAME_SEQUENCE_SPACING + CORNER_FLAME_GROW_TICKS;
            if (ritualTicks == allFullTick) {
                cornerCompleteTick = ritualTicks;
                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.BLOCKS, 0.58F, 0.82F);
                server.sendParticles(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.34D, worldPosition.getZ() + 0.5D,
                        18, 0.72D, 0.18D, 0.72D, 0.012D);
                sync();
            }
        }

        if (ritualKind == RitualKind.UNCALL_CORNERS && cornerFlameSequenceStartTick >= 0) {


            if (ritualTicks == cornerFlameSequenceStartTick) {
                for (int i = 0; i < 4; i++) {
                    cornerFlameStartTicks[i] = ritualTicks;
                    cornerFlameEndTicks[i] = -1;
                    BlockPos candle = cornerCandlePositions[i];
                    if (candle != null) {
                        server.playSound(null, candle, SoundEvents.FIRECHARGE_USE,
                                SoundSource.BLOCKS, 0.74F, 0.72F + i * 0.025F);
                        server.sendParticles(ParticleTypes.EXPLOSION,
                                candle.getX() + 0.5D, candle.getY() + 0.43D, candle.getZ() + 0.5D,
                                1, 0, 0, 0, 0);
                    }
                }
                sync();
            }

            int firstExtinguishTick = cornerFlameSequenceStartTick
                    + CORNER_FLAME_GROW_TICKS + CORNER_UNCALL_BURN_TICKS;

            int[] uncallIndices = {3, 2, 1, 0};
            for (int step = 0; step < uncallIndices.length; step++) {
                int extinguishTick = firstExtinguishTick + step * CORNER_UNCALL_EXTINGUISH_SPACING;
                int index = uncallIndices[step];
                if (ritualTicks == extinguishTick) {
                    cornerFlameEndTicks[index] = ritualTicks;
                    BlockPos candle = cornerCandlePositions[index];
                    if (candle != null) {
                        server.playSound(null, candle, SoundEvents.CANDLE_EXTINGUISH,
                                SoundSource.BLOCKS, 0.70F, 0.90F - step * 0.035F);
                        server.sendParticles(ParticleTypes.POOF,
                                candle.getX() + 0.5D, candle.getY() + 0.48D, candle.getZ() + 0.5D,
                                5, 0.08D, 0.06D, 0.08D, 0.015D);
                    }
                    sync();
                }
            }

            int finalDoneTick = firstExtinguishTick
                    + 3 * CORNER_UNCALL_EXTINGUISH_SPACING + CORNER_FLAME_FALL_TICKS;
            if (ritualTicks >= finalDoneTick) {
                finishCornerRite(server);
                return;
            }
        }

        if (ritualKind == RitualKind.CALL_CORNERS
                && cornerCompleteTick >= 0
                && ritualTicks >= cornerCompleteTick + CORNER_FINAL_BURN_TICKS + CORNER_FLAME_FALL_TICKS) {
            finishCornerRite(server);
        }
    }

    private void tickNight(ServerLevel server) {
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.18D;
        double z = worldPosition.getZ() + 0.5D;

        if (ritualTicks % 18 == 0) {
            double angle = ritualTicks * 0.075D;
            double radius = 0.8D + Math.sin(ritualTicks * 0.04D) * 0.18D;
            server.sendParticles(ParticleTypes.WHITE_ASH,
                    x + Math.cos(angle) * radius, y + 0.35D,
                    z + Math.sin(angle) * radius,
                    1, 0.03D, 0.025D, 0.03D, 0.001D);
        }
        if (ritualTicks == 32) {
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.32F, 0.72F);
        }
        if (ritualTicks == FIRST_OFFERING_POOF_TICK) poofOffering(server, 0);
        if (ritualTicks == SECOND_OFFERING_POOF_TICK) poofOffering(server, 1);

        if (ritualTicks >= RitualPatterns.NIGHT.duration()) {
            if (ritualSuccessful) finishNightRite(server);
            else finishFailedRite(server);
        }
    }

    private void tickEarthVeins(ServerLevel server) {
        for (int i = 0; i < EARTH_VEINS_OFFERING_POOF_TICKS.length; i++) {
            if (ritualTicks == EARTH_VEINS_OFFERING_POOF_TICKS[i]) poofOffering(server, i);
        }

        if (!ritualSuccessful) {
            if (ritualTicks >= EARTH_VEINS_SEARCH_START) finishFailedRite(server);
            return;
        }

        if (earthVeinsFinishTicks >= 0) {
            tickEarthVeinsFinish(server);
            return;
        }


        if (ritualTicks == EARTH_VEINS_PICKAXE_ARRIVAL_START) {
            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + 0.72D;
            double z = worldPosition.getZ() + 0.5D;


            server.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.POOF, x, y, z,
                    16, 0.10D, 0.08D, 0.10D, 0.018D);
            server.sendParticles(ParticleTypes.END_ROD, x, y, z,
                    6, 0.09D, 0.10D, 0.09D, 0.006D);
            server.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, 0.42F, 1.35F);
            server.playSound(null, worldPosition, SoundEvents.SNOW_BREAK,
                    SoundSource.BLOCKS, 0.58F, 0.82F);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.42F, 0.68F);
            sync();
        }

        if (ritualTicks == EARTH_VEINS_SEARCH_START) {
            rebuildEarthVeinCandidates(server);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.44F, 0.48F);
            server.sendParticles(ParticleTypes.POOF,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.18D, worldPosition.getZ() + 0.5D,
                    12, 0.55D, 0.05D, 0.55D, 0.02D);
            sync();
        }

        if (ritualTicks == EARTH_VEINS_EXTRACTION_START) {
            ServerPlayer caster = getRitualCaster(server);
            if (caster != null) MagicalActEffects.impact(caster, worldPosition);
        }

        if (ritualTicks >= EARTH_VEINS_EXTRACTION_START) {

            if ((ritualTicks - EARTH_VEINS_EXTRACTION_START) % 20 == 0) {
                server.playSound(null, worldPosition, SoundEvents.ROOTED_DIRT_BREAK,
                        SoundSource.BLOCKS, 0.20F, 0.56F + server.random.nextFloat() * 0.05F);
                server.sendParticles(ParticleTypes.POOF,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.08D,
                        worldPosition.getZ() + 0.5D,
                        8, 1.25D, 0.025D, 1.25D, 0.015D);
            }

            if ((ritualTicks - EARTH_VEINS_EXTRACTION_START) % Math.max(1, earthVeinsPulseInterval) == 0) {
                if (earthVeinsExtracted >= earthVeinsMaximum || !extractNextEarthVein(server)) {
                    finishEarthVeins(server);
                    return;
                }
            }
        }

        if (ritualTicks >= EARTH_VEINS_MAX_DURATION) finishEarthVeins(server);
    }

    private void rebuildEarthVeinCandidates(ServerLevel server) {
        earthVeinCandidates.clear();
        if (earthVeinTarget == EarthVeinTarget.NONE) return;
        int minY = server.getMinBuildHeight();
        int maxY = worldPosition.getY() - 1;
        int radius = earthVeinsSearchRadius;
        int radiusSqr = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSqr) continue;
                for (int y = maxY; y >= minY; y--) {
                    BlockPos pos = worldPosition.offset(x, y - worldPosition.getY(), z);
                    if (earthVeinTarget.matches(server.getBlockState(pos))) earthVeinCandidates.add(pos.immutable());
                }
            }
        }
        Collections.sort(earthVeinCandidates, Comparator.comparingDouble(pos -> pos.distSqr(worldPosition)));
    }

    private boolean extractNextEarthVein(ServerLevel server) {
        if (earthVeinCandidates.isEmpty()) rebuildEarthVeinCandidates(server);
        while (!earthVeinCandidates.isEmpty()) {
            BlockPos orePos = earthVeinCandidates.remove(0);
            BlockState oreState = server.getBlockState(orePos);
            if (!earthVeinTarget.matches(oreState)) continue;

            ItemStack pickaxe = offerings.get(EARTH_VEINS_PICKAXE_SLOT);
            if (pickaxe.isEmpty() || !(pickaxe.getItem() instanceof PickaxeItem)
                    || !pickaxe.isCorrectToolForDrops(oreState)) {
                finishEarthVeins(server);
                return false;
            }

            ServerPlayer caster = getRitualCaster(server);
            server.setBlock(orePos, earthVeinTarget.hostBlock(oreState), 3);
            spawnEarthVeinThread(server, orePos);

            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + 0.75D;
            double z = worldPosition.getZ() + 0.5D;


            ItemStack yielded = earthVeinTarget.extractedResult();
            if (!yielded.isEmpty()) {
                ItemEntity item = new ItemEntity(server, x, y, z, yielded);
                double angle = server.random.nextDouble() * Math.PI * 2.0D;
                double horizontalSpeed = 0.10D + server.random.nextDouble() * 0.08D;
                item.setDeltaMovement(Math.cos(angle) * horizontalSpeed,
                        0.18D + server.random.nextDouble() * 0.07D,
                        Math.sin(angle) * horizontalSpeed);
                item.setPickUpDelay(8);
                server.addFreshEntity(item);
            }

            earthVeinsExtracted++;
            boolean broke = pickaxe.hurt(1, server.random, caster);
            if (broke) {
                offerings.set(EARTH_VEINS_PICKAXE_SLOT, ItemStack.EMPTY);
                server.playSound(null, worldPosition, SoundEvents.ITEM_BREAK,
                        SoundSource.BLOCKS, 0.95F, 0.82F);
                server.sendParticles(ParticleTypes.POOF,
                        x, y + 0.55D, z, 18, 0.24D, 0.18D, 0.24D, 0.08D);
            } else {
                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.BLOCKS, 0.30F, 0.66F + server.random.nextFloat() * 0.08F);
                server.sendParticles(ParticleTypes.END_ROD, x, y, z,
                        4, 0.18D, 0.16D, 0.18D, 0.015D);
            }
            sync();
            if (broke || earthVeinsExtracted >= earthVeinsMaximum) finishEarthVeins(server);
            return true;
        }
        return false;
    }

    private void spawnEarthVeinThread(ServerLevel server, BlockPos orePos) {
        Vec3 start = Vec3.atCenterOf(orePos);
        Vec3 end = Vec3.atCenterOf(worldPosition).add(0.0D, 0.30D, 0.0D);
        Vec3 delta = end.subtract(start);
        int steps = Math.max(8, Math.min(28, Mth.ceil(delta.length() * 0.65D)));
        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;
            double eased = progress * progress * (3.0D - 2.0D * progress);
            Vec3 point = start.add(delta.scale(eased));
            if ((i & 1) == 0) {
                server.sendParticles(ParticleTypes.END_ROD,
                        point.x, point.y, point.z, 1, 0.012D, 0.012D, 0.012D, 0.0D);
            }
        }
    }

    private void finishEarthVeins(ServerLevel server) {
        if (!ritualActive || ritualKind != RitualKind.EARTH_VEINS || earthVeinsFinishTicks >= 0) return;
        earthVeinsFinishTicks = 0;
        earthVeinCandidates.clear();
        sync();
    }

    private void tickEarthVeinsFinish(ServerLevel server) {
        earthVeinsFinishTicks++;

        if (earthVeinsFinishTicks == EARTH_VEINS_FINISH_POOF_TICK) {
            double x = worldPosition.getX() + 0.5D;
            double y = worldPosition.getY() + 1.95D;
            double z = worldPosition.getZ() + 0.5D;
            server.sendParticles(ParticleTypes.EXPLOSION, x, y, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.POOF, x, y, z,
                    18, 0.12D, 0.12D, 0.12D, 0.025D);
            server.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, 0.48F, 1.28F);
            sync();
        }

        if (earthVeinsFinishTicks < EARTH_VEINS_FINISH_RETURN_TICK) return;

        ItemStack pickaxe = offerings.get(EARTH_VEINS_PICKAXE_SLOT);
        if (!pickaxe.isEmpty()) {
            ItemEntity returned = new ItemEntity(server,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.72D,
                    worldPosition.getZ() + 0.5D, pickaxe.copy());
            returned.setDeltaMovement(0.0D, 0.12D, 0.0D);
            returned.setPickUpDelay(10);
            server.addFreshEntity(returned);
            server.sendParticles(ParticleTypes.POOF,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.78D,
                    worldPosition.getZ() + 0.5D,
                    7, 0.06D, 0.05D, 0.06D, 0.012D);
        }

        clearOfferingSlots();
        ritualActive = false;
        ritualKind = RitualKind.NONE;
        departureTicks = DEPARTURE_DURATION;
        earthVeinsFinishTicks = -1;
        server.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE,
                SoundSource.BLOCKS, 0.45F, 0.66F);
        ServerPlayer caster = getRitualCaster(server);
        if (caster != null) {
            caster.displayClientMessage(Component.literal(
                    "The earth surrendered " + earthVeinsExtracted + " "
                            + earthVeinTarget.displayName + " vein"
                            + (earthVeinsExtracted == 1 ? "." : "s.")), true);
        }
        sync();
    }

    private void tickTemperance(ServerLevel server) {
        updateTemperanceFlameLights(server);


        if (ritualTicks >= 38 && ritualTicks < 475) {
            tickTemperanceFlameAtmosphere(server);
        }


        if (ritualTicks == 8) {
            server.playSound(null, worldPosition, SoundEvents.AMBIENT_CAVE.get(),
                    SoundSource.AMBIENT, 0.16F, 0.78F);
        }
        if (ritualTicks == 32) {
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.28F, 0.48F);
        }


        if (ritualTicks == 36) {
            server.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.BLOCKS, 0.55F, 0.92F);
        }
        if (ritualTicks == 38) {
            ServerPlayer caster = getRitualCaster(server);
            if (caster != null) MagicalActEffects.impact(caster, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, 0.88F, 0.76F);


            server.playSound(null, worldPosition, ModSounds.RITUAL_FLAME_LOOP.get(),
                    SoundSource.BLOCKS, 0.38F, 1.0F);
            spawnTemperanceIgnitionPoofs(server);
        }

        for (int i = 0; i < TEMPERANCE_POOF_TICKS.length; i++) {
            if (ritualTicks == TEMPERANCE_POOF_TICKS[i]) {
                poofOffering(server, i);
                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.20F, 0.58F + i * 0.06F);
            }
        }


        if (ritualTicks == TEMPERANCE_EXTRACTION_START) {
            server.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS, 0.30F, 0.50F);
        }


        int drainStartTick = TEMPERANCE_EXTRACTION_START + TEMPERANCE_BEAM_CONNECT_TICKS;
        if (ritualTicks >= TEMPERANCE_EXTRACTION_START && ritualTicks <= TEMPERANCE_EXTRACTION_END) {
            ServerPlayer target = getTemperanceTarget(server);
            if (target == null) {
                ritualSuccessful = false;
            } else if (ritualSuccessful && ritualTicks >= drainStartTick) {
                spawnTemperanceStream(server, target);

                if (ritualTicks % 4 == 0) {
                    float progress = Mth.clamp((ritualTicks - drainStartTick)
                            / (float) (TEMPERANCE_EXTRACTION_END - drainStartTick), 0.0F, 1.0F);
                    int stable = MagicConstants.NATURAL_LIMIT;
                    int next = Mth.floor(Mth.lerp(progress, temperanceStartingMagic, stable));
                    PlayerMagicManager.setMagic(target, Math.max(stable, next));
                }
            }
        }


        if (ritualTicks == TEMPERANCE_CANDLE_FALL_START) {
            server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.52F, 1.08F);
        }
        if (ritualTicks == TEMPERANCE_CANDLE_FALL_END) {
            server.playSound(null, worldPosition, SoundEvents.CANDLE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.78F, 0.94F);
        }

        if (ritualTicks >= RitualPatterns.TEMPERANCE.duration()) {
            if (ritualSuccessful) finishTemperanceRite(server);
            else finishFailedRite(server);
        }
    }

    private void spawnTemperanceStream(ServerLevel server, ServerPlayer target) {


        if (ritualTicks % 12 != 0) return;

        double progress = ((ritualTicks - TEMPERANCE_EXTRACTION_START) * 0.073D) % 1.0D;
        double sx = target.getX();
        double sy = target.getY() + target.getBbHeight() * 0.72D;
        double sz = target.getZ();
        double ex = worldPosition.getX() + 0.5D;
        double ey = worldPosition.getY() + 0.42D;
        double ez = worldPosition.getZ() + 0.5D;

        double envelope = Math.sin(Math.PI * progress);
        double dx = ex - sx;
        double dz = ez - sz;
        double horizontal = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double perpX = -dz / horizontal;
        double perpZ = dx / horizontal;
        double wave = Math.sin(progress * Math.PI * 5.0D - ritualTicks * 0.145D)
                * 0.17D * envelope;
        double arc = 0.62D * 4.0D * progress * (1.0D - progress);

        server.sendParticles(ParticleTypes.END_ROD,
                Mth.lerp(progress, sx, ex) + perpX * wave,
                Mth.lerp(progress, sy, ey) + arc,
                Mth.lerp(progress, sz, ez) + perpZ * wave,
                1, 0.004D, 0.004D, 0.004D, 0.0D);
    }


    private void tickTemperanceFlameAtmosphere(ServerLevel server) {
        boolean extractionActive = ritualTicks >= TEMPERANCE_EXTRACTION_START
                && ritualTicks <= TEMPERANCE_EXTRACTION_END;

        float rise = getTemperanceCandleRiseProgress(0.0F);
        float renderedHeight = 1.50F * rise;

        for (int index = 0; index < grayCandlePositions.length; index++) {
            BlockPos candle = grayCandlePositions[index];
            if (candle == null) continue;

            long phase = ritualTicks + index * 3L;
            double centerX = candle.getX() + 0.5D;
            double wickY = candle.getY() + 0.40D;
            double centerZ = candle.getZ() + 0.5D;


            if (phase % 4L == 0L && rise > 0.10F) {
                double angle = phase * 0.71D + index * 1.83D;
                double radius = 0.025D + server.random.nextDouble() * 0.075D;
                double yOffset = 0.08D + server.random.nextDouble()
                        * Math.max(0.10D, renderedHeight * 0.88D);
                server.sendParticles(ParticleTypes.FLAME,
                        centerX + Math.cos(angle) * radius,
                        wickY + yOffset,
                        centerZ + Math.sin(angle) * radius,
                        1, 0.006D, 0.014D, 0.006D,
                        extractionActive ? 0.012D : 0.008D);
            }


            if (phase % 22L == 0L && rise > 0.72F
                    && ritualTicks < TEMPERANCE_CANDLE_FALL_START) {
                double angle = phase * 0.31D + index * 1.47D;
                double radius = server.random.nextDouble() * 0.045D;


                double driftX = Math.cos(angle) * 0.006D;
                double driftZ = Math.sin(angle) * 0.006D;
                double riseSpeed = 0.075D + server.random.nextDouble() * 0.025D;
                server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        centerX + Math.cos(angle) * radius,
                        wickY + renderedHeight - 0.06D,
                        centerZ + Math.sin(angle) * radius,
                        0, driftX, riseSpeed, driftZ, 1.0D);
            }
        }
    }


    private void spawnTemperanceIgnitionPoofs(ServerLevel server) {
        for (BlockPos candle : grayCandlePositions) {
            if (candle == null) continue;
            server.sendParticles(ParticleTypes.EXPLOSION,
                    candle.getX() + 0.5D,
                    candle.getY() + 0.43D,
                    candle.getZ() + 0.5D,
                    1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
    }

    private void tickDeparture(ServerLevel server) {
        departureTicks--;


        if (departureTicks % 9 == 0) {
            int elapsed = DEPARTURE_DURATION - departureTicks;
            double angle = elapsed * 0.34D + worldPosition.asLong() * 0.0001D;
            double radius = 0.10D + elapsed * 0.0035D;
            double px = worldPosition.getX() + 0.5D + Math.cos(angle) * radius;
            double py = worldPosition.getY() + 0.55D + elapsed * 0.034D;
            double pz = worldPosition.getZ() + 0.5D + Math.sin(angle) * radius;

            server.sendParticles(ParticleTypes.END_ROD, px, py, pz,
                    1, 0.01D, 0.018D, 0.01D, 0.0D);
        }

        if (departureTicks <= 0) {
            departureTicks = 0;
            sync();
        }
    }

    public boolean isHeartGlyph() {
        BlockState state = getBlockState();
        return state.is(ModBlocks.CHALK_GLYPH.get())
                && state.getValue(ChalkGlyphBlock.TYPE) == ChalkType.GOLD
                && state.getValue(ChalkGlyphBlock.GLYPH) == ChalkGlyph.HEART;
    }


    public boolean tryActivate(ServerPlayer player) {
        if (!isHeartGlyph() || ritualActive || !PlayerPathManager.isWitch(player)) return false;
        if (!(level instanceof ServerLevel server)) return false;

        RitualPattern.Match uncallCorners = RitualPatterns.UNCALL_CORNERS.match(server, worldPosition);
        if (uncallCorners != null && CalledCornersSavedData.get(server).hasCenterNear(server, worldPosition, 4)) {
            return activateCornerRite(server, player, uncallCorners, false);
        }
        RitualPattern.Match callCorners = RitualPatterns.CALL_CORNERS.match(server, worldPosition);
        if (callCorners != null && !CalledCornersSavedData.get(server).hasCenterNear(server, worldPosition, 4)) {
            return activateCornerRite(server, player, callCorners, true);
        }

        RitualPattern.Match transmutation = RitualPatterns.TRANSMUTATION.match(server, worldPosition);
        if (transmutation != null) {
            List<BlockPos> amethystClusters = findPlacedTransmutationClusters(server);
            if (!amethystClusters.isEmpty()) {
                return activateTransmutation(server, player, transmutation, amethystClusters);
            }
        }

        RitualPattern.Match earthVeins = RitualPatterns.EARTH_VEINS.match(server, worldPosition);
        if (earthVeins != null) return activateEarthVeins(server, player, earthVeins);

        TemperanceSetup temperance = findTemperanceSetup(server);
        if (temperance != null) return activateTemperance(server, player, temperance);

        CandlePair candles = findValidCandles();
        if (candles == null || !hasSmallWhiteCircle()) {
            invalidPatternFeedback(server);
            return false;
        }

        OfferingPair found = findThrownOfferings(server);
        if (found == null) {
            missingOfferingFeedback(server);
            return false;
        }

        clearOfferingSlots();
        captureAndConsumeOne(found.daisy(), 0);
        captureAndConsumeOne(found.moonlight(), 1);
        whiteCandlePos = candles.white().immutable();
        blackCandlePos = candles.black().immutable();
        ritualKind = RitualKind.NIGHT;
        ritualCasterUuid = player.getUUID();

        MagicDeedResult payment = MagicDeedManager.performRitual(player, worldPosition, RitualPatterns.NIGHT.magicCost());
        ritualSuccessful = payment.successful();
        ritualTicks = 0;
        ritualActive = true;
        sync();

        MagicalActEffects.impact(player, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 0.42F, 0.68F);
        server.playSound(null, worldPosition, SoundEvents.SNOW_PLACE,
                SoundSource.BLOCKS, 0.28F, 0.82F);
        server.sendParticles(ParticleTypes.WHITE_ASH,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.24D, worldPosition.getZ() + 0.5D,
                5, 0.32D, 0.04D, 0.32D, 0.002D);
        return true;
    }


    public boolean isInsideHollowingSacrificeBoundary(Vec3 sacrificePosition) {
        if (!isHeartGlyph()) return false;

        double centerX = worldPosition.getX() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;
        double dx = Math.abs(sacrificePosition.x - centerX);
        double dz = Math.abs(sacrificePosition.z - centerZ);


        return dx < 8.0D
                && dz < 8.0D
                && dx + dz < 11.0D;
    }


    private String validateHollowingStructure(ServerLevel server) {

        int[][][] circles = {
                RitualPatterns.SMALL_CIRCLE,
                RitualPatterns.MEDIUM_CIRCLE,
                RitualPatterns.LARGE_CIRCLE
        };
        String[] circleNames = {
                "Small Black Circle",
                "Medium Black Circle",
                "Large Black Circle"
        };

        for (int circleIndex = 0; circleIndex < circles.length; circleIndex++) {
            for (int[] offset : circles[circleIndex]) {
                BlockState state = server.getBlockState(
                        worldPosition.offset(offset[0], 0, offset[1])
                );

                if (!state.is(ModBlocks.CHALK_GLYPH.get())
                        || state.getValue(ChalkGlyphBlock.TYPE) != ChalkType.BLACK) {
                    return circleNames[circleIndex]
                            + " is incomplete at offset "
                            + offset[0] + ", " + offset[1] + ".";
                }
            }
        }


        for (int i = 0; i < RitualPatterns.HOLLOWING_CANDLE_RING.length; i++) {
            int[] offset = RitualPatterns.HOLLOWING_CANDLE_RING[i];
            BlockState state = server.getBlockState(
                    worldPosition.offset(offset[0], 0, offset[1])
            );

            if (!state.is(Blocks.BLACK_CANDLE)) {
                return "Missing Black Candle block at offset "
                        + offset[0] + ", " + offset[1] + ".";
            }

            if (!state.getValue(CandleBlock.LIT)) {
                return "Black Candle at offset "
                        + offset[0] + ", " + offset[1] + " is not lit.";
            }
        }

        String[] glimmerNames = {"North", "East", "South", "West"};
        for (int i = 0; i < RitualPatterns.HOLLOWING_GLIMMERSTONES.length; i++) {
            int[] offset = RitualPatterns.HOLLOWING_GLIMMERSTONES[i];
            BlockState state = server.getBlockState(
                    worldPosition.offset(offset[0], 0, offset[1])
            );

            if (!state.is(ModBlocks.GLIMMERSTONE_CLUSTER.get())) {
                return glimmerNames[i]
                        + " Glimmerstone is missing at offset "
                        + offset[0] + ", " + offset[1] + ".";
            }
        }

        return null;
    }

    private ItemEntity findHollowingOffering(
            ServerLevel server,
            java.util.function.Predicate<ItemStack> predicate
    ) {


        AABB area = new AABB(
                worldPosition.getX() - 3.25D,
                worldPosition.getY() - 0.5D,
                worldPosition.getZ() - 3.25D,
                worldPosition.getX() + 4.25D,
                worldPosition.getY() + 2.5D,
                worldPosition.getZ() + 4.25D
        );

        return server.getEntitiesOfClass(
                        ItemEntity.class,
                        area,
                        entity -> entity.isAlive()
                                && !entity.getItem().isEmpty()
                                && predicate.test(entity.getItem())
                )
                .stream()
                .min(java.util.Comparator.comparingDouble(
                        entity -> entity.distanceToSqr(
                                worldPosition.getX() + 0.5D,
                                worldPosition.getY() + 0.15D,
                                worldPosition.getZ() + 0.5D
                        )
                ))
                .orElse(null);
    }

    private WitchesAltarBlockEntity findFundedHollowingAltar(
            ServerLevel server,
            ServerPlayer caster,
            int requiredPower
    ) {
        int range =
                MagicDeedManager.DEFAULT_ALTAR_RANGE;

        BlockPos min =
                worldPosition.offset(
                        -range,
                        -range,
                        -range
                );

        BlockPos max =
                worldPosition.offset(
                        range,
                        range,
                        range
                );

        WitchesAltarBlockEntity bestOwned = null;
        double bestOwnedDistance = Double.MAX_VALUE;

        WitchesAltarBlockEntity bestBound = null;
        double bestBoundDistance = Double.MAX_VALUE;

        for (BlockPos cursor :
                BlockPos.betweenClosed(min, max)) {

            if (cursor.distSqr(worldPosition)
                    > (long) range * range) {
                continue;
            }

            if (!(server.getBlockEntity(cursor)
                    instanceof WitchesAltarBlockEntity altar)
                    || !altar.hasCarpet()
                    || !altar.isAuthorized(caster)
                    || altar.getStoredPower() < requiredPower) {
                continue;
            }

            double distance =
                    cursor.distSqr(worldPosition);

            if (altar.isOwner(caster)) {
                if (bestOwned == null
                        || altar.getStoredPower()
                        > bestOwned.getStoredPower()
                        || (altar.getStoredPower()
                        == bestOwned.getStoredPower()
                        && distance < bestOwnedDistance)) {

                    bestOwned = altar;
                    bestOwnedDistance = distance;
                }
            } else {
                if (bestBound == null
                        || altar.getStoredPower()
                        > bestBound.getStoredPower()
                        || (altar.getStoredPower()
                        == bestBound.getStoredPower()
                        && distance < bestBoundDistance)) {

                    bestBound = altar;
                    bestBoundDistance = distance;
                }
            }
        }

        return bestOwned != null
                ? bestOwned
                : bestBound;
    }


    public boolean tryActivateHollowing(
            ServerPlayer caster,
            Vec3 sacrificePosition
    ) {
        if (!isHeartGlyph()
                || !(level instanceof ServerLevel server)) {
            return false;
        }

        if (ritualActive) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: this Heart Glyph is already performing another rite."
                    ),
                    false
            );
            return false;
        }

        if (!PlayerPathManager.isWitch(caster)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: only a witch can make the sacrifice."
                    ),
                    false
            );
            return false;
        }

        if (PlayerMagicManager.isMagicallyExhausted(caster)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: an Exhausted witch cannot cast this rite."
                    ),
                    false
            );
            return false;
        }

        if (!isInsideHollowingSacrificeBoundary(sacrificePosition)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the lamb must die inside the outer Black Candle boundary."
                    ),
                    false
            );
            return false;
        }

        String structureProblem =
                validateHollowingStructure(server);

        if (structureProblem != null) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: " + structureProblem
                    ),
                    false
            );
            return false;
        }

        ItemEntity blood =
                findHollowingOffering(
                        server,
                        stack -> stack.getItem()
                                instanceof BloodBottleItem
                );

        ItemEntity salt =
                findHollowingOffering(
                        server,
                        stack -> stack.is(
                                ModItems.SALT.get()
                        )
                );

        if (blood == null) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: Blood offering is missing inside the Small Circle."
                    ),
                    false
            );
            return false;
        }

        if (salt == null) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: Salt offering is missing inside the Small Circle."
                    ),
                    false
            );
            return false;
        }


        CompoundTag bloodTag =
                blood.getItem().getTag();

        if (bloodTag == null
                || !bloodTag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)
                || !bloodTag.contains(BloodBottleItem.TAG_ENTITY_UUID)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the Blood offering must be a witch/player's blood."
                    ),
                    false
            );
            return false;
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(
                    bloodTag.getString(
                            BloodBottleItem.TAG_ENTITY_UUID
                    )
            );
        } catch (IllegalArgumentException invalidUuid) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the Blood offering has invalid identity data."
                    ),
                    false
            );
            return false;
        }

        ServerPlayer target =
                server.getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null || target.level() != server) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the witch whose Blood was offered must be online and present."
                    ),
                    false
            );
            return false;
        }

        if (!PlayerPathManager.isWitch(target)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the Blood belongs to someone who is not a witch."
                    ),
                    false
            );
            return false;
        }

        if (PlayerMagicManager.isMagicallyExhausted(target)) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the target witch is already Exhausted."
                    ),
                    false
            );
            return false;
        }


        double targetDx = Math.abs(
                target.getX() - (worldPosition.getX() + 0.5D)
        );
        double targetDz = Math.abs(
                target.getZ() - (worldPosition.getZ() + 0.5D)
        );

        boolean insideSmallCircle =
                targetDx < 3.0D
                        && targetDz < 3.0D
                        && targetDx + targetDz < 4.0D
                        && Math.abs(
                                target.getY() - worldPosition.getY()
                        ) < 3.0D;

        if (!insideSmallCircle) {
            caster.displayClientMessage(
                    Component.literal(
                            "Hollowing failed: the witch whose Blood was offered must stand inside the Small Black Circle."
                    ),
                    false
            );
            return false;
        }


        int activationCost =
                RitualPatterns.HOLLOWING.magicCost();


        WitchesAltarBlockEntity altar =
                findFundedHollowingAltar(
                        server,
                        caster,
                        activationCost
                );

        if (altar == null) {
            caster.displayClientMessage(
                    Component.literal(
                            "The Rite of Hollowing requires "
                                    + activationCost
                                    + " altar magic."
                    ),
                    true
            );
            return false;
        }

        int extracted =
                altar.extractPower(
                        activationCost
                );

        if (extracted != activationCost) {


            if (extracted > 0) {
                altar.addPower(extracted);
            }
            return false;
        }


        clearOfferingSlots();
        captureAndConsumeOne(blood, 0);
        captureAndConsumeOne(salt, 1);

        hollowingTargetUuid =
                target.getUUID();
        hollowingStartingMagic =
                PlayerMagicManager.getMagic(target);

        ritualCasterUuid =
                caster.getUUID();
        ritualKind =
                RitualKind.HOLLOWING;
        ritualSuccessful =
                true;
        ritualTicks =
                0;
        ritualActive =
                true;

        sync();

        MagicalActEffects.impact(
                caster,
                worldPosition
        );

        if (target != caster) {
            MagicalActEffects.impact(
                    target,
                    worldPosition
            );
        }

        server.playSound(
                null,
                worldPosition,
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS,
                0.72F,
                0.48F
        );

        server.sendParticles(
                ParticleTypes.SOUL,
                sacrificePosition.x,
                sacrificePosition.y + 0.30D,
                sacrificePosition.z,
                12,
                0.28D,
                0.18D,
                0.28D,
                0.010D
        );

        caster.displayClientMessage(
                Component.literal(
                        "The Rite of Hollowing has begun."
                ),
                true
        );

        if (target != caster) {
            target.displayClientMessage(
                    Component.literal(
                            "The rite takes hold of your magic."
                    ),
                    true
            );
        }

        return true;
    }


    private void tickHollowing(
            ServerLevel server
    ) {
        ServerPlayer target =
                hollowingTargetUuid == null
                        ? null
                        : server.getServer()
                                .getPlayerList()
                                .getPlayer(hollowingTargetUuid);

        if (target == null
                || target.level() != server) {
            clearOfferingSlots();
            hollowingTargetUuid = null;
            hollowingStartingMagic = 0;
            ritualActive = false;
            ritualSuccessful = false;
            ritualKind = RitualKind.NONE;
            ritualTicks = 0;
            sync();
            return;
        }

        if (ritualTicks == 42) {
            poofOffering(server, 0);
        }

        if (ritualTicks == 68) {
            poofOffering(server, 1);
        }


        for (int i = 0; i < HOLLOWING_LINE_TICKS.length; i++) {
            if (ritualTicks == HOLLOWING_LINE_TICKS[i]) {
                int[] offset = RitualPatterns.HOLLOWING_GLIMMERSTONES[i];
                BlockPos stone = worldPosition.offset(offset[0], 0, offset[1]);

                server.sendParticles(
                        ParticleTypes.EXPLOSION,
                        stone.getX() + 0.5D,
                        stone.getY() + 0.55D,
                        stone.getZ() + 0.5D,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );

                server.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        stone.getX() + 0.5D,
                        stone.getY() + 0.55D,
                        stone.getZ() + 0.5D,
                        7,
                        0.18D, 0.22D, 0.18D,
                        0.015D
                );

                server.playSound(
                        null,
                        stone,
                        SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.BLOCKS,
                        0.48F + i * 0.06F,
                        0.82F - i * 0.06F
                );

                sync();
            }
        }


        if (ritualTicks >= HOLLOWING_LINE_TICKS[0]) {
            double fractionRemoved;

            if (ritualTicks < HOLLOWING_LINE_TICKS[1]) {
                fractionRemoved = lerpHollowing(
                        0.0D, 0.12D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_LINE_TICKS[0],
                                HOLLOWING_LINE_TICKS[1]
                        )
                );
            } else if (ritualTicks < HOLLOWING_LINE_TICKS[2]) {
                fractionRemoved = lerpHollowing(
                        0.12D, 0.32D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_LINE_TICKS[1],
                                HOLLOWING_LINE_TICKS[2]
                        )
                );
            } else if (ritualTicks < HOLLOWING_LINE_TICKS[3]) {
                fractionRemoved = lerpHollowing(
                        0.32D, 0.60D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_LINE_TICKS[2],
                                HOLLOWING_LINE_TICKS[3]
                        )
                );
            } else if (ritualTicks < HOLLOWING_CORRUPTION_TICK) {
                fractionRemoved = lerpHollowing(
                        0.60D, 0.78D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_LINE_TICKS[3],
                                HOLLOWING_CORRUPTION_TICK
                        )
                );
            } else if (ritualTicks < HOLLOWING_FINAL_BREAK_TICK) {
                fractionRemoved = lerpHollowing(
                        0.78D, 0.94D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_CORRUPTION_TICK,
                                HOLLOWING_FINAL_BREAK_TICK
                        )
                );
            } else {
                fractionRemoved = lerpHollowing(
                        0.94D, 1.0D,
                        hollowingPhaseProgress(
                                ritualTicks,
                                HOLLOWING_FINAL_BREAK_TICK,
                                RitualPatterns.HOLLOWING.duration()
                        )
                );
            }

            int desiredRemaining =
                    Math.max(
                            0,
                            (int)Math.ceil(
                                    hollowingStartingMagic
                                            * (1.0D - fractionRemoved)
                            )
                    );

            int current =
                    PlayerMagicManager.getMagic(target);

            if (current > desiredRemaining) {
                PlayerMagicManager.removeMagic(
                        target,
                        current - desiredRemaining
                );
            }
        }


        if (ritualTicks == HOLLOWING_CORRUPTION_TICK) {
            MagicalActEffects.impact(target, worldPosition);

            for (int[] offset : RitualPatterns.HOLLOWING_GLIMMERSTONES) {
                BlockPos stone = worldPosition.offset(offset[0], 0, offset[1]);

                server.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        stone.getX() + 0.5D,
                        stone.getY() + 0.55D,
                        stone.getZ() + 0.5D,
                        10,
                        0.20D, 0.24D, 0.20D,
                        0.015D
                );

                server.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        stone.getX() + 0.5D,
                        stone.getY() + 0.55D,
                        stone.getZ() + 0.5D,
                        6,
                        0.18D, 0.18D, 0.18D,
                        0.008D
                );
            }

            server.playSound(
                    null,
                    worldPosition,
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(),
                    SoundSource.BLOCKS,
                    0.54F,
                    0.58F
            );
        }


        if (ritualTicks >= HOLLOWING_CANDLE_DYING_TICK
                && ritualTicks < HOLLOWING_CANDLE_DYING_TICK + 44) {

            int elapsed =
                    ritualTicks - HOLLOWING_CANDLE_DYING_TICK;


            int wave =
                    elapsed / 11;

            int withinWave =
                    elapsed % 11;

            if (wave < 4) {
                int candleIndex =
                        wave * 11 + withinWave;

                int[] offset =
                        RitualPatterns.HOLLOWING_CANDLE_RING[candleIndex];

                BlockPos candlePos =
                        worldPosition.offset(
                                offset[0],
                                0,
                                offset[1]
                        );

                BlockState candleState =
                        server.getBlockState(
                                candlePos
                        );

                if (candleState.is(Blocks.BLACK_CANDLE)
                        && candleState.hasProperty(CandleBlock.LIT)
                        && candleState.getValue(CandleBlock.LIT)) {

                    server.setBlock(
                            candlePos,
                            candleState.setValue(
                                    CandleBlock.LIT,
                                    false
                            ),
                            3
                    );

                    server.playSound(
                            null,
                            candlePos,
                            SoundEvents.CANDLE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.16F,
                            0.72F
                                    + wave * 0.035F
                    );

                    if (withinWave == 0) {
                        server.sendParticles(
                                ParticleTypes.SMOKE,
                                candlePos.getX() + 0.5D,
                                candlePos.getY() + 0.42D,
                                candlePos.getZ() + 0.5D,
                                3,
                                0.04D, 0.06D, 0.04D,
                                0.004D
                        );
                    }
                }
            }
        }

        if (ritualTicks == HOLLOWING_FINAL_BREAK_TICK) {
            server.sendParticles(
                    ParticleTypes.EXPLOSION,
                    target.getX(),
                    target.getY() + 1.0D,
                    target.getZ(),
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );

            server.sendParticles(
                    ParticleTypes.SOUL,
                    target.getX(),
                    target.getY() + 1.0D,
                    target.getZ(),
                    18,
                    0.34D, 0.55D, 0.34D,
                    0.015D
            );
        }

        if (ritualTicks >= RitualPatterns.HOLLOWING.duration()) {
            PlayerMagicManager.setMagic(target, 0);


            for (int[] offset : RitualPatterns.HOLLOWING_GLIMMERSTONES) {
                BlockPos stonePos =
                        worldPosition.offset(offset[0], 0, offset[1]);

                BlockState oldState =
                        server.getBlockState(stonePos);

                if (!oldState.is(ModBlocks.GLIMMERSTONE_CLUSTER.get())) {
                    continue;
                }

                BlockState gloom =
                        ModBlocks.GLOOMSTONE_CLUSTER.get()
                                .defaultBlockState();

                if (oldState.hasProperty(
                        net.minecraft.world.level.block.AmethystClusterBlock.FACING)) {
                    gloom = gloom.setValue(
                            net.minecraft.world.level.block.AmethystClusterBlock.FACING,
                            oldState.getValue(
                                    net.minecraft.world.level.block.AmethystClusterBlock.FACING
                            )
                    );
                }

                if (oldState.hasProperty(
                        net.minecraft.world.level.block.AmethystClusterBlock.WATERLOGGED)) {
                    gloom = gloom.setValue(
                            net.minecraft.world.level.block.AmethystClusterBlock.WATERLOGGED,
                            oldState.getValue(
                                    net.minecraft.world.level.block.AmethystClusterBlock.WATERLOGGED
                            )
                    );
                }

                server.setBlock(
                        stonePos,
                        gloom,
                        3
                );

                server.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        stonePos.getX() + 0.5D,
                        stonePos.getY() + 0.55D,
                        stonePos.getZ() + 0.5D,
                        8,
                        0.18D, 0.20D, 0.18D,
                        0.012D
                );

                server.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        stonePos.getX() + 0.5D,
                        stonePos.getY() + 0.55D,
                        stonePos.getZ() + 0.5D,
                        8,
                        0.16D, 0.16D, 0.16D,
                        0.006D
                );
            }

            PlayerMagicManager.beginPurgeExhaust(target);

            MagicalActEffects.impact(
                    target,
                    worldPosition
            );

            server.playSound(
                    null,
                    worldPosition,
                    SoundEvents.SOUL_ESCAPE,
                    SoundSource.PLAYERS,
                    0.78F,
                    0.54F
            );

            clearOfferingSlots();
            hollowingTargetUuid = null;
            hollowingStartingMagic = 0;
            ritualActive = false;
            ritualSuccessful = false;
            ritualKind = RitualKind.NONE;
            ritualTicks = 0;
            departureTicks = DEPARTURE_DURATION;
            sync();
        }
    }

    private static double hollowingPhaseProgress(
            int tick,
            int start,
            int end
    ) {
        if (end <= start) return 1.0D;
        double value =
                (tick - start)
                        / (double)(end - start);
        value =
                Math.max(
                        0.0D,
                        Math.min(1.0D, value)
                );
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerpHollowing(
            double from,
            double to,
            double progress
    ) {
        return from + (to - from) * progress;
    }


    public boolean tryActivateWoodsFavor(ServerPlayer caster) {
        if (!isHeartGlyph() || ritualActive || !PlayerPathManager.isWitch(caster)) return false;
        if (!(level instanceof ServerLevel server)) return false;

        RitualPattern.Match match = RitualPatterns.WOODS_FAVOR.match(server, worldPosition);
        if (match == null) return false;

        ItemEntity stick = match.offering("stick");
        ItemEntity growth = match.offering("growth");
        ItemEntity blood = match.offering("blood");
        ItemEntity string = match.offering("string");
        if (stick == null || growth == null || blood == null || string == null) return false;

        CompoundTag bloodTag = blood.getItem().getTag();
        if (bloodTag == null
                || !bloodTag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)
                || !bloodTag.contains(BloodBottleItem.TAG_ENTITY_UUID)) {
            caster.displayClientMessage(Component.literal(
                    "The Wood's Favor requires the casting witch's own blood."), true);
            return false;
        }

        UUID bloodUuid;
        try {
            bloodUuid = UUID.fromString(
                    bloodTag.getString(BloodBottleItem.TAG_ENTITY_UUID));
        } catch (IllegalArgumentException invalid) {
            return false;
        }
        if (!bloodUuid.equals(caster.getUUID())) {
            caster.displayClientMessage(Component.literal(
                    "The blood in the circle does not belong to you."), true);
            return false;
        }

        WandWoodType woodType = WandWoodType.fromGrowthToken(growth.getItem());
        if (woodType == null) return false;

        MagicDeedResult payment = MagicDeedManager.performRitual(
                caster, worldPosition, RitualPatterns.WOODS_FAVOR.magicCost());
        if (!payment.successful()) {
            caster.displayClientMessage(Component.literal(
                    "The wood hears the call, but you cannot sustain it."), true);
            return false;
        }

        clearOfferingSlots();
        captureAndConsumeOne(stick, 0);
        captureAndConsumeOne(growth, 1);
        captureAndConsumeOne(blood, 2);
        captureAndConsumeOne(string, 3);

        woodsFavorWoodId = woodType.id();
        ritualKind = RitualKind.WOODS_FAVOR;
        ritualCasterUuid = caster.getUUID();
        ritualSuccessful = true;
        ritualTicks = 0;
        ritualActive = true;
        sync();

        MagicalActEffects.impact(caster, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 0.38F, 0.72F);
        server.sendParticles(ParticleTypes.WHITE_ASH,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.20D,
                worldPosition.getZ() + 0.5D,
                8, 0.55D, 0.06D, 0.55D, 0.003D);
        return true;
    }

    private void tickWoodsFavor(ServerLevel server) {
        for (int i = 0; i < WOODS_FAVOR_POOF_TICKS.length; i++) {
            if (ritualTicks == WOODS_FAVOR_POOF_TICKS[i]) {
                poofOffering(server, i);
            }
        }

        if (ritualTicks == 140) {


            for (int i = 0; i < 36; i++) {
                double angle = Math.PI * 2.0D * i / 36.0D;
                double radius = 0.4D + (i % 6) * 0.45D;
                server.sendParticles(
                        ParticleTypes.COMPOSTER,
                        worldPosition.getX() + 0.5D + Math.cos(angle) * radius,
                        worldPosition.getY() + 0.18D,
                        worldPosition.getZ() + 0.5D + Math.sin(angle) * radius,
                        1, 0.02D, 0.035D, 0.02D, 0.0D
                );
            }
            server.playSound(null, worldPosition, SoundEvents.GRASS_PLACE,
                    SoundSource.BLOCKS, 0.48F, 0.72F);
        }

        if (ritualTicks == WOODS_FAVOR_MANIFEST_TICK) {
            ServerPlayer caster = getRitualCaster(server);
            WandWoodType woodType = WandWoodType.fromId(woodsFavorWoodId);

            if (caster != null && woodType != null) {
                MagicalActEffects.impact(caster, worldPosition);


                ItemStack core = new ItemStack(woodType.coreItem());

                ItemEntity result = new ItemEntity(
                        server,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.72D,
                        worldPosition.getZ() + 0.5D,
                        core
                );
                result.setDeltaMovement(0.0D, 0.11D, 0.0D);
                result.setPickUpDelay(10);
                server.addFreshEntity(result);

                server.sendParticles(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.82D,
                        worldPosition.getZ() + 0.5D,
                        18, 0.18D, 0.20D, 0.18D, 0.018D);
                server.sendParticles(ParticleTypes.COMPOSTER,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.70D,
                        worldPosition.getZ() + 0.5D,
                        16, 0.35D, 0.16D, 0.35D, 0.012D);
                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.55F, 1.18F);
            }
        }

        if (ritualTicks >= WOODS_FAVOR_END_TICK) {
            clearOfferingSlots();
            ritualActive = false;
            ritualSuccessful = false;
            ritualKind = RitualKind.NONE;
            ritualTicks = 0;
            woodsFavorWoodId = "";
            departureTicks = DEPARTURE_DURATION;
            sync();
        }
    }

    private boolean activateCornerRite(ServerLevel server, ServerPlayer caster,
                                      RitualPattern.Match match, boolean calling) {


        CornerDirection[] directions = {CornerDirection.EAST, CornerDirection.SOUTH,
                CornerDirection.WEST, CornerDirection.NORTH};
        BlockPos[] matchedCandles = new BlockPos[directions.length];
        for (int i = 0; i < directions.length; i++) {
            BlockPos matched = match.block(directions[i].key() + "_candle");
            if (matched == null) {
                invalidPatternFeedback(server);
                return false;
            }
            matchedCandles[i] = matched.immutable();
        }

        ItemEntity blessing = match.offering("blessing");
        ItemEntity sage = match.offering("sage");
        if (blessing == null || sage == null) {
            missingOfferingFeedback(server);
            return false;
        }

        clearOfferingSlots();
        captureAndConsumeOne(blessing, 0);
        captureAndConsumeOne(sage, 1);

        for (int i = 0; i < matchedCandles.length; i++) {
            cornerCandlePositions[i] = matchedCandles[i];
            cornerFlameStartTicks[i] = -1;
            cornerFlameEndTicks[i] = -1;
        }

        ritualKind = calling ? RitualKind.CALL_CORNERS : RitualKind.UNCALL_CORNERS;
        MagicDeedResult payment = MagicDeedManager.performRitual(caster, worldPosition,
                calling ? RitualPatterns.CALL_CORNERS.magicCost() : RitualPatterns.UNCALL_CORNERS.magicCost());
        ritualSuccessful = payment.successful();
        ritualTicks = 0;
        ritualActive = true;
        cornerStep = 0;
        cornerCompleteTick = -1;
        cornerFlameSequenceStartTick = -1;
        cornerAwaitingSequence = false;
        cornerCasterUuid = caster.getUUID();
        ritualCasterUuid = caster.getUUID();
        sync();

        MagicalActEffects.impact(caster, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.38F, calling ? 0.82F : 0.66F);
        server.sendParticles(ParticleTypes.END_ROD,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.25D, worldPosition.getZ() + 0.5D,
                5, 0.42D, 0.06D, 0.42D, 0.002D);
        return true;
    }

    private boolean activateTransmutation(ServerLevel server, ServerPlayer caster,
                                          RitualPattern.Match match, List<BlockPos> clusters) {
        ItemEntity pure = match.offering("pure_essence");
        ItemEntity wool = match.offering("wool_of_bat");
        ItemEntity salt = match.offering("salt");
        ItemEntity tear = match.offering("tear");
        ItemEntity milk = match.offering("milk");
        ItemEntity spit = match.offering("spit");
        if (pure == null || wool == null || salt == null || tear == null || milk == null || spit == null) {
            missingOfferingFeedback(server);
            return false;
        }

        int clusterCount = clusters.size();
        int essenceRequired = Math.max(1, (clusterCount + 3) / 4);
        if (pure.getItem().getCount() < essenceRequired) {
            caster.displayClientMessage(Component.literal(
                    "The working needs " + essenceRequired + " Pure Essence for "
                            + clusterCount + " cluster" + (clusterCount == 1 ? "." : "s.")
            ), true);
            missingOfferingFeedback(server);
            return false;
        }

        int magicCost = TRANSMUTATION_BASE_MAGIC_COST
                + clusterCount * TRANSMUTATION_MAGIC_PER_CLUSTER;


        MagicDeedResult payment = MagicDeedManager.performRitual(caster, worldPosition, magicCost);
        if (!payment.successful()) {
            caster.displayClientMessage(Component.literal("The transmutation cannot gather enough magic."), true);
            invalidPatternFeedback(server);
            return false;
        }

        clearOfferingSlots();
        captureAndConsumeAmount(pure, 0, essenceRequired);
        captureAndConsumeOne(wool, 1);
        captureAndConsumeOne(salt, 2);
        captureAndConsumeOne(tear, 3);
        captureAndConsumeOne(milk, 4);
        captureAndConsumeOne(spit, 5);

        transmutationClusters.clear();
        for (BlockPos cluster : clusters) transmutationClusters.add(cluster.immutable());

        ritualKind = RitualKind.TRANSMUTATION;
        ritualCasterUuid = caster.getUUID();
        ritualSuccessful = true;
        ritualTicks = 0;
        ritualActive = true;
        sync();

        MagicalActEffects.impact(caster, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 0.46F, 1.05F);
        server.sendParticles(ParticleTypes.END_ROD,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.30D, worldPosition.getZ() + 0.5D,
                12, 0.55D, 0.10D, 0.55D, 0.005D);
        return true;
    }

    private List<BlockPos> findPlacedTransmutationClusters(ServerLevel server) {
        List<BlockPos> found = new ArrayList<>();


        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos pos = worldPosition.offset(dx, 0, dz);
                BlockState state = server.getBlockState(pos);
                if (!state.is(Blocks.AMETHYST_CLUSTER)) continue;
                if (state.hasProperty(BlockStateProperties.FACING)
                        && state.getValue(BlockStateProperties.FACING) != Direction.UP) continue;
                found.add(pos.immutable());
            }
        }
        return found;
    }

    private void tickTransmutation(ServerLevel server) {
        for (int i = 0; i < TRANSMUTATION_POOF_TICKS.length; i++) {
            if (ritualTicks == TRANSMUTATION_POOF_TICKS[i]) {
                poofOffering(server, i);
            }
        }

        ServerPlayer caster = getRitualCaster(server);


        if (ritualTicks == TRANSMUTATION_FIRST_IMPACT_TICK) {
            if (caster != null) MagicalActEffects.impact(caster, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.52F, 0.88F);
            sync();
        }


        if (ritualTicks == TRANSMUTATION_FLAME_FULL_TICK) {
            server.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, 0.42F, 0.92F);
        }


        if (ritualTicks == TRANSMUTATION_SECOND_IMPACT_TICK) {
            if (caster != null) MagicalActEffects.impact(caster, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.56F, 1.08F);
            sync();
        }

        int essenceCount = transmutationClusters.size();
        int essencePhaseEnd = TRANSMUTATION_ESSENCE_START
                + essenceCount * TRANSMUTATION_ESSENCE_TOTAL_TICKS;

        if (ritualTicks >= TRANSMUTATION_ESSENCE_START && ritualTicks < essencePhaseEnd) {
            int elapsed = ritualTicks - TRANSMUTATION_ESSENCE_START;
            int index = elapsed / TRANSMUTATION_ESSENCE_TOTAL_TICKS;
            int localTick = elapsed % TRANSMUTATION_ESSENCE_TOTAL_TICKS;

            if (index >= 0 && index < transmutationClusters.size()) {
                BlockPos target = transmutationClusters.get(index);
                if (server.getBlockState(target).is(Blocks.AMETHYST_CLUSTER)) {
                    tickTransmutationEssence(server, target, index, localTick);
                }
            }
        }

        int finalImpactTick = essencePhaseEnd + TRANSMUTATION_FINAL_PAUSE_TICKS;
        int changeTick = finalImpactTick + 7;
        int endTick = changeTick + TRANSMUTATION_END_PAUSE_TICKS;

        if (ritualTicks == finalImpactTick) {
            if (caster != null) MagicalActEffects.impact(caster, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 0.72F, 1.22F);
            sync();
        }

        if (ritualTicks == changeTick) {
            transmuteClusters(server);
        }

        if (ritualTicks >= endTick) {
            ritualActive = false;
            ritualSuccessful = false;
            ritualTicks = 0;
            departureTicks = DEPARTURE_DURATION;
            clearRuntimeState();
        }
    }


    private void tickTransmutationEssence(
            ServerLevel server,
            BlockPos target,
            int essenceIndex,
            int localTick
    ) {
        Vec3 heart = new Vec3(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.42D,
                worldPosition.getZ() + 0.5D
        );
        Vec3 targetTop = new Vec3(
                target.getX() + 0.5D,
                target.getY() + 1.12D,
                target.getZ() + 0.5D
        );
        Vec3 targetCore = new Vec3(
                target.getX() + 0.5D,
                target.getY() + 0.42D,
                target.getZ() + 0.5D
        );

        if (localTick < TRANSMUTATION_ESSENCE_TRAVEL_TICKS) {
            double p = localTick / (double) Math.max(1, TRANSMUTATION_ESSENCE_TRAVEL_TICKS - 1);
            double eased = smoothStep(p);

            Vec3 base = heart.lerp(targetTop, eased);
            Vec3 direction = targetTop.subtract(heart);
            Vec3 perpendicular = new Vec3(-direction.z, 0.0D, direction.x);
            if (perpendicular.lengthSqr() > 1.0E-6D) perpendicular = perpendicular.normalize();

            double spiralEnvelope = Math.sin(Math.PI * p);
            double angle = p * Math.PI * 4.0D + essenceIndex * 1.37D;
            double radius = 0.34D * spiralEnvelope;

            Vec3 sideways = perpendicular.scale(Math.cos(angle) * radius);
            Vec3 vertical = new Vec3(0.0D, Math.sin(angle) * radius * 0.55D, 0.0D);
            Vec3 pos = base.add(sideways).add(vertical);

            spawnWhiteEssence(server, pos, 3);
            return;
        }

        int hoverStart = TRANSMUTATION_ESSENCE_TRAVEL_TICKS;
        int descendStart = hoverStart + TRANSMUTATION_ESSENCE_HOVER_TICKS;
        int sparkleStart = descendStart + TRANSMUTATION_ESSENCE_DESCEND_TICKS;

        if (localTick < descendStart) {
            int hoverTick = localTick - hoverStart;
            double time = hoverTick * 0.22D + essenceIndex * 1.41D;
            Vec3 pos = targetTop.add(
                    Math.cos(time) * 0.10D,
                    Math.sin(time * 1.35D) * 0.08D,
                    Math.sin(time) * 0.10D
            );
            spawnWhiteEssence(server, pos, 4);
            return;
        }

        if (localTick < sparkleStart) {
            double p = (localTick - descendStart)
                    / (double) Math.max(1, TRANSMUTATION_ESSENCE_DESCEND_TICKS - 1);
            Vec3 pos = targetTop.lerp(targetCore, smoothStep(p));
            spawnWhiteEssence(server, pos, 4);

            if (localTick == sparkleStart - 1) {
                server.playSound(null, target, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.42F, 1.30F);
            }
            return;
        }


        if ((localTick & 1) == 0) {
            server.sendParticles(
                    ParticleTypes.END_ROD,
                    target.getX() + 0.5D,
                    target.getY() + 0.55D,
                    target.getZ() + 0.5D,
                    3,
                    0.16D, 0.30D, 0.16D,
                    0.008D
            );
        }
    }

    private void spawnWhiteEssence(ServerLevel server, Vec3 pos, int count) {


        server.sendParticles(
                ParticleTypes.END_ROD,
                pos.x, pos.y, pos.z,
                count,
                0.035D, 0.035D, 0.035D,
                0.001D
        );
        server.sendParticles(
                ParticleTypes.WHITE_ASH,
                pos.x, pos.y, pos.z,
                Math.max(1, count / 2),
                0.025D, 0.025D, 0.025D,
                0.0D
        );
    }

    private void transmuteClusters(ServerLevel server) {
        int changed = 0;
        for (BlockPos pos : transmutationClusters) {
            BlockState oldState = server.getBlockState(pos);
            if (!oldState.is(Blocks.AMETHYST_CLUSTER)) continue;

            BlockState replacement = ModBlocks.GLIMMERSTONE_CLUSTER.get().defaultBlockState();
            if (oldState.hasProperty(BlockStateProperties.FACING)
                    && replacement.hasProperty(BlockStateProperties.FACING)) {
                replacement = replacement.setValue(BlockStateProperties.FACING,
                        oldState.getValue(BlockStateProperties.FACING));
            }
            if (oldState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && replacement.hasProperty(BlockStateProperties.WATERLOGGED)) {
                replacement = replacement.setValue(BlockStateProperties.WATERLOGGED,
                        oldState.getValue(BlockStateProperties.WATERLOGGED));
            }

            server.setBlock(pos, replacement, 3);
            server.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    10, 0.18D, 0.28D, 0.18D, 0.012D);
            changed++;
        }

        server.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.BLOCKS, 0.72F, 1.34F);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 0.68F, 1.08F);

        ServerPlayer caster = getRitualCaster(server);
        if (caster != null) {
            caster.displayClientMessage(Component.literal(
                    changed + " Glimmerstone Cluster" + (changed == 1 ? " answers" : "s answer")
                            + " the transmutation."
            ), true);
        }
        sync();
    }

    private boolean activateEarthVeins(ServerLevel server, ServerPlayer caster, RitualPattern.Match match) {
        ItemEntity milk = match.offering("milk");
        ItemEntity egg = match.offering("egg");
        ItemEntity dirt = match.offering("dirt");
        ItemEntity calling = match.offering("calling");
        ItemEntity pickaxe = match.offering("pickaxe");
        if (milk == null || egg == null || dirt == null || calling == null || pickaxe == null) return false;

        EarthVeinTarget target = EarthVeinTarget.fromCalling(calling.getItem());
        if (target == EarthVeinTarget.NONE) return false;

        clearOfferingSlots();
        captureAndConsumeOne(milk, 0);
        captureAndConsumeOne(egg, 1);
        captureAndConsumeOne(dirt, 2);
        captureAndConsumeWholeStack(calling, 3);
        captureAndConsumeOne(pickaxe, EARTH_VEINS_PICKAXE_SLOT);

        int candles = match.augments().count(RitualAugmentType.CANDLE);
        int power = match.augments().count(RitualAugmentType.SKULL)
                + match.augments().count(RitualAugmentType.HEAD);
        int crystals = match.augments().count(RitualAugmentType.CRYSTAL);

        earthVeinsOfferingCount = offerings.get(3).getCount();
        float returnMultiplier = Math.min(3.0F, 2.0F + power * 0.25F);
        earthVeinsMaximum = Math.max(1, Mth.floor(earthVeinsOfferingCount * returnMultiplier));
        earthVeinsPulseInterval = Math.max(8, 28 - candles * 2);
        earthVeinsSearchRadius = Math.min(32, 16 + crystals * 4);
        earthVeinsExtracted = 0;
        earthVeinsFinishTicks = -1;
        earthVeinTarget = target;
        earthVeinCandidates.clear();
        ritualKind = RitualKind.EARTH_VEINS;
        ritualCasterUuid = caster.getUUID();

        int augmentedMagicCost = RitualPatterns.EARTH_VEINS.magicCost()
                + candles * EARTH_VEINS_CANDLE_MAGIC_COST
                + power * EARTH_VEINS_POWER_AUGMENT_MAGIC_COST
                + crystals * EARTH_VEINS_CRYSTAL_MAGIC_COST;

        MagicDeedResult payment = MagicDeedManager.performRitual(caster, worldPosition, augmentedMagicCost);
        ritualSuccessful = payment.successful();
        ritualTicks = 0;
        ritualActive = true;
        sync();

        MagicalActEffects.impact(caster, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.ROOTED_DIRT_BREAK,
                SoundSource.BLOCKS, 0.62F, 0.58F);
        caster.displayClientMessage(Component.literal("The earth begins listening for " + target.displayName + "."), true);
        return true;
    }

    private boolean activateTemperance(ServerLevel server, ServerPlayer caster, TemperanceSetup setup) {
        MagicState targetState = PlayerMagicManager.getState(setup.target());
        if (targetState == MagicState.FESTERING || targetState == MagicState.SUPERCRITICAL) {
            caster.displayClientMessage(
                    Component.literal("Temperance can no longer quiet magic this deeply rooted."),
                    true
            );
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundSource.BLOCKS, 0.45F, 0.48F);
            return false;
        }

        clearOfferingSlots();
        captureAndConsumeOne(setup.blood(), 0);
        captureAndConsumeOne(setup.pulse(), 1);
        captureAndConsumeOne(setup.salt(), 2);
        captureAndConsumeOne(setup.sage(), 3);
        captureAndConsumeOne(setup.moonlight(), 4);

        for (int i = 0; i < 4; i++) grayCandlePositions[i] = setup.grayCandles()[i].immutable();
        temperanceTargetUuid = setup.target().getUUID();
        temperanceStartingMagic = PlayerMagicManager.getMagic(setup.target());
        ritualKind = RitualKind.TEMPERANCE;
        ritualCasterUuid = caster.getUUID();

        MagicDeedResult payment = MagicDeedManager.performRitual(caster, worldPosition, RitualPatterns.TEMPERANCE.magicCost());
        ritualSuccessful = payment.successful();
        ritualTicks = 0;
        ritualActive = true;
        sync();

        MagicalActEffects.impact(caster, worldPosition);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 0.50F, 0.48F);
        server.playSound(null, worldPosition, SoundEvents.CANDLE_AMBIENT,
                SoundSource.BLOCKS, 0.55F, 0.70F);
        server.sendParticles(ParticleTypes.WHITE_ASH,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.25D, worldPosition.getZ() + 0.5D,
                7, 0.46D, 0.05D, 0.46D, 0.001D);
        return true;
    }

    @Nullable
    private TemperanceSetup findTemperanceSetup(ServerLevel server) {
        RitualPattern.Match match = RitualPatterns.TEMPERANCE.match(server, worldPosition);
        if (match == null) return null;

        ItemEntity blood = match.offering("blood");
        CompoundTag bloodTag = blood.getItem().getTag();
        if (bloodTag == null || !bloodTag.getBoolean(BloodBottleItem.TAG_IS_PLAYER)) return null;

        try {
            UUID targetUuid = UUID.fromString(bloodTag.getString(BloodBottleItem.TAG_ENTITY_UUID));
            ServerPlayer target = server.getServer().getPlayerList().getPlayer(targetUuid);
            if (target == null || !PlayerPathManager.isWitch(target)
                    || PlayerMagicManager.getMagic(target) <= MagicConstants.NATURAL_LIMIT) return null;

            BlockPos[] candles = {
                    match.block("north_candle"), match.block("east_candle"),
                    match.block("south_candle"), match.block("west_candle")
            };
            return new TemperanceSetup(
                    blood,
                    match.offering("pulse"),
                    match.offering("salt"),
                    match.offering("sage"),
                    match.offering("moonlight"),
                    candles,
                    target);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    private OfferingPair findThrownOfferings(ServerLevel server) {


        AABB searchBox = new AABB(
                worldPosition.getX() - 2.95D, worldPosition.getY() - 0.25D,
                worldPosition.getZ() - 2.95D,
                worldPosition.getX() + 3.95D, worldPosition.getY() + 2.25D,
                worldPosition.getZ() + 3.95D);

        List<ItemEntity> entities = server.getEntitiesOfClass(ItemEntity.class, searchBox,
                entity -> entity.isAlive() && !entity.getItem().isEmpty());

        Comparator<ItemEntity> nearestToHeart = Comparator.comparingDouble(entity ->
                entity.distanceToSqr(worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.15D,
                        worldPosition.getZ() + 0.5D));

        ItemEntity daisy = entities.stream()
                .filter(entity -> entity.getItem().is(Items.OXEYE_DAISY))
                .min(nearestToHeart)
                .orElse(null);
        ItemEntity moonlight = entities.stream()
                .filter(entity -> entity.getItem().is(ModItems.PALE_MOONLIGHT.get()))
                .min(nearestToHeart)
                .orElse(null);

        return daisy != null && moonlight != null ? new OfferingPair(daisy, moonlight) : null;
    }

    private void captureAndConsumeOne(ItemEntity entity, int slot) {
        ItemStack entityStack = entity.getItem();
        ItemStack captured = entityStack.copy();
        captured.setCount(1);
        offerings.set(slot, captured);

        offeringStartX[slot] = entity.getX() - (worldPosition.getX() + 0.5D);
        offeringStartY[slot] = entity.getY() - worldPosition.getY();
        offeringStartZ[slot] = entity.getZ() - (worldPosition.getZ() + 0.5D);

        if (entityStack.getCount() <= 1) {
            entity.discard();
        } else {
            entityStack.shrink(1);
            entity.setItem(entityStack);
        }
    }

    private void captureAndConsumeAmount(ItemEntity entity, int slot, int amount) {
        int take = Math.max(1, Math.min(amount, entity.getItem().getCount()));
        ItemStack entityStack = entity.getItem();
        ItemStack captured = entityStack.copy();
        captured.setCount(take);
        offerings.set(slot, captured);

        offeringStartX[slot] = entity.getX() - (worldPosition.getX() + 0.5D);
        offeringStartY[slot] = entity.getY() - worldPosition.getY();
        offeringStartZ[slot] = entity.getZ() - (worldPosition.getZ() + 0.5D);

        if (entityStack.getCount() <= take) {
            entity.discard();
        } else {
            entityStack.shrink(take);
            entity.setItem(entityStack);
        }
    }

    private void captureAndConsumeWholeStack(ItemEntity entity, int slot) {
        ItemStack captured = entity.getItem().copy();
        offerings.set(slot, captured);

        offeringStartX[slot] = entity.getX() - (worldPosition.getX() + 0.5D);
        offeringStartY[slot] = entity.getY() - worldPosition.getY();
        offeringStartZ[slot] = entity.getZ() - (worldPosition.getZ() + 0.5D);

        entity.discard();
    }

    private void poofOffering(ServerLevel server, int slot) {
        if (slot < 0 || slot >= offerings.size() || offerings.get(slot).isEmpty()) return;


        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.72D;
        double z = worldPosition.getZ() + 0.5D;


        server.sendParticles(ParticleTypes.POOF,
                x, y, z, 9, 0.055D, 0.050D, 0.055D, 0.008D);
        server.sendParticles(ParticleTypes.END_ROD,
                x, y, z, 3, 0.065D, 0.075D, 0.065D, 0.004D);

        server.playSound(null, worldPosition, SoundEvents.SNOW_BREAK,
                SoundSource.BLOCKS, 0.48F, slot == 0 ? 1.28F : 1.08F);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_HIT,
                SoundSource.BLOCKS, 0.30F, slot == 0 ? 0.92F : 0.76F);

        offerings.set(slot, ItemStack.EMPTY);
        sync();
    }


    public boolean handleCornerCandleMagicIgnition(ServerPlayer player, BlockPos clicked) {
        if (!(level instanceof ServerLevel server) || !isCornerRite()
                || ritualKind != RitualKind.CALL_CORNERS || !ritualActive) {
            return false;
        }

        boolean isRitualCandle = false;
        for (BlockPos candlePos : cornerCandlePositions) {
            if (candlePos != null && candlePos.equals(clicked)) {
                isRitualCandle = true;
                break;
            }
        }
        if (!isRitualCandle) return false;

        if (!ritualSuccessful) {
            player.displayClientMessage(Component.literal("The rite has no power to continue."), true);
            return true;
        }
        if (!cornerAwaitingSequence) {
            player.displayClientMessage(Component.literal("The offerings have not yet been accepted."), true);
            server.playSound(null, clicked, SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS, 0.22F, 0.78F);
            return true;
        }

        CornerDirection expected = CornerDirection.CALL_ORDER[Math.min(cornerStep, 3)];
        int canonicalIndex = switch (expected) {
            case EAST -> 0; case SOUTH -> 1; case WEST -> 2; case NORTH -> 3;
        };
        if (!clicked.equals(cornerCandlePositions[canonicalIndex])) {
            server.playSound(null, clicked, SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS, 0.35F, 0.52F);
            resetCornerCandleSequence(server);
            player.displayClientMessage(Component.literal(
                    "The order breaks. Begin again with the eastern candle."), true);
            sync();
            return true;
        }

        BlockState state = server.getBlockState(clicked);
        if (!(state.getBlock() instanceof CandleBlock)
                || !state.hasProperty(CandleBlock.LIT)) {
            return false;
        }
        if (state.getValue(CandleBlock.LIT)) return true;

        server.setBlock(clicked, state.setValue(CandleBlock.LIT, true), 3);
        server.playSound(null, clicked, SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS, 0.72F, 0.86F);

        cornerStep++;
        if (cornerStep >= 4) {
            cornerAwaitingSequence = false;
            MagicalActEffects.impact(player, worldPosition);
            server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.72F, 1.02F);
            cornerFlameSequenceStartTick = ritualTicks + CORNER_FLAME_SEQUENCE_DELAY;
            cornerCompleteTick = -1;
        }
        sync();
        return true;
    }

    public boolean handleCornerCandleInteraction(ServerPlayer player, BlockPos clicked,
                                                 InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !isCornerRite()
                || !ritualActive || hand != InteractionHand.MAIN_HAND) return false;


        boolean isRitualCandle = false;
        for (BlockPos candlePos : cornerCandlePositions) {
            if (candlePos != null && candlePos.equals(clicked)) {
                isRitualCandle = true;
                break;
            }
        }
        if (!isRitualCandle) return false;

        if (!ritualSuccessful) {
            player.displayClientMessage(Component.literal("The rite has no power to continue."), true);
            return true;
        }

        if (!cornerAwaitingSequence) {
            player.displayClientMessage(Component.literal("The offerings have not yet been accepted."), true);
            server.playSound(null, clicked, SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS, 0.22F, 0.78F);
            return true;
        }

        CornerDirection[] order = ritualKind == RitualKind.CALL_CORNERS
                ? CornerDirection.CALL_ORDER : CornerDirection.UNCALL_ORDER;
        CornerDirection expected = order[Math.min(cornerStep, order.length - 1)];
        int canonicalIndex = switch (expected) {
            case EAST -> 0; case SOUTH -> 1; case WEST -> 2; case NORTH -> 3;
        };
        if (!clicked.equals(cornerCandlePositions[canonicalIndex])) {
            server.playSound(null, clicked, SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS, 0.35F, 0.52F);


            resetCornerCandleSequence(server);
            player.displayClientMessage(Component.literal(
                    ritualKind == RitualKind.CALL_CORNERS
                            ? "The order breaks. Begin again with the eastern candle."
                            : "The order breaks. Begin again with the northern candle."
            ), true);
            sync();
            return true;
        }

        BlockState state = server.getBlockState(clicked);
        if (!(state.getBlock() instanceof CandleBlock) || !state.hasProperty(CandleBlock.LIT)) return false;
        boolean calling = ritualKind == RitualKind.CALL_CORNERS;
        if (state.getValue(CandleBlock.LIT) == calling) return true;

        if (calling) {
            ItemStack held = player.getItemInHand(hand);
            if (!held.is(Items.FLINT_AND_STEEL) && !held.is(Items.FIRE_CHARGE)) return false;
            server.setBlock(clicked, state.setValue(CandleBlock.LIT, true), 3);
            if (!player.getAbilities().instabuild) {
                if (held.is(Items.FLINT_AND_STEEL)) held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                else held.shrink(1);
            }
            server.playSound(null, clicked, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.72F, 0.86F);
        } else {
            if (!player.getItemInHand(hand).isEmpty()) return false;
            server.setBlock(clicked, state.setValue(CandleBlock.LIT, false), 3);
            server.playSound(null, clicked, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 0.72F, 0.94F);
        }


        cornerStep++;
        if (cornerStep >= 4) {
            cornerAwaitingSequence = false;
            MagicalActEffects.impact(player, worldPosition);
            if (calling) {


                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.72F, 1.02F);
                cornerFlameSequenceStartTick = ritualTicks + CORNER_FLAME_SEQUENCE_DELAY;
                cornerCompleteTick = -1;
            } else {


                server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 0.72F, 0.92F);
                cornerFlameSequenceStartTick = ritualTicks + CORNER_UNCALL_RESPONSE_DELAY;
                cornerCompleteTick = -1;
            }
        }
        sync();
        return true;
    }

    private void resetCornerCandleSequence(ServerLevel server) {
        boolean shouldBeLit = ritualKind == RitualKind.UNCALL_CORNERS;
        for (int i = 0; i < cornerCandlePositions.length; i++) {
            BlockPos candlePos = cornerCandlePositions[i];
            if (candlePos != null) setCandleLit(server, candlePos, shouldBeLit);
            cornerFlameStartTicks[i] = -1;
            cornerFlameEndTicks[i] = -1;
        }
        cornerStep = 0;
        cornerCompleteTick = -1;
        cornerFlameSequenceStartTick = -1;
    }

    private void finishCornerRite(ServerLevel server) {
        boolean calling = ritualKind == RitualKind.CALL_CORNERS;
        if (calling) CalledCornersSavedData.get(server).call(server, worldPosition);
        else CalledCornersSavedData.get(server).uncallNear(worldPosition, 4);

        server.playSound(null, worldPosition,
                calling ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.BLOCKS, 0.42F, calling ? 0.72F : 0.82F);
        server.sendParticles(ParticleTypes.END_ROD,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.35D, worldPosition.getZ() + 0.5D,
                8, 0.75D, 0.18D, 0.75D, 0.004D);
        ritualActive = false;
        ritualSuccessful = false;
        ritualTicks = 0;
        departureTicks = DEPARTURE_DURATION;
        clearRuntimeState();
    }

    private void finishNightRite(ServerLevel server) {
        ServerPlayer caster = getRitualCaster(server);
        if (caster != null) MagicalActEffects.impact(caster, worldPosition);

        ritualActive = false;
        ritualSuccessful = false;
        ritualTicks = 0;

        if (whiteCandlePos != null) setCandleLit(server, whiteCandlePos, false);
        if (blackCandlePos != null) setCandleLit(server, blackCandlePos, true);

        if (whiteCandlePos != null) {
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    whiteCandlePos.getX() + 0.5D, whiteCandlePos.getY() + 0.55D, whiteCandlePos.getZ() + 0.5D,
                    6, 0.08D, 0.08D, 0.08D, 0.005D);
        }
        if (blackCandlePos != null) {
            server.sendParticles(ParticleTypes.END_ROD,
                    blackCandlePos.getX() + 0.5D, blackCandlePos.getY() + 0.55D, blackCandlePos.getZ() + 0.5D,
                    5, 0.10D, 0.13D, 0.10D, 0.004D);
        }

        long day = server.getDayTime();
        long nextNight = day - Math.floorMod(day, 24000L) + 13000L;
        if (day >= nextNight) nextNight += 24000L;
        server.setDayTime(nextNight);

        server.playSound(null, worldPosition, SoundEvents.WITHER_SPAWN,
                SoundSource.BLOCKS, 0.35F, 1.6F);
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS, 1.2F, 0.55F);
        server.sendParticles(ParticleTypes.WHITE_ASH,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.34D, worldPosition.getZ() + 0.5D,
                10, 0.72D, 0.22D, 0.72D, 0.006D);
        server.sendParticles(ParticleTypes.END_ROD,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.42D, worldPosition.getZ() + 0.5D,
                4, 0.30D, 0.14D, 0.30D, 0.003D);

        departureTicks = DEPARTURE_DURATION;
        clearRuntimeState();
    }

    private void finishTemperanceRite(ServerLevel server) {
        ServerPlayer target = getTemperanceTarget(server);
        if (target != null) PlayerMagicManager.setMagic(target, MagicConstants.NATURAL_LIMIT);
        for (BlockPos candle : grayCandlePositions) {
            if (candle == null) continue;
            BlockState candleState = server.getBlockState(candle);
            if (candleState.getBlock() instanceof CandleBlock
                    && candleState.hasProperty(BlockStateProperties.LIT)) {
                server.setBlock(candle, candleState.setValue(BlockStateProperties.LIT, false), 3);
            }
        }

        ItemEntity bottle = new ItemEntity(server,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.48D,
                worldPosition.getZ() + 0.5D, new ItemStack(Items.GLASS_BOTTLE));
        ServerPlayer nearest = server.getNearestPlayer(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, 12.0D, false)
                instanceof ServerPlayer p ? p : null;
        double vx = nearest == null ? 0.0D : nearest.getX() - bottle.getX();
        double vz = nearest == null ? 0.0D : nearest.getZ() - bottle.getZ();
        double length = Math.max(0.001D, Math.sqrt(vx * vx + vz * vz));
        bottle.setDeltaMovement(vx / length * 0.16D, 0.28D, vz / length * 0.16D);
        bottle.setPickUpDelay(12);
        server.addFreshEntity(bottle);

        server.playSound(null, worldPosition, SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS, 0.62F, 1.18F);
        server.sendParticles(ParticleTypes.POOF,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.48D,
                worldPosition.getZ() + 0.5D, 7, 0.12D, 0.08D, 0.12D, 0.01D);

        clearTemperanceFlameLights(server);
        ritualActive = false;
        ritualSuccessful = false;
        ritualTicks = 0;
        departureTicks = DEPARTURE_DURATION;
        clearRuntimeState();
    }


    private void updateTemperanceFlameLights(ServerLevel server) {
        boolean shouldGlow = ritualActive
                && ritualKind == RitualKind.TEMPERANCE
                && ritualTicks >= 36
                && ritualTicks < TEMPERANCE_CANDLE_FALL_END;

        for (BlockPos candlePos : grayCandlePositions) {
            if (candlePos == null) continue;

            BlockPos lightPos = candlePos.above();
            BlockState state = server.getBlockState(lightPos);

            if (shouldGlow && ritualTicks < TEMPERANCE_CANDLE_FALL_START) {
                if (state.isAir() || state.is(Blocks.LIGHT)) {
                    BlockState lightState = Blocks.LIGHT.defaultBlockState()
                            .setValue(LightBlock.LEVEL, 14);
                    if (!state.equals(lightState)) {
                        server.setBlock(lightPos, lightState, 3);
                    }
                }
            } else if (state.is(Blocks.LIGHT)) {
                server.setBlock(lightPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private void clearTemperanceFlameLights(ServerLevel server) {
        for (BlockPos candlePos : grayCandlePositions) {
            if (candlePos == null) continue;
            BlockPos lightPos = candlePos.above();
            if (server.getBlockState(lightPos).is(Blocks.LIGHT)) {
                server.setBlock(lightPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Nullable
    private ServerPlayer getTemperanceTarget(ServerLevel server) {
        return temperanceTargetUuid == null ? null
                : server.getServer().getPlayerList().getPlayer(temperanceTargetUuid);
    }

    private void finishFailedRite(ServerLevel server) {
        clearTemperanceFlameLights(server);
        ritualActive = false;
        ritualSuccessful = false;
        ritualTicks = 0;
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.25D, worldPosition.getZ() + 0.5D,
                30, 0.65D, 0.22D, 0.65D, 0.035D);
        server.sendParticles(ParticleTypes.SNOWFLAKE,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.45D, worldPosition.getZ() + 0.5D,
                12, 0.4D, 0.22D, 0.4D, 0.015D);
        server.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                SoundSource.BLOCKS, 0.9F, 0.55F);
        for (BlockPos candle : grayCandlePositions) if (candle != null) setCandleLit(server, candle, false);
        clearRuntimeState();
    }

    @Nullable
    private ServerPlayer getRitualCaster(ServerLevel server) {
        return ritualCasterUuid == null
                ? null
                : server.getServer().getPlayerList().getPlayer(ritualCasterUuid);
    }

    private void clearRuntimeState() {
        clearOfferingSlots();
        whiteCandlePos = null;
        blackCandlePos = null;
        for (int i = 0; i < grayCandlePositions.length; i++) grayCandlePositions[i] = null;
        temperanceTargetUuid = null;
        ritualCasterUuid = null;
        temperanceStartingMagic = 0;
        for (int i = 0; i < cornerCandlePositions.length; i++) {
            cornerCandlePositions[i] = null;
            cornerFlameStartTicks[i] = -1;
            cornerFlameEndTicks[i] = -1;
        }
        cornerStep = 0;
        cornerCompleteTick = -1;
        cornerFlameSequenceStartTick = -1;
        cornerAwaitingSequence = false;
        cornerCasterUuid = null;
        transmutationClusters.clear();
        ritualKind = RitualKind.NONE;
        sync();
    }

    private void clearOfferingSlots() {
        for (int i = 0; i < offerings.size(); i++) offerings.set(i, ItemStack.EMPTY);
    }

    private void invalidPatternFeedback(ServerLevel server) {
        server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.5F, 0.7F);
        server.sendParticles(ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.15D, worldPosition.getZ() + 0.5D,
                8, 0.3D, 0.05D, 0.3D, 0.01D);
    }

    private void missingOfferingFeedback(ServerLevel server) {
        server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_HIT,
                SoundSource.BLOCKS, 0.65F, 0.65F);
        server.sendParticles(ParticleTypes.SNOWFLAKE,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.22D, worldPosition.getZ() + 0.5D,
                7, 0.25D, 0.08D, 0.25D, 0.01D);
    }

    @Nullable
    private CandlePair findValidCandles() {
        CandlePair northSouth = findValidCandlePair(Direction.NORTH, Direction.SOUTH);
        if (northSouth != null) return northSouth;
        return findValidCandlePair(Direction.EAST, Direction.WEST);
    }

    @Nullable
    private CandlePair findValidCandlePair(Direction firstDirection, Direction secondDirection) {
        BlockPos first = worldPosition.relative(firstDirection);
        BlockPos second = worldPosition.relative(secondDirection);

        if (isCandle(first, Blocks.WHITE_CANDLE, true)
                && isCandle(second, Blocks.BLACK_CANDLE, false)) {
            return new CandlePair(first, second);
        }
        if (isCandle(second, Blocks.WHITE_CANDLE, true)
                && isCandle(first, Blocks.BLACK_CANDLE, false)) {
            return new CandlePair(second, first);
        }
        return null;
    }

    private boolean isCandle(BlockPos pos, net.minecraft.world.level.block.Block candle, boolean lit) {
        if (level == null) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(candle) && state.hasProperty(CandleBlock.LIT)
                && state.getValue(CandleBlock.LIT) == lit;
    }

    private boolean hasSmallWhiteCircle() {
        if (level == null) return false;

        int[][] circleOffsets = {
                {-1, -3}, {0, -3}, {1, -3},
                {-2, -2}, {2, -2},
                {-3, -1}, {3, -1},
                {-3,  0}, {3,  0},
                {-3,  1}, {3,  1},
                {-2,  2}, {2,  2},
                {-1,  3}, {0,  3}, {1,  3}
        };

        for (int[] offset : circleOffsets) {
            BlockState state = level.getBlockState(worldPosition.offset(offset[0], 0, offset[1]));
            if (!state.is(ModBlocks.CHALK_GLYPH.get())
                    || state.getValue(ChalkGlyphBlock.TYPE) != ChalkType.WHITE) {
                return false;
            }
        }
        return true;
    }

    private static void setCandleLit(ServerLevel level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CandleBlock && state.hasProperty(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
            level.playSound(null, pos,
                    lit ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.CANDLE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.8F, lit ? 0.8F : 1.0F);
        }
    }

    public NonNullList<ItemStack> getOfferings() {
        return offerings;
    }

    public double getOfferingStartX(int slot) {
        return validSlot(slot) ? offeringStartX[slot] : 0.0D;
    }

    public double getOfferingStartY(int slot) {
        return validSlot(slot) ? offeringStartY[slot] : 0.12D;
    }

    public double getOfferingStartZ(int slot) {
        return validSlot(slot) ? offeringStartZ[slot] : 0.0D;
    }

    private boolean validSlot(int slot) {
        return slot >= 0 && slot < offerings.size();
    }

    public boolean isRitualActive() {
        return ritualActive;
    }

    public int getRitualTicks() {
        return ritualTicks;
    }

    public boolean isCornerRite() {
        return ritualKind == RitualKind.CALL_CORNERS || ritualKind == RitualKind.UNCALL_CORNERS;
    }

    public BlockPos[] getCornerCandlePositions() { return cornerCandlePositions.clone(); }

    public float getCornerFlameProgress(int index, float partialTick) {
        if (!isCornerRite() || index < 0 || index >= 4 || cornerFlameStartTicks[index] < 0) return 0.0F;
        float now = ritualTicks + partialTick;
        float grow = Mth.clamp((now - cornerFlameStartTicks[index]) / CORNER_FLAME_GROW_TICKS, 0.0F, 1.0F);
        grow = grow * grow * (3.0F - 2.0F * grow);
        if (cornerFlameEndTicks[index] >= 0) {
            float fall = Mth.clamp((now - cornerFlameEndTicks[index]) / CORNER_FLAME_FALL_TICKS, 0.0F, 1.0F);
            fall = fall * fall * (3.0F - 2.0F * fall);
            return grow * (1.0F - fall);
        }
        if (ritualKind == RitualKind.CALL_CORNERS && cornerCompleteTick >= 0) {
            float fallStart = cornerCompleteTick + CORNER_FINAL_BURN_TICKS;
            if (now >= fallStart) {
                float fall = Mth.clamp((now - fallStart) / CORNER_FLAME_FALL_TICKS, 0.0F, 1.0F);
                fall = fall * fall * (3.0F - 2.0F * fall);
                return grow * (1.0F - fall);
            }
        }
        return grow;
    }

    public boolean isEarthVeinsRitual() { return ritualKind == RitualKind.EARTH_VEINS; }

    public ItemStack getEarthVeinsPickaxe() {
        return offerings.get(EARTH_VEINS_PICKAXE_SLOT);
    }

    public float getEarthVeinsPickaxeArrivalProgress(float partialTick) {
        if (!isEarthVeinsRitual()) return 0.0F;
        float progress = Mth.clamp((ritualTicks + partialTick - EARTH_VEINS_PICKAXE_ARRIVAL_START)
                / (float) (EARTH_VEINS_PICKAXE_GROW_END - EARTH_VEINS_PICKAXE_ARRIVAL_START), 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    public float getEarthVeinsWorkProgress(float partialTick) {
        if (!isEarthVeinsRitual()) return 0.0F;
        return Mth.clamp((ritualTicks + partialTick - EARTH_VEINS_SEARCH_START)
                / (float) (EARTH_VEINS_EXTRACTION_START - EARTH_VEINS_SEARCH_START), 0.0F, 1.0F);
    }

    public float getEarthVeinsFinishProgress(float partialTick) {
        if (!isEarthVeinsRitual() || earthVeinsFinishTicks < 0) return 0.0F;
        return Mth.clamp((earthVeinsFinishTicks + partialTick)
                / (float) EARTH_VEINS_FINISH_POOF_TICK, 0.0F, 1.0F);
    }

    public float getEarthVeinsFinishElapsed(float partialTick) {
        if (!isEarthVeinsRitual() || earthVeinsFinishTicks < 0) return 0.0F;
        return Math.max(0.0F, earthVeinsFinishTicks + partialTick);
    }

    public boolean isEarthVeinsPickaxeVisible() {
        return earthVeinsFinishTicks < 0 || earthVeinsFinishTicks < EARTH_VEINS_FINISH_POOF_TICK;
    }

    public boolean isHollowingRite() {
        return ritualActive && ritualKind == RitualKind.HOLLOWING;
    }

    public UUID getHollowingTargetUuid() {
        return hollowingTargetUuid;
    }

    public int getHollowingActiveLineCount(float partialTick) {
        if (!isHollowingRite()) return 0;

        double tick = ritualTicks + partialTick;
        int count = 0;
        for (int lineTick : HOLLOWING_LINE_TICKS) {
            if (tick >= lineTick) count++;
        }
        return count;
    }

    public float getHollowingLineRevealProgress(int index, float partialTick) {
        if (!isHollowingRite()
                || index < 0
                || index >= HOLLOWING_LINE_TICKS.length) {
            return 0.0F;
        }

        double tick = ritualTicks + partialTick;
        double raw =
                (tick - HOLLOWING_LINE_TICKS[index])
                        / 12.0D;

        raw = Math.max(0.0D, Math.min(1.0D, raw));
        return (float)(raw * raw * (3.0D - 2.0D * raw));
    }

    public float getHollowingCandleFlareProgress(float partialTick) {
        if (!isHollowingRite()) return 0.0F;

        double tick = ritualTicks + partialTick;


        if (tick < 18.0D) return 0.0F;
        if (tick < 48.0D) {
            double p = (tick - 18.0D) / 30.0D;
            return (float)(p * p * (3.0D - 2.0D * p));
        }
        if (tick < 72.0D) return 1.0F;
        if (tick < 100.0D) {
            double p = (tick - 72.0D) / 28.0D;
            p = p * p * (3.0D - 2.0D * p);
            return (float)(1.0D - p * 0.72D);
        }


        if (tick < HOLLOWING_CORRUPTION_TICK) {
            double pulse =
                    0.5D
                            + 0.5D
                            * Math.sin(
                                    tick * 0.095D
                            );
            return (float)(0.22D + pulse * 0.08D);
        }


        double p =
                (tick - HOLLOWING_CORRUPTION_TICK)
                        / Math.max(
                                1.0D,
                                HOLLOWING_CANDLE_DYING_TICK
                                        + 44
                                        - HOLLOWING_CORRUPTION_TICK
                        );

        p = Math.max(0.0D, Math.min(1.0D, p));
        return (float)(0.30D * (1.0D - p));
    }

    public float getHollowingCandleDyingProgress(float partialTick) {
        if (!isHollowingRite()) return 0.0F;

        double tick = ritualTicks + partialTick;
        double p =
                (tick - HOLLOWING_CANDLE_DYING_TICK)
                        / 44.0D;

        p = Math.max(0.0D, Math.min(1.0D, p));
        return (float)p;
    }

    public float getHollowingCorruptionProgress(float partialTick) {
        if (!isHollowingRite()) return 0.0F;

        double tick = ritualTicks + partialTick;
        double raw =
                (tick - HOLLOWING_CORRUPTION_TICK)
                        / Math.max(
                                1.0D,
                                RitualPatterns.HOLLOWING.duration()
                                        - HOLLOWING_CORRUPTION_TICK
                        );

        raw = Math.max(0.0D, Math.min(1.0D, raw));
        return (float)(raw * raw * (3.0D - 2.0D * raw));
    }

    public BlockPos[] getHollowingGlimmerstonePositions() {
        BlockPos[] positions =
                new BlockPos[
                        RitualPatterns.HOLLOWING_GLIMMERSTONES.length
                ];

        for (int i = 0; i < positions.length; i++) {
            int[] offset =
                    RitualPatterns.HOLLOWING_GLIMMERSTONES[i];

            positions[i] =
                    worldPosition.offset(
                            offset[0],
                            0,
                            offset[1]
                    );
        }

        return positions;
    }

    public boolean isTemperanceRite() {
        return ritualKind == RitualKind.TEMPERANCE;
    }

    @Nullable
    public UUID getTemperanceTargetUuid() {
        return temperanceTargetUuid;
    }

    public boolean isTemperanceExtractionActive() {
        return ritualActive
                && ritualKind == RitualKind.TEMPERANCE
                && ritualTicks >= TEMPERANCE_EXTRACTION_START

                && ritualTicks < TEMPERANCE_CANDLE_FALL_START;
    }


    public BlockPos[] getGrayCandlePositions() {
        return grayCandlePositions.clone();
    }

    public float getTemperanceCandleRiseProgress(float partialTick) {
        float ticks = ritualTicks + partialTick;


        final float growStart = 36.0F;
        final float growEnd = growStart
                + (TEMPERANCE_CANDLE_FALL_END - TEMPERANCE_CANDLE_FALL_START);

        if (ticks < growStart) {
            return 0.0F;
        }

        if (ticks >= TEMPERANCE_CANDLE_FALL_START) {
            float fallProgress = Mth.clamp(
                    (ticks - TEMPERANCE_CANDLE_FALL_START)
                            / (float) (TEMPERANCE_CANDLE_FALL_END - TEMPERANCE_CANDLE_FALL_START),
                    0.0F,
                    1.0F
            );
            float easedFall = fallProgress * fallProgress * (3.0F - 2.0F * fallProgress);
            return 1.0F - easedFall;
        }

        if (ticks < growEnd) {
            float growProgress = Mth.clamp(
                    (ticks - growStart) / (growEnd - growStart),
                    0.0F,
                    1.0F
            );
            return growProgress * growProgress * (3.0F - 2.0F * growProgress);
        }

        return 1.0F;
    }

    public float getTemperanceExtractionProgress(float partialTick) {
        float ticks = ritualTicks + partialTick;
        return Mth.clamp((ticks - TEMPERANCE_EXTRACTION_START)
                / (float) TEMPERANCE_BEAM_CONNECT_TICKS, 0.0F, 1.0F);
    }


    public float getTemperanceBeamDetachProgress(float partialTick) {
        float ticks = ritualTicks + partialTick;
        return Mth.clamp((ticks - TEMPERANCE_EXTRACTION_END)
                / (float) (TEMPERANCE_CANDLE_FALL_START - TEMPERANCE_EXTRACTION_END),
                0.0F, 1.0F);
    }

    public boolean isTransmutationRitual() {
        return ritualKind == RitualKind.TRANSMUTATION;
    }

    public BlockPos[] getTransmutationCandlePositions() {
        BlockPos[] result = new BlockPos[RitualPatterns.TRANSMUTATION_CANDLE_RING.length];
        for (int i = 0; i < result.length; i++) {
            int[] offset = RitualPatterns.TRANSMUTATION_CANDLE_RING[i];
            result[i] = worldPosition.offset(offset[0], 0, offset[1]);
        }
        return result;
    }


    public float getTransmutationLargeFlameProgress(float partialTick) {
        if (!isTransmutationRitual()) return 0.0F;
        float ticks = ritualTicks + partialTick;

        if (ticks < TRANSMUTATION_FLAME_GROW_START
                || ticks >= TRANSMUTATION_FLAME_NORMAL_TICK) {
            return 0.0F;
        }

        if (ticks < TRANSMUTATION_FLAME_FULL_TICK) {
            float p = Mth.clamp(
                    (ticks - TRANSMUTATION_FLAME_GROW_START)
                            / (float) (TRANSMUTATION_FLAME_FULL_TICK - TRANSMUTATION_FLAME_GROW_START),
                    0.0F, 1.0F
            );
            return p * p * (3.0F - 2.0F * p);
        }

        if (ticks < TRANSMUTATION_FLAME_SHRINK_START) {
            return 1.0F;
        }

        float p = Mth.clamp(
                (ticks - TRANSMUTATION_FLAME_SHRINK_START)
                        / (float) (TRANSMUTATION_FLAME_NORMAL_TICK - TRANSMUTATION_FLAME_SHRINK_START),
                0.0F, 1.0F
        );
        float eased = p * p * (3.0F - 2.0F * p);
        return 1.0F - eased;
    }

    public int getOfferingPoofTick(int slot) {
        if (ritualKind == RitualKind.EARTH_VEINS) {
            if (slot == EARTH_VEINS_PICKAXE_SLOT) return EARTH_VEINS_PICKAXE_ARRIVAL_START;
            return slot >= 0 && slot < EARTH_VEINS_OFFERING_POOF_TICKS.length
                    ? EARTH_VEINS_OFFERING_POOF_TICKS[slot] : EARTH_VEINS_MAX_DURATION;
        }
        if (ritualKind == RitualKind.TRANSMUTATION) {
            return slot >= 0 && slot < TRANSMUTATION_POOF_TICKS.length
                    ? TRANSMUTATION_POOF_TICKS[slot] : 1200;
        }
        if (ritualKind == RitualKind.TEMPERANCE) {
            return slot >= 0 && slot < TEMPERANCE_POOF_TICKS.length
                    ? TEMPERANCE_POOF_TICKS[slot] : RitualPatterns.TEMPERANCE.duration();
        }
        if (ritualKind == RitualKind.CALL_CORNERS || ritualKind == RitualKind.UNCALL_CORNERS) {
            return slot >= 0 && slot < CORNER_OFFERING_POOF_TICKS.length
                    ? CORNER_OFFERING_POOF_TICKS[slot] : CORNER_READY_TICK;
        }
        return slot == 0 ? FIRST_OFFERING_POOF_TICK : SECOND_OFFERING_POOF_TICK;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static double smoothStep(double value) {
        double t = clamp(value);
        return t * t * (3.0D - 2.0D * t);
    }

    private void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < offerings.size(); i++) {
            if (!offerings.get(i).isEmpty()) {
                tag.put("Offering" + i, offerings.get(i).save(new CompoundTag()));
            }
            tag.putDouble("OfferingStartX" + i, offeringStartX[i]);
            tag.putDouble("OfferingStartY" + i, offeringStartY[i]);
            tag.putDouble("OfferingStartZ" + i, offeringStartZ[i]);
        }
        tag.putBoolean("RitualActive", ritualActive);
        tag.putBoolean("RitualSuccessful", ritualSuccessful);
        tag.putInt("RitualTicks", ritualTicks);
        tag.putInt("DepartureTicks", departureTicks);
        tag.putString("RitualKind", ritualKind.name());
        if (!woodsFavorWoodId.isBlank()) tag.putString("WoodsFavorWood", woodsFavorWoodId);
        tag.putInt("TemperanceStartingMagic", temperanceStartingMagic);
        tag.putInt("HollowingStartingMagic", hollowingStartingMagic);
        if (hollowingTargetUuid != null) tag.putUUID("HollowingTarget", hollowingTargetUuid);
        tag.putString("EarthVeinTarget", earthVeinTarget.name());
        tag.putInt("EarthVeinsExtracted", earthVeinsExtracted);
        tag.putInt("EarthVeinsMaximum", earthVeinsMaximum);
        tag.putInt("EarthVeinsOfferingCount", earthVeinsOfferingCount);
        tag.putInt("EarthVeinsPulseInterval", earthVeinsPulseInterval);
        tag.putInt("EarthVeinsSearchRadius", earthVeinsSearchRadius);
        tag.putInt("EarthVeinsFinishTicks", earthVeinsFinishTicks);
        tag.putInt("TransmutationClusterCount", transmutationClusters.size());
        for (int i = 0; i < transmutationClusters.size(); i++) {
            tag.putLong("TransmutationCluster" + i, transmutationClusters.get(i).asLong());
        }
        if (temperanceTargetUuid != null) tag.putUUID("TemperanceTarget", temperanceTargetUuid);
        if (ritualCasterUuid != null) tag.putUUID("RitualCaster", ritualCasterUuid);
        for (int i = 0; i < grayCandlePositions.length; i++) if (grayCandlePositions[i] != null) tag.putLong("GrayCandle" + i, grayCandlePositions[i].asLong());
        for (int i = 0; i < cornerCandlePositions.length; i++) {
            if (cornerCandlePositions[i] != null) tag.putLong("CornerCandle" + i, cornerCandlePositions[i].asLong());
            tag.putInt("CornerFlameStart" + i, cornerFlameStartTicks[i]);
            tag.putInt("CornerFlameEnd" + i, cornerFlameEndTicks[i]);
        }
        tag.putInt("CornerStep", cornerStep);
        tag.putInt("CornerCompleteTick", cornerCompleteTick);
        tag.putInt("CornerFlameSequenceStartTick", cornerFlameSequenceStartTick);
        tag.putBoolean("CornerAwaiting", cornerAwaitingSequence);
        if (cornerCasterUuid != null) tag.putUUID("CornerCaster", cornerCasterUuid);
        if (whiteCandlePos != null) tag.putLong("WhiteCandle", whiteCandlePos.asLong());
        if (blackCandlePos != null) tag.putLong("BlackCandle", blackCandlePos.asLong());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < offerings.size(); i++) {
            offerings.set(i, tag.contains("Offering" + i)
                    ? ItemStack.of(tag.getCompound("Offering" + i)) : ItemStack.EMPTY);
            offeringStartX[i] = tag.getDouble("OfferingStartX" + i);
            offeringStartY[i] = tag.contains("OfferingStartY" + i)
                    ? tag.getDouble("OfferingStartY" + i) : 0.12D;
            offeringStartZ[i] = tag.getDouble("OfferingStartZ" + i);
        }
        ritualActive = tag.getBoolean("RitualActive");
        ritualSuccessful = tag.getBoolean("RitualSuccessful");
        ritualTicks = tag.getInt("RitualTicks");
        departureTicks = tag.getInt("DepartureTicks");
        try { ritualKind = RitualKind.valueOf(tag.getString("RitualKind")); }
        catch (IllegalArgumentException ignored) { ritualKind = RitualKind.NONE; }
        woodsFavorWoodId = tag.getString("WoodsFavorWood");
        temperanceStartingMagic = tag.getInt("TemperanceStartingMagic");
        hollowingStartingMagic = tag.getInt("HollowingStartingMagic");
        hollowingTargetUuid = tag.hasUUID("HollowingTarget") ? tag.getUUID("HollowingTarget") : null;
        try { earthVeinTarget = EarthVeinTarget.valueOf(tag.getString("EarthVeinTarget")); }
        catch (IllegalArgumentException ignored) { earthVeinTarget = EarthVeinTarget.NONE; }
        earthVeinsExtracted = tag.getInt("EarthVeinsExtracted");
        earthVeinsMaximum = tag.getInt("EarthVeinsMaximum");
        earthVeinsOfferingCount = tag.contains("EarthVeinsOfferingCount")
                ? tag.getInt("EarthVeinsOfferingCount") : 0;
        earthVeinsPulseInterval = tag.contains("EarthVeinsPulseInterval") ? tag.getInt("EarthVeinsPulseInterval") : 28;
        earthVeinsSearchRadius = tag.contains("EarthVeinsSearchRadius") ? tag.getInt("EarthVeinsSearchRadius") : 16;
        earthVeinsFinishTicks = tag.contains("EarthVeinsFinishTicks") ? tag.getInt("EarthVeinsFinishTicks") : -1;
        transmutationClusters.clear();
        int transmutationCount = tag.getInt("TransmutationClusterCount");
        for (int i = 0; i < transmutationCount; i++) {
            if (tag.contains("TransmutationCluster" + i)) {
                transmutationClusters.add(BlockPos.of(tag.getLong("TransmutationCluster" + i)));
            }
        }
        temperanceTargetUuid = tag.hasUUID("TemperanceTarget") ? tag.getUUID("TemperanceTarget") : null;
        ritualCasterUuid = tag.hasUUID("RitualCaster") ? tag.getUUID("RitualCaster") : null;
        for (int i = 0; i < grayCandlePositions.length; i++)
            grayCandlePositions[i] = tag.contains("GrayCandle" + i) ? BlockPos.of(tag.getLong("GrayCandle" + i)) : null;
        for (int i = 0; i < cornerCandlePositions.length; i++) {
            cornerCandlePositions[i] = tag.contains("CornerCandle" + i) ? BlockPos.of(tag.getLong("CornerCandle" + i)) : null;
            cornerFlameStartTicks[i] = tag.contains("CornerFlameStart" + i) ? tag.getInt("CornerFlameStart" + i) : -1;
            cornerFlameEndTicks[i] = tag.contains("CornerFlameEnd" + i) ? tag.getInt("CornerFlameEnd" + i) : -1;
        }
        cornerStep = tag.getInt("CornerStep");
        cornerCompleteTick = tag.contains("CornerCompleteTick") ? tag.getInt("CornerCompleteTick") : -1;
        cornerFlameSequenceStartTick = tag.contains("CornerFlameSequenceStartTick")
                ? tag.getInt("CornerFlameSequenceStartTick") : -1;
        cornerAwaitingSequence = tag.getBoolean("CornerAwaiting");
        cornerCasterUuid = tag.hasUUID("CornerCaster") ? tag.getUUID("CornerCaster") : null;
        whiteCandlePos = tag.contains("WhiteCandle") ? BlockPos.of(tag.getLong("WhiteCandle")) : null;
        blackCandlePos = tag.contains("BlackCandle") ? BlockPos.of(tag.getLong("BlackCandle")) : null;
    }


    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(10.0D, 4.0D, 10.0D);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }

    private enum RitualKind { NONE, NIGHT, TEMPERANCE, HOLLOWING, TRANSMUTATION, EARTH_VEINS, WOODS_FAVOR, CALL_CORNERS, UNCALL_CORNERS }

    private enum EarthVeinTarget {
        NONE("nothing"), COAL("coal"), IRON("iron"), COPPER("copper"), GOLD("gold"),
        REDSTONE("redstone"), LAPIS("lapis"), DIAMOND("diamond"), EMERALD("emerald");

        private final String displayName;
        EarthVeinTarget(String displayName) { this.displayName = displayName; }

        static EarthVeinTarget fromCalling(ItemStack stack) {
            if (stack.is(Items.COAL)) return COAL;
            if (stack.is(Items.IRON_INGOT)) return IRON;
            if (stack.is(Items.COPPER_INGOT)) return COPPER;
            if (stack.is(Items.GOLD_INGOT)) return GOLD;
            if (stack.is(Items.REDSTONE)) return REDSTONE;
            if (stack.is(Items.LAPIS_LAZULI)) return LAPIS;
            if (stack.is(Items.DIAMOND)) return DIAMOND;
            if (stack.is(Items.EMERALD)) return EMERALD;
            return NONE;
        }

        ItemStack extractedResult() {
            return switch (this) {
                case COAL -> new ItemStack(Items.COAL);
                case IRON -> new ItemStack(Items.RAW_IRON);
                case COPPER -> new ItemStack(Items.RAW_COPPER);
                case GOLD -> new ItemStack(Items.RAW_GOLD);
                case REDSTONE -> new ItemStack(Items.REDSTONE);
                case LAPIS -> new ItemStack(Items.LAPIS_LAZULI);
                case DIAMOND -> new ItemStack(Items.DIAMOND);
                case EMERALD -> new ItemStack(Items.EMERALD);
                default -> ItemStack.EMPTY;
            };
        }

        boolean matches(BlockState state) {
            return switch (this) {
                case COAL -> state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
                case IRON -> state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE);
                case COPPER -> state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE);
                case GOLD -> state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE);
                case REDSTONE -> state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE);
                case LAPIS -> state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE);
                case DIAMOND -> state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
                case EMERALD -> state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE);
                default -> false;
            };
        }

        BlockState hostBlock(BlockState ore) {
            return (ore.is(Blocks.DEEPSLATE_COAL_ORE) || ore.is(Blocks.DEEPSLATE_IRON_ORE)
                    || ore.is(Blocks.DEEPSLATE_COPPER_ORE) || ore.is(Blocks.DEEPSLATE_GOLD_ORE)
                    || ore.is(Blocks.DEEPSLATE_REDSTONE_ORE) || ore.is(Blocks.DEEPSLATE_LAPIS_ORE)
                    || ore.is(Blocks.DEEPSLATE_DIAMOND_ORE) || ore.is(Blocks.DEEPSLATE_EMERALD_ORE))
                    ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }
    }
    private record CandlePair(BlockPos white, BlockPos black) {}
    private record OfferingPair(ItemEntity daisy, ItemEntity moonlight) {}
    private record TemperanceSetup(ItemEntity blood, ItemEntity pulse, ItemEntity salt,
                                    ItemEntity sage, ItemEntity moonlight,
                                    BlockPos[] grayCandles, ServerPlayer target) {}
}
