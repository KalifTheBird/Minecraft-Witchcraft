package net.toxicteddie.witchcraft.block.custom;


import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;
import net.toxicteddie.witchcraft.item.ModItems;

public class HemlockCropBlock extends CropBlock {
    public static final int MAX_AGE = 5;
    public static final int TRANSITION_START_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 8);
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    public HemlockCropBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_AGE[this.getAge(pState)];
    }
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!pLevel.isLoaded(pPos)) return;
        if (pLevel.getRawBrightness(pPos, 0) >= 9) {
            int currentAge = this.getAge(pState);


            if (currentAge < this.getMaxAge()) {
                float growthSpeed = getGrowthSpeed(this, pLevel, pPos);


                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, pRandom.nextInt((int)(25.0F / growthSpeed) + 1) == 0)) {
                    if ((currentAge + 1) >= TRANSITION_START_AGE) {
                        int topBlockAge = (currentAge + 1) + 3;


                        pLevel.setBlock(pPos.above(1), this.getStateForAge(topBlockAge), 2);
                        pLevel.setBlock(pPos, this.getStateForAge(currentAge + 1), 2);
                    } else {
                        pLevel.setBlock(pPos, this.getStateForAge(currentAge + 1), 2);
                    }

                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
                }
            }
        }
    }
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable platable) {
        return super.mayPlaceOn(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {

        if (super.canSurvive(pState, pLevel, pPos)) {
            return true;
        }


        BlockState blockBelow = pLevel.getBlockState(pPos.below());
        if (blockBelow.is(this)) {
            int bottomBlockAge = blockBelow.getValue(AGE);

            return bottomBlockAge >= TRANSITION_START_AGE && bottomBlockAge <= MAX_AGE;
        }

        return false;
    }
    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        int age = getAge(state);

        if (!world.isClientSide) {
            world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) return;
            Random rand = new Random();


            dropLootForAge(world, pos, age, rand);


            if (age >= TRANSITION_START_AGE && age <= MAX_AGE) {
                BlockPos topPos = pos.above();
                BlockState topState = world.getBlockState(topPos);
                if (topState.getBlock() == this) {
                    int topBlockAge = getAge(topState);
                    dropLootForAge(world, topPos, topBlockAge, rand);
                    world.removeBlock(topPos, false);
                }
            }

            world.removeBlock(pos, false);
        }
    }

    private void dropLootForAge(Level world, BlockPos pos, int age, Random rand) {
        if (age == 1) {
            popResource(world, pos, new ItemStack(ModItems.HEMLOCK_ROOT.get(), 1));
        } else if (age == 5) {
            if (rand.nextBoolean()) {
                popResource(world, pos, new ItemStack(ModItems.HEMLOCK_SEEDS.get(), 1));
            }
            popResource(world, pos, new ItemStack(ModItems.HEMLOCK_FLOWER.get(), 1));
        } else if (age == 8) {
            popResource(world, pos, new ItemStack(ModItems.HEMLOCK_SEEDS.get(), 1 + rand.nextInt(2)));
            popResource(world, pos, new ItemStack(ModItems.HEMLOCK_FLOWER.get(), 1));
        }
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int nextAge = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
        int maxAge = this.getMaxAge();

        if(nextAge > maxAge) {
            nextAge = maxAge;
        }

        if (nextAge >= TRANSITION_START_AGE) {
            int topBlockAge = nextAge + 3;


            pLevel.setBlock(pPos.above(1), this.getStateForAge(topBlockAge), 2);
            pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
        } else {

            pLevel.setBlock(pPos, this.getStateForAge(nextAge), 2);
        }


    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.HEMLOCK_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder){
        pBuilder.add(AGE);
    }


}
