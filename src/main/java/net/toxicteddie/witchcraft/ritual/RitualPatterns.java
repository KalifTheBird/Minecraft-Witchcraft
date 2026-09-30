package net.toxicteddie.witchcraft.ritual;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.toxicteddie.witchcraft.block.custom.ChalkType;
import net.toxicteddie.witchcraft.item.ModItems;
import net.toxicteddie.witchcraft.item.custom.BloodBottleItem;


public final class RitualPatterns {
    private RitualPatterns() {}

    public static final int[][] SMALL_CIRCLE = {
            {-1,-3},{0,-3},{1,-3},{-2,-2},{2,-2},{-3,-1},{3,-1},
            {-3,0},{3,0},{-3,1},{3,1},{-2,2},{2,2},{-1,3},{0,3},{1,3}
    };

    public static final int[][] LARGE_CIRCLE = {
            {-3,-7},{-2,-7},{-1,-7},{0,-7},{1,-7},{2,-7},{3,-7},
            {-4,-6},{4,-6},{-5,-5},{5,-5},{-6,-4},{6,-4},
            {-7,-3},{7,-3},{-7,-2},{7,-2},{-7,-1},{7,-1},{-7,0},{7,0},
            {-7,1},{7,1},{-7,2},{7,2},{-7,3},{7,3},
            {-6,4},{6,4},{-5,5},{5,5},{-4,6},{4,6},
            {-3,7},{-2,7},{-1,7},{0,7},{1,7},{2,7},{3,7}
    };


    public static final int[][] HOLLOWING_CANDLE_RING = {

            {-3,-8},{-2,-8},{-1,-8},{0,-8},{1,-8},{2,-8},{3,-8},


            {4,-7},{5,-6},{6,-5},{7,-4},


            {8,-3},{8,-2},{8,-1},{8,0},{8,1},{8,2},{8,3},


            {7,4},{6,5},{5,6},{4,7},


            {3,8},{2,8},{1,8},{0,8},{-1,8},{-2,8},{-3,8},


            {-4,7},{-5,6},{-6,5},{-7,4},


            {-8,3},{-8,2},{-8,1},{-8,0},{-8,-1},{-8,-2},{-8,-3},


            {-7,-4},{-6,-5},{-5,-6},{-4,-7}
    };


    public static final int[][] HOLLOWING_GLIMMERSTONES = {
            {0,-4},{4,0},{0,4},{-4,0}
    };

    public static final int[][] MEDIUM_CIRCLE = {
            {-2,-5},{-1,-5},{0,-5},{1,-5},{2,-5},
            {-3,-4},{3,-4},{-4,-3},{4,-3},
            {-5,-2},{5,-2},{-5,-1},{5,-1},{-5,0},{5,0},{-5,1},{5,1},{-5,2},{5,2},
            {-4,3},{4,3},{-3,4},{3,4},
            {-2,5},{-1,5},{0,5},{1,5},{2,5}
    };


    public static final int[][] TRANSMUTATION_CANDLE_RING = {

            {-1,-4},{0,-4},{1,-4},


            {2,-3},{3,-2},


            {4,-1},{4,0},{4,1},


            {3,2},{2,3},


            {-1,4},{0,4},{1,4},


            {-2,3},{-3,2},


            {-4,-1},{-4,0},{-4,1},


            {-3,-2},{-2,-3}
    };

    public static final RitualPattern TRANSMUTATION = buildTransmutationPattern();


    public static final RitualPattern WOODS_FAVOR = RitualPattern.builder("woods_favor")
            .magicCost(300)
            .duration(190)
            .offeringRadius(5.0D)
            .circle(ChalkType.WHITE, MEDIUM_CIRCLE)
            .offering("stick", stack -> stack.is(Items.STICK))
            .offering("growth", stack ->
                    net.toxicteddie.witchcraft.item.custom.WandWoodType.fromGrowthToken(stack) != null)
            .offering("blood", stack -> stack.getItem() instanceof BloodBottleItem)
            .offering("string", stack -> stack.is(Items.STRING))
            .build();


    public static final RitualPattern HOLLOWING = buildHollowingPattern();

    private static RitualPattern buildHollowingPattern() {
        RitualPattern.Builder builder = RitualPattern.builder("hollowing")
                .magicCost(150)
                .duration(520)
                .offeringRadius(3.5D)
                .circle(ChalkType.BLACK, SMALL_CIRCLE)
                .circle(ChalkType.BLACK, MEDIUM_CIRCLE)
                .circle(ChalkType.BLACK, LARGE_CIRCLE)
                .offering("blood", stack -> stack.getItem() instanceof BloodBottleItem)
                .offering("salt", stack -> stack.is(ModItems.SALT.get()));

        for (int i = 0; i < HOLLOWING_CANDLE_RING.length; i++) {
            int[] offset = HOLLOWING_CANDLE_RING[i];
            builder.block(
                    "black_candle_" + i,
                    offset[0], 0, offset[1],
                    state -> state.is(Blocks.BLACK_CANDLE)
                            && state.getValue(CandleBlock.LIT)
            );
        }

        for (int i = 0; i < HOLLOWING_GLIMMERSTONES.length; i++) {
            int[] offset = HOLLOWING_GLIMMERSTONES[i];
            builder.block(
                    "glimmerstone_" + i,
                    offset[0], 0, offset[1],
                    state -> state.is(net.toxicteddie.witchcraft.block.ModBlocks.GLIMMERSTONE_CLUSTER.get())
            );
        }

        return builder.build();
    }

    public static final RitualPattern NIGHT = RitualPattern.builder("night")
            .magicCost(150)
            .duration(180)
            .offeringRadius(3.0D)
            .circle(ChalkType.WHITE, SMALL_CIRCLE)
            .block("white_candle", 1, 0, 0,
                    state -> state.is(Blocks.WHITE_CANDLE)
                            && state.getValue(CandleBlock.LIT))
            .block("black_candle", -1, 0, 0,
                    state -> state.is(Blocks.BLACK_CANDLE)
                            && !state.getValue(CandleBlock.LIT))
            .offering("daisy", stack -> stack.is(Items.OXEYE_DAISY))
            .offering("moonlight", stack -> stack.is(ModItems.PALE_MOONLIGHT.get()))
            .build();

    public static final RitualPattern TEMPERANCE = RitualPattern.builder("temperance")
            .magicCost(400)
            .duration(500)
            .offeringRadius(5.0D)
            .circle(ChalkType.BLACK, SMALL_CIRCLE)
            .circle(ChalkType.WHITE, MEDIUM_CIRCLE)
            .block("north_candle", 0, 0, -6, RitualPatterns::isLitGrayCandle)
            .block("east_candle", 6, 0, 0, RitualPatterns::isLitGrayCandle)
            .block("south_candle", 0, 0, 6, RitualPatterns::isLitGrayCandle)
            .block("west_candle", -6, 0, 0, RitualPatterns::isLitGrayCandle)
            .offering("blood", stack -> stack.getItem() instanceof BloodBottleItem)
            .offering("pulse", stack -> stack.is(ModItems.PULSE_OF_MORTALITY.get()))
            .offering("salt", stack -> stack.is(ModItems.SALT.get()))
            .offering("sage", stack -> stack.is(ModItems.SAGE_LEAF.get()))
            .offering("moonlight", stack -> stack.is(ModItems.PALE_MOONLIGHT.get()))
            .build();


    public static final RitualPattern EARTH_VEINS = RitualPattern.builder("earth_veins")
            .magicCost(500)
            .duration(1800)
            .offeringRadius(5.0D)


            .circle(ChalkType.WHITE, MEDIUM_CIRCLE)
            .offering("milk", stack -> stack.is(ModItems.MILK_OF_THE_MOTHER.get()))
            .offering("egg", stack -> stack.is(Items.EGG))
            .offering("dirt", stack -> stack.is(Items.DIRT))
            .offering("calling", RitualPatterns::isEarthVeinCallingOffering)
            .offering("pickaxe", stack -> stack.getItem() instanceof net.minecraft.world.item.PickaxeItem)
            .augments(RitualAugmentRules.builder()
                    .accepts(RitualAugmentType.CANDLE, RitualAugmentType.SKULL,
                            RitualAugmentType.HEAD, RitualAugmentType.CRYSTAL)
                    .outsideLargestCircle(1.0D)
                    .maximum(RitualAugmentType.CANDLE, 8)
                    .maximum(RitualAugmentType.SKULL, 4)
                    .maximum(RitualAugmentType.HEAD, 4)
                    .maximum(RitualAugmentType.CRYSTAL, 4)
                    .requireLitCandles(true)
                    .build())
            .build();

    public static final RitualPattern CALL_CORNERS = RitualPattern.builder("call_corners")
            .magicCost(250)
            .duration(0)
            .offeringRadius(3.0D)
            .circle(ChalkType.WHITE, LARGE_CIRCLE)
            .block("east_candle", 8, 0, 0, state -> isCandle(state, Blocks.YELLOW_CANDLE, false))
            .block("south_candle", 0, 0, 8, state -> isCandle(state, Blocks.RED_CANDLE, false))
            .block("west_candle", -8, 0, 0, state -> isCandle(state, Blocks.BLUE_CANDLE, false))
            .block("north_candle", 0, 0, -8, state -> isCandle(state, Blocks.GREEN_CANDLE, false))
            .offering("blessing", stack -> stack.is(ModItems.BLESSING_OF_THE_GODDESS.get()))
            .offering("sage", stack -> stack.is(ModItems.SAGE_LEAF.get()))
            .build();

    public static final RitualPattern UNCALL_CORNERS = RitualPattern.builder("uncall_corners")
            .magicCost(100)
            .duration(0)
            .offeringRadius(7.0D)
            .circle(ChalkType.WHITE, LARGE_CIRCLE)
            .block("east_candle", 8, 0, 0, state -> isCandle(state, Blocks.YELLOW_CANDLE, true))
            .block("south_candle", 0, 0, 8, state -> isCandle(state, Blocks.RED_CANDLE, true))
            .block("west_candle", -8, 0, 0, state -> isCandle(state, Blocks.BLUE_CANDLE, true))
            .block("north_candle", 0, 0, -8, state -> isCandle(state, Blocks.GREEN_CANDLE, true))
            .offering("blessing", stack -> stack.is(ModItems.BLESSING_OF_THE_GODDESS.get()))
            .offering("sage", stack -> stack.is(ModItems.SAGE_LEAF.get()))
            .build();


    private static RitualPattern buildTransmutationPattern() {
        RitualPattern.Builder builder = RitualPattern.builder("transmutation")
                .magicCost(200)
                .duration(1600)
                .offeringRadius(4.0D)
                .circle(ChalkType.WHITE, SMALL_CIRCLE)
                .offering("pure_essence", stack -> stack.is(ModItems.PURE_ESSENCE.get()))
                .offering("wool_of_bat", stack -> stack.is(ModItems.WOOL_OF_BAT.get()))
                .offering("salt", stack -> stack.is(ModItems.SALT.get()))
                .offering("tear", stack -> stack.is(ModItems.TEAR_OF_THE_MAIDEN.get()))
                .offering("milk", stack -> stack.is(ModItems.MILK_OF_THE_MOTHER.get()))
                .offering("spit", stack -> stack.is(ModItems.SPIT_OF_THE_CRONE.get()));

        for (int i = 0; i < TRANSMUTATION_CANDLE_RING.length; i++) {
            int[] pos = TRANSMUTATION_CANDLE_RING[i];
            builder.block("transmutation_candle_" + i, pos[0], 0, pos[1],
                    state -> state.is(Blocks.WHITE_CANDLE)
                            && state.hasProperty(CandleBlock.LIT)
                            && state.getValue(CandleBlock.LIT));
        }
        return builder.build();
    }

    private static boolean isEarthVeinCallingOffering(net.minecraft.world.item.ItemStack stack) {
        return stack.is(Items.COAL)
                || stack.is(Items.IRON_INGOT)
                || stack.is(Items.COPPER_INGOT)
                || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.REDSTONE)
                || stack.is(Items.LAPIS_LAZULI)
                || stack.is(Items.DIAMOND)
                || stack.is(Items.EMERALD);
    }

    private static boolean isCandle(net.minecraft.world.level.block.state.BlockState state,
                                    net.minecraft.world.level.block.Block block, boolean lit) {
        return state.is(block) && state.hasProperty(CandleBlock.LIT)
                && state.getValue(CandleBlock.LIT) == lit;
    }

    private static boolean isLitGrayCandle(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Blocks.GRAY_CANDLE)
                && state.hasProperty(CandleBlock.LIT)
                && state.getValue(CandleBlock.LIT);
    }


}
