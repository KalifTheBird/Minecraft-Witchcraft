package net.toxicteddie.witchcraft.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class RitualAugmentScanner {
    private RitualAugmentScanner() {}

    public static RitualAugments scan(ServerLevel level, BlockPos heartPos,
                                      Collection<BlockPos> claimedPositions,
                                      RitualAugmentRules rules,
                                      double largestCircleRadius) {
        if (!rules.isEnabled()) return RitualAugments.empty();

        Set<BlockPos> claimed = new HashSet<>();
        for (BlockPos pos : claimedPositions) claimed.add(pos.immutable());

        EnumMap<RitualAugmentType, List<BlockPos>> found =
                new EnumMap<>(RitualAugmentType.class);
        for (RitualAugmentType type : RitualAugmentType.values()) {
            found.put(type, new ArrayList<>());
        }

        double targetRadius = largestCircleRadius + rules.outsideOffset();
        double innerRadius = Math.max(0.0D, targetRadius - rules.ringTolerance());
        double outerRadius = targetRadius + rules.ringTolerance();
        int extent = (int) Math.ceil(outerRadius);
        double innerSqr = innerRadius * innerRadius;
        double outerSqr = outerRadius * outerRadius;

        for (int y = -rules.verticalRange(); y <= rules.verticalRange(); y++) {
            for (int x = -extent; x <= extent; x++) {
                for (int z = -extent; z <= extent; z++) {
                    double horizontalDistanceSqr = x * x + z * z;
                    if (horizontalDistanceSqr < innerSqr || horizontalDistanceSqr > outerSqr) continue;

                    BlockPos pos = heartPos.offset(x, y, z);
                    if (claimed.contains(pos)) continue;

                    BlockState state = level.getBlockState(pos);
                    RitualAugmentType type = classify(state, rules.requireLitCandles());
                    if (type == null || !rules.accepts(type)) continue;

                    List<BlockPos> positions = found.get(type);
                    if (positions.size() < rules.maximumCount(type)) {
                        positions.add(pos.immutable());
                    }
                }
            }
        }

        Comparator<BlockPos> nearest = Comparator.comparingDouble(pos ->
                pos.distSqr(heartPos));
        found.values().forEach(list -> list.sort(nearest));
        return new RitualAugments(found);
    }

    @Nullable
    private static RitualAugmentType classify(BlockState state, boolean requireLitCandles) {
        Block block = state.getBlock();

        if (block instanceof CandleBlock) {
            if (requireLitCandles && state.hasProperty(CandleBlock.LIT)
                    && !state.getValue(CandleBlock.LIT)) {
                return null;
            }
            return RitualAugmentType.CANDLE;
        }

        if (isSkull(block)) return RitualAugmentType.SKULL;
        if (isHead(block)) return RitualAugmentType.HEAD;
        if (isCrystal(block)) return RitualAugmentType.CRYSTAL;
        return null;
    }

    private static boolean isSkull(Block block) {
        return block == Blocks.SKELETON_SKULL
                || block == Blocks.SKELETON_WALL_SKULL
                || block == Blocks.WITHER_SKELETON_SKULL
                || block == Blocks.WITHER_SKELETON_WALL_SKULL;
    }

    private static boolean isHead(Block block) {
        return block == Blocks.ZOMBIE_HEAD
                || block == Blocks.ZOMBIE_WALL_HEAD
                || block == Blocks.CREEPER_HEAD
                || block == Blocks.CREEPER_WALL_HEAD
                || block == Blocks.PLAYER_HEAD
                || block == Blocks.PLAYER_WALL_HEAD
                || block == Blocks.DRAGON_HEAD
                || block == Blocks.DRAGON_WALL_HEAD
                || block == Blocks.PIGLIN_HEAD
                || block == Blocks.PIGLIN_WALL_HEAD;
    }

    private static boolean isCrystal(Block block) {
        return block == Blocks.SMALL_AMETHYST_BUD
                || block == Blocks.MEDIUM_AMETHYST_BUD
                || block == Blocks.LARGE_AMETHYST_BUD
                || block == Blocks.AMETHYST_CLUSTER;
    }
}
