package net.toxicteddie.witchcraft.power.fire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.effect.MagicalActEffects;
import net.toxicteddie.witchcraft.block.entity.ChalkGlyphBlockEntity;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.PlayerElementPowerManager;
import net.toxicteddie.witchcraft.power.PlayerElementSelectionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class FirePowerManager {
    private static final Map<UUID, Integer> PRIMARY_HELD_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> SECONDARY_HELD_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> RADIAL_IGNITE_HELD_TICKS = new HashMap<>();

    private static final int HOLD_THRESHOLD_TICKS = 25;
    private static final int FLAME_STREAM_INTERVAL_TICKS = 7;
    private static final int MAX_EXTINGUISH_CHARGE_TICKS = 25;
    private static final int MAX_RADIAL_IGNITE_CHARGE_TICKS = 25;

    private static final double IGNITE_RANGE = 6.0D;
    private static final int IGNITE_MAGIC_COST = 2;
    private static final int IGNITE_FIRE_COST = 2;

    private static final double FLAME_STREAM_RANGE = 12.0D;
    private static final double FLAME_STREAM_RADIUS = 0.72D;
    private static final int FLAME_STREAM_MAGIC_COST = 3;
    private static final int FLAME_STREAM_FIRE_COST = 4;
    private static final int FLAME_STREAM_BURN_SECONDS = 4;


    private static final double FLAME_STREAM_REAL_FIRE_CHANCE = 0.22D;
    private static final int FLAME_STREAM_MAX_REAL_FIRE_PER_PULSE = 2;


    private static final int FLAME_STREAM_PARTICLES_PER_TICK = 10;
    private static final int FLAME_STREAM_SMOKE_PER_TICK = 2;
    private static final double FLAME_STREAM_PARTICLE_SPEED = 0.31D;
    private static final double FLAME_STREAM_PARTICLE_SPREAD = 0.052D;

    private static final int RADIAL_IGNITE_MIN_FIRE_COST = 5;
    private static final int RADIAL_IGNITE_MAX_FIRE_COST = 18;
    private static final double RADIAL_IGNITE_MIN_RADIUS = 2.0D;
    private static final double RADIAL_IGNITE_MAX_RADIUS = 8.5D;

    private static final double LIGHTNING_RANGE = 24.0D;
    private static final int LIGHTNING_MAGIC_COST = 12;
    private static final int LIGHTNING_FIRE_COST = 16;

    private static final double EXTINGUISH_RANGE = 8.0D;
    private static final int EXTINGUISH_MAGIC_COST = 2;
    private static final int EXTINGUISH_FIRE_COST = 2;

    private static final int GREATER_MIN_MAGIC_COST = 4;
    private static final int GREATER_MAX_MAGIC_COST = 14;
    private static final int GREATER_MIN_FIRE_COST = 5;
    private static final int GREATER_MAX_FIRE_COST = 18;
    private static final double GREATER_MIN_RADIUS = 2.0D;
    private static final double GREATER_MAX_RADIUS = 8.5D;

    private FirePowerManager() {}

    public static void beginPrimary(ServerPlayer player) {
        if (!canUseFirePower(player)) return;
        UUID id = player.getUUID();
        SECONDARY_HELD_TICKS.remove(id);
        RADIAL_IGNITE_HELD_TICKS.remove(id);
        PRIMARY_HELD_TICKS.put(id, 0);
    }

    public static void releasePrimary(ServerPlayer player) {
        Integer heldTicks = PRIMARY_HELD_TICKS.remove(player.getUUID());
        if (heldTicks == null || !canUseFirePower(player)) return;
        if (heldTicks < HOLD_THRESHOLD_TICKS) ignite(player);
    }

    public static void beginSecondary(ServerPlayer player) {
        if (!canUseFirePower(player)) return;
        UUID id = player.getUUID();
        PRIMARY_HELD_TICKS.remove(id);
        RADIAL_IGNITE_HELD_TICKS.remove(id);
        SECONDARY_HELD_TICKS.put(id, 0);
    }

    public static void releaseSecondary(ServerPlayer player) {
        Integer heldTicks = SECONDARY_HELD_TICKS.remove(player.getUUID());
        if (heldTicks == null || !canUseFirePower(player)) return;
        if (heldTicks < HOLD_THRESHOLD_TICKS) {
            extinguish(player);
        } else {
            greaterExtinguish(player, heldTicks);
        }
    }

    public static void beginRadialIgnite(ServerPlayer player) {
        if (!canUseFirePower(player)) return;
        UUID id = player.getUUID();
        PRIMARY_HELD_TICKS.remove(id);
        SECONDARY_HELD_TICKS.remove(id);
        RADIAL_IGNITE_HELD_TICKS.put(id, 0);
    }

    public static void releaseRadialIgnite(ServerPlayer player) {
        Integer heldTicks = RADIAL_IGNITE_HELD_TICKS.remove(player.getUUID());
        if (heldTicks == null || !canUseFirePower(player)) return;
        radialIgnite(player, heldTicks);
    }

    public static void stormLightning(ServerPlayer player) {
        if (!canUseFirePower(player) || !player.serverLevel().isThundering()) return;
        BlockHitResult hit = blockRaycast(player, LIGHTNING_RANGE);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos strikePos = hit.getBlockPos().relative(hit.getDirection());
        ServerLevel level = player.serverLevel();
        if (!level.canSeeSky(strikePos)) return;
        if (!hasResources(player, LIGHTNING_MAGIC_COST, LIGHTNING_FIRE_COST)) return;

        spend(player, LIGHTNING_MAGIC_COST, LIGHTNING_FIRE_COST);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
        bolt.setCause(player);
        level.addFreshEntity(bolt);
        MagicalActEffects.impact(player, strikePos);
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();

        if (PRIMARY_HELD_TICKS.containsKey(id)) {
            if (!canUseFirePower(player) || !player.isAlive()) {
                PRIMARY_HELD_TICKS.remove(id);
            } else {
                int ticks = PRIMARY_HELD_TICKS.get(id) + 1;
                PRIMARY_HELD_TICKS.put(id, ticks);

                if (ticks >= HOLD_THRESHOLD_TICKS) {


                    spawnFlameStreamVisual(player);


                    if (ticks == HOLD_THRESHOLD_TICKS
                            || (ticks - HOLD_THRESHOLD_TICKS) % FLAME_STREAM_INTERVAL_TICKS == 0) {
                        if (!flameStreamPulse(player)) {
                            PRIMARY_HELD_TICKS.remove(id);
                        }
                    }
                }
            }
        }

        if (SECONDARY_HELD_TICKS.containsKey(id)) {
            if (!canUseFirePower(player) || !player.isAlive()) {
                SECONDARY_HELD_TICKS.remove(id);
            } else {
                SECONDARY_HELD_TICKS.computeIfPresent(id,
                        (ignored, ticks) -> Math.min(MAX_EXTINGUISH_CHARGE_TICKS, ticks + 1));
            }
        }

        if (RADIAL_IGNITE_HELD_TICKS.containsKey(id)) {
            if (!canUseFirePower(player) || !player.isAlive()) {
                RADIAL_IGNITE_HELD_TICKS.remove(id);
            } else {
                RADIAL_IGNITE_HELD_TICKS.computeIfPresent(id,
                        (ignored, ticks) -> Math.min(MAX_RADIAL_IGNITE_CHARGE_TICKS, ticks + 1));
            }
        }
    }

    private static void ignite(ServerPlayer player) {
        Entity target = findEntityTarget(player, IGNITE_RANGE);
        if (target != null) {
            if (!hasResources(player, IGNITE_MAGIC_COST, IGNITE_FIRE_COST)) return;
            spend(player, IGNITE_MAGIC_COST, IGNITE_FIRE_COST);
            target.setSecondsOnFire(6);
            fireParticles(player.serverLevel(), target.getBoundingBox().getCenter(), 10);
            MagicalActEffects.impact(player);
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS, 0.65F, 1.08F);
            return;
        }

        BlockHitResult hit = blockRaycast(player, IGNITE_RANGE);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        if (!hasResources(player, IGNITE_MAGIC_COST, IGNITE_FIRE_COST)) return;
        ServerLevel level = player.serverLevel();
        BlockPos clicked = hit.getBlockPos();
        BlockState state = level.getBlockState(clicked);
        boolean changed = false;

        if (igniteImpactedBlock(player, clicked)) {
            changed = true;
        } else {
            BlockPos firePos = clicked.relative(hit.getDirection());
            BlockState fire = Blocks.FIRE.defaultBlockState();
            if (level.getBlockState(firePos).canBeReplaced()
                    && fire.canSurvive(level, firePos)) {
                level.setBlock(firePos, fire, 3);
                clicked = firePos;
                changed = true;
            }
        }

        if (!changed) return;
        spend(player, IGNITE_MAGIC_COST, IGNITE_FIRE_COST);
        fireParticles(level, Vec3.atCenterOf(clicked), 12);
        MagicalActEffects.impact(player, clicked);
        level.playSound(null, clicked, SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.72F, 1.0F);
    }


    private static boolean flameStreamPulse(ServerPlayer player) {
        if (!hasResources(player, FLAME_STREAM_MAGIC_COST, FLAME_STREAM_FIRE_COST)) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        BlockHitResult hit = blockRaycast(player, FLAME_STREAM_RANGE);
        double distance = hit.getType() == HitResult.Type.BLOCK
                ? start.distanceTo(hit.getLocation())
                : FLAME_STREAM_RANGE;
        Vec3 end = start.add(look.scale(distance));

        spend(player, FLAME_STREAM_MAGIC_COST, FLAME_STREAM_FIRE_COST);


        AABB search = player.getBoundingBox()
                .expandTowards(look.scale(distance))
                .inflate(FLAME_STREAM_RADIUS);

        for (Entity entity : level.getEntities(player, search, FirePowerManager::isValidTarget)) {
            Vec3 center = entity.getBoundingBox().getCenter();
            double along = center.subtract(start).dot(look);
            if (along < 0.0D || along > distance) continue;

            Vec3 nearest = start.add(look.scale(along));
            double allowed = FLAME_STREAM_RADIUS + entity.getBbWidth() * 0.45D;
            if (center.distanceToSqr(nearest) > allowed * allowed) continue;

            entity.setSecondsOnFire(FLAME_STREAM_BURN_SECONDS);
        }


        int samples = Math.max(1, (int) Math.ceil(distance / 0.28D));
        BlockPos last = null;
        int realFirePlaced = 0;
        net.minecraft.util.RandomSource placementRandom = level.getRandom();

        for (int i = 0; i <= samples; i++) {
            double d = distance * i / samples;
            Vec3 point = start.add(look.scale(d));
            BlockPos pos = BlockPos.containing(point);

            if (last == null || !last.equals(pos)) {
                igniteLitStateAlongStream(player, pos);

                if (realFirePlaced < FLAME_STREAM_MAX_REAL_FIRE_PER_PULSE
                        && placementRandom.nextDouble() < FLAME_STREAM_REAL_FIRE_CHANCE
                        && tryPlaceStreamFire(player, level, pos)) {
                    realFirePlaced++;
                }

                last = pos;
            }
        }


        if (hit.getType() == HitResult.Type.BLOCK) {
            igniteImpactedBlock(player, hit.getBlockPos());
        }

        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS, 0.42F, 1.12F);
        return true;
    }


    private static Vec3 getCastingHandPosition(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();


        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 1.0E-6D) {
            flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatLook = flatLook.normalize();
        }

        Vec3 right = new Vec3(-flatLook.z, 0.0D, flatLook.x);
        double side = player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT
                ? 0.31D
                : -0.31D;


        return player.getEyePosition()
                .add(look.scale(0.72D))
                .add(right.scale(side))
                .add(0.0D, -0.33D, 0.0D);
    }

    private static void spawnFlameStreamVisual(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 hand = getCastingHandPosition(player);
        net.minecraft.util.RandomSource random = level.getRandom();


        final int pathLayers = 9;
        final double visualLength = 6.5D;

        for (int layer = 0; layer < pathLayers; layer++) {
            double progress = layer / (double) (pathLayers - 1);
            double distance = 0.18D + progress * visualLength;

            Vec3 center = hand.add(look.scale(distance));


            double spread = 0.018D + progress * 0.095D;
            int particlesHere = layer < 2 ? 3 : 2;

            for (int i = 0; i < particlesHere; i++) {
                double ox = random.nextGaussian() * spread;
                double oy = random.nextGaussian() * spread;
                double oz = random.nextGaussian() * spread;


                double speed = 0.035D + progress * 0.045D;

                level.sendParticles(
                        ((layer + i) % 5 == 0)
                                ? ParticleTypes.SMALL_FLAME
                                : ParticleTypes.FLAME,
                        center.x + ox,
                        center.y + oy,
                        center.z + oz,
                        0,
                        look.x * speed + random.nextGaussian() * 0.008D,
                        look.y * speed + random.nextGaussian() * 0.008D,
                        look.z * speed + random.nextGaussian() * 0.008D,
                        1.0D
                );
            }


            if (layer >= 3 && (layer & 1) == 1) {
                level.sendParticles(
                        ParticleTypes.SMOKE,
                        center.x,
                        center.y,
                        center.z,
                        0,
                        look.x * 0.018D,
                        look.y * 0.018D + 0.006D,
                        look.z * 0.018D,
                        1.0D
                );
            }
        }


        level.sendParticles(
                ParticleTypes.SMALL_FLAME,
                hand.x, hand.y, hand.z,
                2,
                0.025D, 0.025D, 0.025D,
                0.01D
        );
    }


    private static boolean tryPlaceStreamFire(
            ServerPlayer caster,
            ServerLevel level,
            BlockPos samplePos
    ) {
        BlockState fire = Blocks.FIRE.defaultBlockState();

        if (canPlaceStreamFireAt(caster, level, samplePos, fire)) {
            level.setBlock(samplePos, fire, 3);
            return true;
        }

        for (Direction direction : Direction.values()) {
            BlockPos candidate = samplePos.relative(direction);


            if (canPlaceStreamFireAt(caster, level, candidate, fire)) {
                level.setBlock(candidate, fire, 3);
                return true;
            }
        }

        return false;
    }

    private static boolean canPlaceStreamFireAt(
            ServerPlayer caster,
            ServerLevel level,
            BlockPos pos,
            BlockState fire
    ) {
        BlockState current = level.getBlockState(pos);


        AABB protectedArea = caster.getBoundingBox().inflate(1.25D, 0.75D, 1.25D);
        AABB candidateBox = new AABB(pos);

        if (protectedArea.intersects(candidateBox)) {
            return false;
        }

        return current.canBeReplaced()
                && !current.is(Blocks.FIRE)
                && !current.is(Blocks.SOUL_FIRE)
                && fire.canSurvive(level, pos);
    }

    private static void igniteLitStateAlongStream(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockStateProperties.LIT)
                || state.getValue(BlockStateProperties.LIT)) {
            return;
        }


        if (notifyCornerRitualOfMagicalIgnition(player, pos)) {
            return;
        }

        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
    }


    private static boolean igniteImpactedBlock(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockStateProperties.LIT)
                || state.getValue(BlockStateProperties.LIT)) {
            return false;
        }

        if (notifyCornerRitualOfMagicalIgnition(player, pos)) {
            return true;
        }

        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS, 0.72F, 0.92F);
        fireParticles(level, Vec3.atCenterOf(pos), 8);
        return true;
    }

    private static boolean notifyCornerRitualOfMagicalIgnition(ServerPlayer player, BlockPos candlePos) {
        ServerLevel level = player.serverLevel();
        BlockPos min = candlePos.offset(-9, -2, -9);
        BlockPos max = candlePos.offset(9, 2, 9);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(cursor) instanceof ChalkGlyphBlockEntity glyph
                    && glyph.handleCornerCandleMagicIgnition(player, candlePos)) {
                return true;
            }
        }
        return false;
    }

    private static void extinguish(ServerPlayer player) {
        Entity target = findEntityTarget(player, EXTINGUISH_RANGE);
        if (target != null && target.isOnFire()) {
            if (!hasResources(player, EXTINGUISH_MAGIC_COST, EXTINGUISH_FIRE_COST)) return;
            spend(player, EXTINGUISH_MAGIC_COST, EXTINGUISH_FIRE_COST);
            target.clearFire();
            extinguishParticles(player.serverLevel(), target.getBoundingBox().getCenter(), 12);
            MagicalActEffects.impact(player);
            return;
        }

        BlockHitResult hit = blockRaycast(player, EXTINGUISH_RANGE);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        if (!canExtinguish(player.serverLevel(), pos)) return;
        if (!hasResources(player, EXTINGUISH_MAGIC_COST, EXTINGUISH_FIRE_COST)) return;

        spend(player, EXTINGUISH_MAGIC_COST, EXTINGUISH_FIRE_COST);
        extinguishAt(player.serverLevel(), pos);
        extinguishParticles(player.serverLevel(), Vec3.atCenterOf(pos), 12);
        MagicalActEffects.impact(player, pos);
    }

    private static void radialIgnite(ServerPlayer player, int heldTicks) {
        float charge = Math.min(1.0F,
                heldTicks / (float) MAX_RADIAL_IGNITE_CHARGE_TICKS);

        int fireCost = lerpCost(
                RADIAL_IGNITE_MIN_FIRE_COST,
                RADIAL_IGNITE_MAX_FIRE_COST,
                charge
        );

        if (!hasResources(player, 0, fireCost)) return;

        double radius = lerp(
                RADIAL_IGNITE_MIN_RADIUS,
                RADIAL_IGNITE_MAX_RADIUS,
                charge
        );

        ServerLevel level = player.serverLevel();
        Vec3 center = player.position().add(
                0.0D,
                player.getBbHeight() * 0.5D,
                0.0D
        );
        double verticalReach = 3.5D;

        java.util.List<BlockPos> targets = new java.util.ArrayList<>();

        BlockPos min = BlockPos.containing(
                center.x - radius,
                center.y - verticalReach,
                center.z - radius
        );
        BlockPos max = BlockPos.containing(
                center.x + radius,
                center.y + verticalReach,
                center.z + radius
        );

        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            Vec3 blockCenter = Vec3.atCenterOf(cursor);
            double dx = blockCenter.x - center.x;
            double dz = blockCenter.z - center.z;

            if (dx * dx + dz * dz > radius * radius) continue;
            if (Math.abs(blockCenter.y - center.y) > verticalReach) continue;

            BlockState state = level.getBlockState(cursor);
            if (state.hasProperty(BlockStateProperties.LIT)
                    && !state.getValue(BlockStateProperties.LIT)) {
                targets.add(cursor.immutable());
            }
        }

        if (targets.isEmpty()) return;

        spend(player, 0, fireCost);


        for (BlockPos pos : targets) {
            if (!notifyCornerRitualOfMagicalIgnition(player, pos)) {
                BlockState state = level.getBlockState(pos);
                if (state.hasProperty(BlockStateProperties.LIT)
                        && !state.getValue(BlockStateProperties.LIT)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
                }
            }

            fireParticles(level, Vec3.atCenterOf(pos), 5);
        }


        int points = 36 + Math.round(28.0F * charge);
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3.0D;

            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0D * i / points;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;

                level.sendParticles(
                        ParticleTypes.SMALL_FLAME,
                        x, center.y - 0.35D, z,
                        1,
                        0.02D, 0.035D, 0.02D,
                        0.0D
                );
            }
        }

        MagicalActEffects.impact(player);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.82F, 0.78F);
    }

    private static void greaterExtinguish(ServerPlayer player, int heldTicks) {
        float charge = Math.min(1.0F, heldTicks / (float) MAX_EXTINGUISH_CHARGE_TICKS);
        int magicCost = lerpCost(GREATER_MIN_MAGIC_COST, GREATER_MAX_MAGIC_COST, charge);
        int fireCost = lerpCost(GREATER_MIN_FIRE_COST, GREATER_MAX_FIRE_COST, charge);
        if (!hasResources(player, magicCost, fireCost)) return;

        double radius = lerp(GREATER_MIN_RADIUS, GREATER_MAX_RADIUS, charge);
        double verticalReach = 3.5D;
        ServerLevel level = player.serverLevel();


        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);

        int changed = 0;
        BlockPos min = BlockPos.containing(center.x - radius, center.y - verticalReach, center.z - radius);
        BlockPos max = BlockPos.containing(center.x + radius, center.y + verticalReach, center.z + radius);
        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            Vec3 blockCenter = Vec3.atCenterOf(cursor);
            double dx = blockCenter.x - center.x;
            double dz = blockCenter.z - center.z;
            if (dx * dx + dz * dz > radius * radius) continue;
            if (Math.abs(blockCenter.y - center.y) > verticalReach) continue;

            if (canExtinguish(level, cursor)) {
                extinguishAt(level, cursor.immutable());
                changed++;
            }
        }

        AABB area = player.getBoundingBox().inflate(radius, verticalReach, radius);
        for (Entity entity : level.getEntities(player, area, Entity::isOnFire)) {
            Vec3 entityCenter = entity.getBoundingBox().getCenter();
            double dx = entityCenter.x - center.x;
            double dz = entityCenter.z - center.z;
            if (dx * dx + dz * dz > radius * radius) continue;
            if (Math.abs(entityCenter.y - center.y) > verticalReach) continue;

            entity.clearFire();
            changed++;
        }
        if (changed == 0) return;

        spend(player, magicCost, fireCost);


        int ringPoints = 28 + Math.round(24 * charge);
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3.0D;
            for (int i = 0; i < ringPoints; i++) {
                double angle = Math.PI * 2.0D * i / ringPoints;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                level.sendParticles(ParticleTypes.CLOUD, x, center.y - 0.35D, z,
                        1, 0.03D, 0.04D, 0.03D, 0.01D);
                if ((i & 3) == 0) {
                    level.sendParticles(ParticleTypes.END_ROD, x, center.y - 0.15D, z,
                            1, 0.01D, 0.03D, 0.01D, 0.0D);
                }
            }
        }

        MagicalActEffects.impact(player);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 0.8F, 0.72F);
    }

    private static boolean canExtinguish(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || (state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT));
    }

    private static void extinguishAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            level.removeBlock(pos, false);
        } else if (state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
        }
    }

    private static Entity findEntityTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        Entity best = null;
        double bestDistance = range * range;
        for (Entity entity : player.serverLevel().getEntities(player, search, FirePowerManager::isValidTarget)) {
            var hit = entity.getBoundingBox().inflate(0.25D).clip(start, end);
            if (hit.isEmpty()) continue;
            double distance = start.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private static boolean isValidTarget(Entity entity) {
        if (!entity.isAlive() || entity instanceof Player) return false;
        return entity instanceof LivingEntity || entity instanceof ItemEntity;
    }

    private static BlockHitResult blockRaycast(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().normalize().scale(range));
        return player.serverLevel().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private static void drawFireTrail(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 0.01D) return;
        Vec3 step = delta.normalize().scale(0.42D);
        Vec3 cursor = start;
        int points = Math.min(48, (int) Math.ceil(length / 0.42D));
        for (int i = 0; i <= points; i++) {
            level.sendParticles(i % 3 == 0 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME,
                    cursor.x, cursor.y, cursor.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
            if ((i & 3) == 0) {
                level.sendParticles(ParticleTypes.SMOKE, cursor.x, cursor.y, cursor.z,
                        1, 0.015D, 0.015D, 0.015D, 0.0D);
            }
            cursor = cursor.add(step);
        }
        fireParticles(level, end, 7);
    }

    private static void fireParticles(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z,
                count, 0.22D, 0.22D, 0.22D, 0.035D);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y, pos.z,
                Math.max(1, count / 4), 0.16D, 0.16D, 0.16D, 0.02D);
    }

    private static void extinguishParticles(ServerLevel level, Vec3 pos, int count) {
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z,
                count, 0.25D, 0.22D, 0.25D, 0.025D);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z,
                Math.max(2, count / 3), 0.2D, 0.2D, 0.2D, 0.005D);
    }

    private static boolean canUseFirePower(Player player) {
        return PlayerPathManager.isWitch(player)
                && PlayerElementSelectionManager.isPowerMode(player)
                && PlayerElementSelectionManager.selectedElement(player) == ElementPower.FIRE;
    }

    private static boolean hasResources(Player player, int ignoredBodyMagicCost, int fire) {


        return PlayerMagicManager.getMagic(player) >= MagicConstants.NATURAL_LIMIT
                && PlayerElementPowerManager.get(player, ElementPower.FIRE) >= fire;
    }

    private static void spend(Player player, int ignoredBodyMagicCost, int fire) {

        PlayerElementPowerManager.remove(player, ElementPower.FIRE, fire);
    }

    private static int lerpCost(int min, int max, float amount) {
        return Math.round(min + (max - min) * amount);
    }

    private static double lerp(double min, double max, float amount) {
        return min + (max - min) * amount;
    }
}
