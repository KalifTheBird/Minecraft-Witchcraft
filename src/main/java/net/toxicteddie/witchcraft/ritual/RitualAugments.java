package net.toxicteddie.witchcraft.ritual;

import net.minecraft.core.BlockPos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public final class RitualAugments {
    private static final RitualAugments EMPTY = new RitualAugments(Map.of());

    private final Map<RitualAugmentType, List<BlockPos>> positions;

    RitualAugments(Map<RitualAugmentType, List<BlockPos>> positions) {
        EnumMap<RitualAugmentType, List<BlockPos>> copy =
                new EnumMap<>(RitualAugmentType.class);
        positions.forEach((type, found) -> copy.put(type, List.copyOf(found)));
        this.positions = Map.copyOf(copy);
    }

    public static RitualAugments empty() {
        return EMPTY;
    }

    public int count(RitualAugmentType type) {
        return positions(type).size();
    }

    public boolean has(RitualAugmentType type) {
        return count(type) > 0;
    }

    public List<BlockPos> positions(RitualAugmentType type) {
        return positions.getOrDefault(type, List.of());
    }

    public int totalCount() {
        return positions.values().stream().mapToInt(List::size).sum();
    }
}
