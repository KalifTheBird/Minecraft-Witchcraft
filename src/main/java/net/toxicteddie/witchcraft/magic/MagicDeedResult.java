package net.toxicteddie.witchcraft.magic;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;


public record MagicDeedResult(
        boolean successful,
        int requested,
        int altarUsed,
        int bodyUsed,
        int overflowGranted,
        @Nullable BlockPos altarPosition,
        AltarRelationship altarRelationship
) {
    public int totalUsed() {
        return altarUsed + bodyUsed;
    }

    public int missingPower() {
        return Math.max(0, requested - totalUsed());
    }

    public boolean usedAltar() {
        return altarUsed > 0;
    }

    public enum AltarRelationship {
        NONE,
        OWNER,
        BOUND
    }
}
