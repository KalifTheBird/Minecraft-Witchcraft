package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.toxicteddie.witchcraft.block.entity.ModBlockEntities;
import net.toxicteddie.witchcraft.block.entity.WitchesCauldronBlockEntity;
import org.jetbrains.annotations.Nullable;

public class WitchesCauldronBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    private static final VoxelShape SHAPE = Shapes.or(

            box(4, 0, 4, 6, 1, 6),
            box(10, 0, 4, 12, 1, 6),
            box(4, 0, 10, 6, 1, 12),
            box(10, 0, 10, 12, 1, 12),


            box(4, 1, 4, 12, 2, 12),


            box(4, 2, 1, 12, 12, 4),
            box(4, 2, 12, 12, 12, 15),
            box(1, 2, 4, 4, 12, 12),
            box(12, 2, 4, 15, 12, 12)
    );

    public WitchesCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        int waterLevel = state.getValue(LEVEL);

        if (stack.is(Items.WATER_BUCKET) && waterLevel < 3) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LEVEL, waterLevel + 1), 3);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.is(Items.BUCKET) && waterLevel > 0) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LEVEL, waterLevel - 1), 3);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.getAbilities().instabuild) {
                    if (stack.getCount() == 1) {
                        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                    } else {
                        stack.shrink(1);
                        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);

                        if (!player.getInventory().add(waterBucket)) {
                            player.drop(waterBucket, false);
                        }
                    }
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchesCauldronBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.WITCHES_CAULDRON_BE.get(),
                WitchesCauldronBlockEntity::tick
        );
    }
}
