package net.toxicteddie.witchcraft.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import org.jetbrains.annotations.Nullable;


public final class MagicDeedManager {
    public static final int DEFAULT_ALTAR_RANGE = 16;
    public static final float RITUAL_OVERFLOW_RATIO = 0.25F;

    public static final int FULL_BODY_RITUAL_COST = 150;
    public static final int MAX_RITUAL_BODY_CONTRIBUTION = 250;
    public static final float LARGE_RITUAL_BODY_SCALING = 0.20F;

    private MagicDeedManager() {}


    public static MagicDeedResult perform(ServerPlayer witch, BlockPos deedPosition, int cost) {
        return performInternal(witch, deedPosition, cost, DEFAULT_ALTAR_RANGE, false, 0.0F);
    }


    public static MagicDeedResult perform(
            ServerPlayer witch,
            BlockPos deedPosition,
            int cost,
            int altarRange
    ) {
        return performInternal(witch, deedPosition, cost, altarRange, false, 0.0F);
    }


    public static MagicDeedResult performRitual(ServerPlayer witch, BlockPos ritualPosition, int cost) {
        return performRitual(witch, ritualPosition, cost, DEFAULT_ALTAR_RANGE);
    }


    public static MagicDeedResult performRitual(
            ServerPlayer witch,
            BlockPos ritualPosition,
            int cost,
            int altarRange
    ) {
        return performInternal(
                witch,
                ritualPosition,
                cost,
                altarRange,
                true,
                RITUAL_OVERFLOW_RATIO
        );
    }

    private static MagicDeedResult performInternal(
            ServerPlayer witch,
            BlockPos deedPosition,
            int cost,
            int altarRange,
            boolean ritual,
            float overflowRatio
    ) {
        int requested = Math.max(0, cost);
        if (requested == 0) {
            return new MagicDeedResult(true, 0, 0, 0, 0, null,
                    MagicDeedResult.AltarRelationship.NONE);
        }

        if (!PlayerPathManager.isWitch(witch) || !(witch.level() instanceof ServerLevel level)) {
            return new MagicDeedResult(false, requested, 0, 0, 0, null,
                    MagicDeedResult.AltarRelationship.NONE);
        }


        if (PlayerMagicManager.isMagicallyExhausted(witch)) {
            return new MagicDeedResult(false, requested, 0, 0, 0, null,
                    MagicDeedResult.AltarRelationship.NONE);
        }

        SelectedAltar selected = selectAltar(level, witch, deedPosition, altarRange);

        int altarAvailable = selected == null ? 0 : selected.altar().getStoredPower();


        boolean altarCouldFullyFund = ritual
                && selected != null
                && altarAvailable >= requested;

        int bodyAvailable = PlayerMagicManager.getMagic(witch);
        int bodyContributionLimit = ritual
                ? ritualBodyContributionLimit(requested)
                : requested;

        int altarNeeded = Math.min(altarAvailable, requested);
        int deficitAfterAltar = requested - altarNeeded;


        if (ritual && (deficitAfterAltar > bodyContributionLimit
                || bodyAvailable < deficitAfterAltar)) {
            return new MagicDeedResult(
                    false,
                    requested,
                    0,
                    0,
                    0,
                    selected == null ? null : selected.altar().getBlockPos().immutable(),
                    selected == null
                            ? MagicDeedResult.AltarRelationship.NONE
                            : selected.relationship()
            );
        }

        int remaining = requested;
        int altarUsed = 0;
        if (selected != null) {
            altarUsed = selected.altar().extractPower(remaining);
            remaining -= altarUsed;
        }

        int bodyUsed = PlayerMagicManager.removeMagic(witch, remaining);
        remaining -= bodyUsed;

        boolean successful = remaining <= 0;
        int overflowGranted = 0;

        if (successful && altarCouldFullyFund && overflowRatio > 0.0F) {
            int overflow = Math.max(1, (int) Math.ceil(requested * (double) overflowRatio));
            overflowGranted = PlayerMagicManager.addMagic(witch, overflow);
        }

        return new MagicDeedResult(
                successful,
                requested,
                altarUsed,
                bodyUsed,
                overflowGranted,
                selected == null ? null : selected.altar().getBlockPos().immutable(),
                selected == null
                        ? MagicDeedResult.AltarRelationship.NONE
                        : selected.relationship()
        );
    }


    public static int ritualBodyContributionLimit(int ritualCost) {
        int cost = Math.max(0, ritualCost);
        if (cost <= FULL_BODY_RITUAL_COST) {
            return cost;
        }

        int extraCost = cost - FULL_BODY_RITUAL_COST;
        int scaledExtra = (int) Math.ceil(extraCost * (double) LARGE_RITUAL_BODY_SCALING);
        return Math.min(MAX_RITUAL_BODY_CONTRIBUTION,
                FULL_BODY_RITUAL_COST + scaledExtra);
    }


    @Nullable
    public static WitchesAltarBlockEntity findPreferredAltar(
            ServerLevel level,
            ServerPlayer witch,
            BlockPos deedPosition,
            int altarRange
    ) {
        SelectedAltar selected = selectAltar(level, witch, deedPosition, altarRange);
        return selected == null ? null : selected.altar();
    }

    @Nullable
    private static SelectedAltar selectAltar(
            ServerLevel level,
            ServerPlayer witch,
            BlockPos deedPosition,
            int altarRange
    ) {
        int range = Math.max(0, altarRange);
        long maxDistanceSquared = (long) range * range;
        SelectedAltar bestOwned = null;
        SelectedAltar bestBound = null;

        BlockPos min = deedPosition.offset(-range, -range, -range);
        BlockPos max = deedPosition.offset(range, range, range);

        for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
            if (cursor.distSqr(deedPosition) > maxDistanceSquared) {
                continue;
            }
            if (!(level.getBlockEntity(cursor) instanceof WitchesAltarBlockEntity altar)
                    || !altar.hasCarpet()
                    || altar.getStoredPower() <= 0
                    || !altar.isAuthorized(witch)) {
                continue;
            }

            MagicDeedResult.AltarRelationship relationship = altar.isOwner(witch)
                    ? MagicDeedResult.AltarRelationship.OWNER
                    : MagicDeedResult.AltarRelationship.BOUND;

            SelectedAltar candidate = new SelectedAltar(
                    altar,
                    relationship,
                    altar.getStoredPower(),
                    cursor.distSqr(deedPosition)
            );

            if (relationship == MagicDeedResult.AltarRelationship.OWNER) {
                if (isBetter(candidate, bestOwned)) {
                    bestOwned = candidate;
                }
            } else if (isBetter(candidate, bestBound)) {
                bestBound = candidate;
            }
        }

        return bestOwned != null ? bestOwned : bestBound;
    }

    private static boolean isBetter(SelectedAltar candidate, @Nullable SelectedAltar current) {
        if (current == null) {
            return true;
        }
        if (candidate.power() != current.power()) {
            return candidate.power() > current.power();
        }
        return candidate.distanceSquared() < current.distanceSquared();
    }

    private record SelectedAltar(
            WitchesAltarBlockEntity altar,
            MagicDeedResult.AltarRelationship relationship,
            int power,
            double distanceSquared
    ) {}
}
