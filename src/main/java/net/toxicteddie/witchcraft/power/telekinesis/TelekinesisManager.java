package net.toxicteddie.witchcraft.power.telekinesis;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.effect.MagicalActEffects;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.PlayerMagicManager;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.PlayerElementPowerManager;
import net.toxicteddie.witchcraft.power.PlayerElementSelectionManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


public final class TelekinesisManager {
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private static final Map<UUID, Integer> SECONDARY_CHARGE = new HashMap<>();

    private static final double GRAB_RANGE = 8.0D;
    private static final double HOLD_DISTANCE = 4.0D;
    private static final double FOLLOW_STRENGTH = 0.34D;
    private static final double MAX_HOLD_SPEED = 1.15D;
    private static final double FLING_THRESHOLD = 0.32D;
    private static final double FLING_MULTIPLIER = 1.85D;
    private static final double MAX_FLING_SPEED = 2.35D;

    private static final int START_MAGIC_COST = 5;
    private static final int START_AIR_COST = 2;
    private static final int DRAIN_INTERVAL_TICKS = 5;
    private static final int HOLD_MAGIC_COST = 1;
    private static final int HOLD_AIR_COST = 1;

    private static final int PULL_MAGIC_COST = 4;
    private static final int PULL_AIR_COST = 5;
    private static final double PULL_STRENGTH = 1.25D;

    private static final int PUSH_MAGIC_COST = 5;
    private static final int PUSH_AIR_COST = 7;
    private static final double PUSH_STRENGTH = 1.55D;

    private static final int BURST_THRESHOLD_TICKS = 8;
    private static final int MAX_BURST_CHARGE_TICKS = 30;
    private static final int BURST_MIN_MAGIC_COST = 7;
    private static final int BURST_MAX_MAGIC_COST = 18;
    private static final int BURST_MIN_AIR_COST = 9;
    private static final int BURST_MAX_AIR_COST = 25;
    private static final double BURST_MIN_RADIUS = 3.25D;
    private static final double BURST_MAX_RADIUS = 7.0D;
    private static final double BURST_MIN_STRENGTH = 0.85D;
    private static final double BURST_MAX_STRENGTH = 2.35D;

    private TelekinesisManager() {}

    public static boolean start(ServerPlayer player) {
        if (!canUseAirPower(player) || ACTIVE.containsKey(player.getUUID())) return false;
        Entity target = findTarget(player, GRAB_RANGE);
        if (target == null || isHeld(target)) return false;
        if (!hasResources(player, START_MAGIC_COST, START_AIR_COST)) return false;

        spend(player, START_MAGIC_COST, START_AIR_COST);
        target.setNoGravity(true);
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
        target.hurtMarked = true;
        ACTIVE.put(player.getUUID(), new Session(target.getId(), target.position()));

        ServerLevel level = player.serverLevel();
        spark(level, target, 5);
        MagicalActEffects.impact(player);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.42F, 0.62F);
        return true;
    }

    public static void release(ServerPlayer player, boolean allowFling) {
        Session session = ACTIVE.remove(player.getUUID());
        if (session == null) return;
        Entity target = player.serverLevel().getEntity(session.entityId);
        if (target == null) return;

        target.setNoGravity(false);
        if (allowFling && session.smoothedMotion.length() >= FLING_THRESHOLD) {
            Vec3 fling = clampLength(session.smoothedMotion.scale(FLING_MULTIPLIER), MAX_FLING_SPEED);
            target.setDeltaMovement(fling);
            target.hurtMarked = true;
            player.serverLevel().sendParticles(ParticleTypes.POOF,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    10, 0.22D, 0.22D, 0.22D, 0.08D);
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 0.7F, 0.75F);
        } else {
            target.hurtMarked = true;
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundSource.PLAYERS, 0.45F, 0.62F);
        }
    }

    public static void pull(ServerPlayer player) {
        if (!canUseAirPower(player)) return;
        Entity target = findTarget(player, 10.0D);
        if (target == null || isHeld(target) || !hasResources(player, PULL_MAGIC_COST, PULL_AIR_COST)) return;

        spend(player, PULL_MAGIC_COST, PULL_AIR_COST);
        Vec3 destination = player.getEyePosition().add(player.getLookAngle().scale(1.4D));
        Vec3 velocity = clampLength(destination.subtract(target.getBoundingBox().getCenter()).scale(0.42D), PULL_STRENGTH);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        spark(player.serverLevel(), target, 6);
        MagicalActEffects.impact(player);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
                SoundSource.PLAYERS, 0.55F, 1.25F);
    }

    public static void beginSecondary(ServerPlayer player) {
        if (!canUseAirPower(player)) return;

        release(player, false);
        SECONDARY_CHARGE.put(player.getUUID(), 0);
    }

    public static void releaseSecondary(ServerPlayer player) {
        Integer ticks = SECONDARY_CHARGE.remove(player.getUUID());
        if (ticks == null || !canUseAirPower(player)) return;
        if (ticks < BURST_THRESHOLD_TICKS) {
            forcePush(player);
        } else {
            burst(player, ticks);
        }
    }

    private static void forcePush(ServerPlayer player) {
        Entity target = findTarget(player, 9.0D);
        if (target == null || isHeld(target) || !hasResources(player, PUSH_MAGIC_COST, PUSH_AIR_COST)) return;

        spend(player, PUSH_MAGIC_COST, PUSH_AIR_COST);
        Vec3 away = target.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
        Vec3 velocity = away.scale(PUSH_STRENGTH).add(0.0D, 0.34D, 0.0D);
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;
        spark(player.serverLevel(), target, 7);
        MagicalActEffects.impact(player);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP,
                SoundSource.PLAYERS, 0.72F, 0.82F);
    }

    private static void burst(ServerPlayer player, int chargedTicks) {
        float charge = Math.min(1.0F, chargedTicks / (float) MAX_BURST_CHARGE_TICKS);
        int magicCost = lerpCost(BURST_MIN_MAGIC_COST, BURST_MAX_MAGIC_COST, charge);
        int airCost = lerpCost(BURST_MIN_AIR_COST, BURST_MAX_AIR_COST, charge);
        if (!hasResources(player, magicCost, airCost)) return;

        spend(player, magicCost, airCost);
        double radius = lerp(BURST_MIN_RADIUS, BURST_MAX_RADIUS, charge);
        double strength = lerp(BURST_MIN_STRENGTH, BURST_MAX_STRENGTH, charge);


        AABB area = player.getBoundingBox().inflate(radius, 3.5D, radius);
        Vec3 burstCenter = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        for (Entity entity : player.serverLevel().getEntities(player, area, TelekinesisManager::isValidTarget)) {
            if (isHeld(entity)) continue;

            Vec3 entityCenter = entity.getBoundingBox().getCenter();
            Vec3 offset = entityCenter.subtract(burstCenter);
            double horizontalDistance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
            if (horizontalDistance > radius || Math.abs(offset.y) > 3.5D) continue;

            double distance = Math.max(0.6D, horizontalDistance);
            double falloff = 1.0D - (distance / radius) * 0.45D;


            Vec3 outward = new Vec3(offset.x, 0.0D, offset.z);
            if (outward.lengthSqr() < 1.0E-4D) {
                outward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            }
            outward = outward.normalize().scale(strength * falloff);
            double launchY = (0.34D + 0.56D * charge) * falloff;
            entity.setDeltaMovement(outward.add(0.0D, launchY, 0.0D));
            entity.hurtMarked = true;
        }

        ServerLevel level = player.serverLevel();
        spawnBurstCircle(level, burstCenter, radius, charge);
        MagicalActEffects.impact(player);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 0.42F + charge * 0.32F, 1.35F - charge * 0.35F);
    }

    private static void spawnBurstCircle(ServerLevel level, Vec3 center, double radius, float charge) {
        int rings = 3;
        int points = 28 + Math.round(20.0F * charge);

        for (int ring = 1; ring <= rings; ring++) {
            double ringRadius = radius * ring / rings;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0D * i / points;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;
                double y = center.y - 0.45D + 0.08D * Math.sin(angle * 3.0D + ring);

                level.sendParticles(ParticleTypes.POOF, x, y, z,
                        1, 0.0D, 0.015D, 0.0D, 0.0D);

                if ((i + ring) % 4 == 0) {
                    level.sendParticles(ParticleTypes.END_ROD, x, y + 0.12D, z,
                            1, 0.0D, 0.025D, 0.0D, 0.0D);
                }
            }
        }
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();
        if (SECONDARY_CHARGE.containsKey(id)) {
            if (!canUseAirPower(player) || !player.isAlive()) {
                SECONDARY_CHARGE.remove(id);
            } else {
                SECONDARY_CHARGE.computeIfPresent(id, (ignored, value) -> Math.min(MAX_BURST_CHARGE_TICKS, value + 1));
            }
        }

        Session session = ACTIVE.get(id);
        if (session == null) return;
        Entity target = player.serverLevel().getEntity(session.entityId);
        if (target == null || !target.isAlive() || !player.isAlive()
                || player.distanceToSqr(target) > 18.0D * 18.0D || !canUseAirPower(player)) {
            release(player, false);
            return;
        }

        session.heldTicks++;
        if (session.heldTicks % DRAIN_INTERVAL_TICKS == 0) {
            if (!hasResources(player, HOLD_MAGIC_COST, HOLD_AIR_COST)) {
                release(player, false);
                return;
            }
            spend(player, HOLD_MAGIC_COST, HOLD_AIR_COST);
        }

        Vec3 desiredCenter = player.getEyePosition().add(player.getLookAngle().normalize().scale(HOLD_DISTANCE));
        Vec3 currentCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 velocity = clampLength(desiredCenter.subtract(currentCenter).scale(FOLLOW_STRENGTH), MAX_HOLD_SPEED);
        target.setNoGravity(true);
        target.fallDistance = 0.0F;
        target.setDeltaMovement(velocity);
        target.hurtMarked = true;

        Vec3 moved = target.position().subtract(session.lastPosition);
        session.smoothedMotion = session.smoothedMotion.scale(0.68D).add(moved.scale(0.32D));
        session.lastPosition = target.position();
        if (session.heldTicks % 6 == 0) spark(player.serverLevel(), target, 1);
    }

    public static void releaseAllForMissingPlayers(ServerLevel level) {
        Iterator<Map.Entry<UUID, Session>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Session> entry = iterator.next();
            if (level.getServer().getPlayerList().getPlayer(entry.getKey()) != null) continue;
            Entity target = level.getEntity(entry.getValue().entityId);
            if (target != null) {
                target.setNoGravity(false);
                target.hurtMarked = true;
            }
            SECONDARY_CHARGE.remove(entry.getKey());
            iterator.remove();
        }
    }

    private static boolean canUseAirPower(Player player) {
        return PlayerPathManager.isWitch(player)
                && PlayerElementSelectionManager.isPowerMode(player)
                && PlayerElementSelectionManager.selectedElement(player) == ElementPower.AIR;
    }

    private static boolean hasResources(Player player, int ignoredBodyMagicCost, int air) {


        return PlayerMagicManager.getMagic(player) >= MagicConstants.NATURAL_LIMIT
                && PlayerElementPowerManager.get(player, ElementPower.AIR) >= air;
    }

    private static void spend(Player player, int ignoredBodyMagicCost, int air) {

        PlayerElementPowerManager.remove(player, ElementPower.AIR, air);
    }

    private static boolean isHeld(Entity entity) {
        for (Session session : ACTIVE.values()) if (session.entityId == entity.getId()) return true;
        return false;
    }

    private static Entity findTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        Entity best = null;
        double bestDistance = range * range;
        for (Entity entity : player.serverLevel().getEntities(player, search, TelekinesisManager::isValidTarget)) {
            var hit = entity.getBoundingBox().inflate(0.3D).clip(start, end);
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
        if (!entity.isAlive() || entity.isPassenger() || !entity.getPassengers().isEmpty()) return false;
        if (entity instanceof Player) return false;
        return entity instanceof LivingEntity || entity instanceof ItemEntity;
    }

    private static void spark(ServerLevel level, Entity target, int count) {
        level.sendParticles(ParticleTypes.END_ROD,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                count, target.getBbWidth() * 0.38D, target.getBbHeight() * 0.32D,
                target.getBbWidth() * 0.38D, 0.008D);
    }

    private static int lerpCost(int min, int max, float amount) {
        return Math.round(min + (max - min) * amount);
    }

    private static double lerp(double min, double max, float amount) {
        return min + (max - min) * amount;
    }

    private static Vec3 clampLength(Vec3 value, double maximum) {
        if (value.lengthSqr() <= maximum * maximum) return value;
        return value.normalize().scale(maximum);
    }

    private static final class Session {
        private final int entityId;
        private Vec3 lastPosition;
        private Vec3 smoothedMotion = Vec3.ZERO;
        private int heldTicks;
        private Session(int entityId, Vec3 lastPosition) {
            this.entityId = entityId;
            this.lastPosition = lastPosition;
        }
    }
}
