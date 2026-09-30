package net.toxicteddie.witchcraft.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.custom.ChalkGlyphBlock;
import net.toxicteddie.witchcraft.block.custom.ChalkType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public final class RitualPattern {
    private final String id;
    private final int magicCost;
    private final int duration;
    private final double offeringRadius;
    private final List<CircleRequirement> circles;
    private final List<BlockRequirement> blocks;
    private final List<OfferingRequirement> offerings;
    private final RitualAugmentRules augmentRules;

    private RitualPattern(Builder builder) {
        this.id = builder.id;
        this.magicCost = builder.magicCost;
        this.duration = builder.duration;
        this.offeringRadius = builder.offeringRadius;
        this.circles = List.copyOf(builder.circles);
        this.blocks = List.copyOf(builder.blocks);
        this.offerings = List.copyOf(builder.offerings);
        this.augmentRules = builder.augmentRules;
    }

    public String id() { return id; }
    public int magicCost() { return magicCost; }
    public int duration() { return duration; }
    public int offeringCount() { return offerings.size(); }

    @Nullable
    public Match match(ServerLevel level, BlockPos heartPos) {
        for (CircleRequirement circle : circles) {
            if (!circle.matches(level, heartPos)) return null;
        }

        Map<String, BlockPos> matchedBlocks = new LinkedHashMap<>();
        for (BlockRequirement requirement : blocks) {
            BlockPos actualPos = heartPos.offset(requirement.offset());
            if (!requirement.stateTest().test(level.getBlockState(actualPos))) return null;
            matchedBlocks.put(requirement.key(), actualPos.immutable());
        }

        AABB searchBox = new AABB(
                heartPos.getX() - offeringRadius, heartPos.getY() - 0.25D,
                heartPos.getZ() - offeringRadius,
                heartPos.getX() + offeringRadius + 1.0D, heartPos.getY() + 2.25D,
                heartPos.getZ() + offeringRadius + 1.0D);
        List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, searchBox,
                entity -> entity.isAlive() && !entity.getItem().isEmpty());
        Comparator<ItemEntity> nearest = Comparator.comparingDouble(entity ->
                entity.distanceToSqr(heartPos.getX() + 0.5D,
                        heartPos.getY() + 0.15D, heartPos.getZ() + 0.5D));

        Map<String, ItemEntity> matchedOfferings = new LinkedHashMap<>();
        List<ItemEntity> alreadyUsed = new ArrayList<>();
        for (OfferingRequirement requirement : offerings) {
            ItemEntity match = nearby.stream()
                    .filter(entity -> !alreadyUsed.contains(entity))
                    .filter(entity -> requirement.stackTest().test(entity.getItem()))
                    .min(nearest)
                    .orElse(null);
            if (match == null) return null;
            alreadyUsed.add(match);
            matchedOfferings.put(requirement.key(), match);
        }

        RitualAugments augments = RitualAugmentScanner.scan(
                level, heartPos, matchedBlocks.values(), augmentRules, outermostCircleRadius());

        return new Match(this, matchedOfferings, matchedBlocks, augments);
    }


    private double outermostCircleRadius() {
        double farthestSqr = 0.0D;
        for (CircleRequirement circle : circles) {
            for (int[] offset : circle.offsets()) {
                double distanceSqr = (double) offset[0] * offset[0]
                        + (double) offset[1] * offset[1];
                farthestSqr = Math.max(farthestSqr, distanceSqr);
            }
        }
        return Math.sqrt(farthestSqr);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public record Match(RitualPattern pattern,
                        Map<String, ItemEntity> offerings,
                        Map<String, BlockPos> blocks,
                        RitualAugments augments) {
        public ItemEntity offering(String key) { return offerings.get(key); }
        public BlockPos block(String key) { return blocks.get(key); }
    }

    private record CircleRequirement(ChalkType chalkType, int[][] offsets) {
        private boolean matches(ServerLevel level, BlockPos heartPos) {
            for (int[] offset : offsets) {
                BlockState state = level.getBlockState(heartPos.offset(offset[0], 0, offset[1]));
                if (!state.is(ModBlocks.CHALK_GLYPH.get())
                        || state.getValue(ChalkGlyphBlock.TYPE) != chalkType) return false;
            }
            return true;
        }
    }

    private record BlockRequirement(String key, BlockPos offset,
                                    Predicate<BlockState> stateTest) {}
    private record OfferingRequirement(String key, Predicate<ItemStack> stackTest) {}

    public static final class Builder {
        private final String id;
        private int magicCost;
        private int duration;
        private double offeringRadius = 3.0D;
        private final List<CircleRequirement> circles = new ArrayList<>();
        private final List<BlockRequirement> blocks = new ArrayList<>();
        private final List<OfferingRequirement> offerings = new ArrayList<>();
        private RitualAugmentRules augmentRules = RitualAugmentRules.none();

        private Builder(String id) { this.id = id; }

        public Builder magicCost(int value) { this.magicCost = value; return this; }
        public Builder duration(int ticks) { this.duration = ticks; return this; }
        public Builder offeringRadius(double blocks) { this.offeringRadius = blocks; return this; }

        public Builder circle(ChalkType chalkType, int[][] offsets) {
            circles.add(new CircleRequirement(chalkType, offsets));
            return this;
        }


        public Builder block(String key, int x, int y, int z, Predicate<BlockState> stateTest) {
            blocks.add(new BlockRequirement(key, new BlockPos(x, y, z), stateTest));
            return this;
        }

        public Builder augments(RitualAugmentRules rules) {
            this.augmentRules = rules == null ? RitualAugmentRules.none() : rules;
            return this;
        }

        public Builder offering(String key, Predicate<ItemStack> stackTest) {
            offerings.add(new OfferingRequirement(key, stackTest));
            return this;
        }

        public RitualPattern build() {
            if (offerings.isEmpty()) throw new IllegalStateException(id + " needs at least one offering");
            return new RitualPattern(this);
        }
    }
}
