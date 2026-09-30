package net.toxicteddie.witchcraft.ritual;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


public final class RitualAugmentRules {
    private static final RitualAugmentRules NONE = new Builder().build();

    private final Set<RitualAugmentType> enabledTypes;
    private final Map<RitualAugmentType, Integer> maximumCounts;
    private final double outsideOffset;
    private final double ringTolerance;
    private final int verticalRange;
    private final boolean requireLitCandles;

    private RitualAugmentRules(Builder builder) {
        this.enabledTypes = Set.copyOf(builder.enabledTypes);
        this.maximumCounts = Map.copyOf(builder.maximumCounts);
        this.outsideOffset = builder.outsideOffset;
        this.ringTolerance = builder.ringTolerance;
        this.verticalRange = builder.verticalRange;
        this.requireLitCandles = builder.requireLitCandles;
    }

    public static RitualAugmentRules none() {
        return NONE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEnabled() {
        return !enabledTypes.isEmpty();
    }

    public boolean accepts(RitualAugmentType type) {
        return enabledTypes.contains(type);
    }

    public int maximumCount(RitualAugmentType type) {
        return maximumCounts.getOrDefault(type, Integer.MAX_VALUE);
    }


    public double outsideOffset() {
        return outsideOffset;
    }


    public double ringTolerance() {
        return ringTolerance;
    }

    public int verticalRange() {
        return verticalRange;
    }

    public boolean requireLitCandles() {
        return requireLitCandles;
    }

    public static final class Builder {
        private final EnumSet<RitualAugmentType> enabledTypes =
                EnumSet.noneOf(RitualAugmentType.class);
        private final EnumMap<RitualAugmentType, Integer> maximumCounts =
                new EnumMap<>(RitualAugmentType.class);
        private double outsideOffset = 1.0D;
        private double ringTolerance = 0.65D;
        private int verticalRange = 2;
        private boolean requireLitCandles = true;

        private Builder() {}

        public Builder accepts(RitualAugmentType... types) {
            for (RitualAugmentType type : types) {
                enabledTypes.add(type);
            }
            return this;
        }

        public Builder maximum(RitualAugmentType type, int count) {
            if (count < 0) throw new IllegalArgumentException("Augment maximum cannot be negative");
            maximumCounts.put(type, count);
            return this;
        }


        public Builder outsideLargestCircle(double blocks) {
            if (blocks < 0.0D) {
                throw new IllegalArgumentException("Outside-circle offset cannot be negative");
            }
            this.outsideOffset = blocks;
            return this;
        }


        public Builder ringTolerance(double blocks) {
            if (blocks < 0.0D) {
                throw new IllegalArgumentException("Ring tolerance cannot be negative");
            }
            this.ringTolerance = blocks;
            return this;
        }

        public Builder verticalRange(int blocks) {
            if (blocks < 0) throw new IllegalArgumentException("Vertical range cannot be negative");
            this.verticalRange = blocks;
            return this;
        }

        public Builder requireLitCandles(boolean value) {
            this.requireLitCandles = value;
            return this;
        }

        public RitualAugmentRules build() {
            return new RitualAugmentRules(this);
        }
    }
}
