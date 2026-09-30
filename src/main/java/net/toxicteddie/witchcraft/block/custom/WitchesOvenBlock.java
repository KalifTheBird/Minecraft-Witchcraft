package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.toxicteddie.witchcraft.block.entity.ModBlockEntities;
import net.toxicteddie.witchcraft.block.entity.WitchesOvenBlockEntity;
import org.jetbrains.annotations.Nullable;

public class WitchesOvenBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    private static final VoxelShape FRONT_LEFT_FOOT =
            box(
                    2.0D, 0.0D, 2.5D,
                    4.0D, 2.0D, 4.5D
            );

    private static final VoxelShape FRONT_RIGHT_FOOT =
            box(
                    12.0D, 0.0D, 2.5D,
                    14.0D, 2.0D, 4.5D
            );

    private static final VoxelShape BACK_LEFT_FOOT =
            box(
                    2.0D, 0.0D, 12.5D,
                    4.0D, 2.0D, 14.5D
            );

    private static final VoxelShape BACK_RIGHT_FOOT =
            box(
                    12.0D, 0.0D, 12.5D,
                    14.0D, 2.0D, 14.5D
            );


    private static final VoxelShape BODY =
            box(
                    1.0D, 2.0D, 1.5D,
                    15.0D, 15.0D, 15.5D
            );


    private static final VoxelShape FRONT =
            box(
                    3.0D, 3.0D, 0.0D,
                    13.0D, 11.0D, 2.0D
            );


    private static final VoxelShape CHIMNEY =
            box(
                    1.0D, 15.0D, 9.5D,
                    5.5D, 19.5D, 13.5D
            );


    private static final VoxelShape SHAPE =
            Shapes.or(
                    BODY,
                    FRONT,
                    CHIMNEY,
                    FRONT_LEFT_FOOT,
                    FRONT_RIGHT_FOOT,
                    BACK_LEFT_FOOT,
                    BACK_RIGHT_FOOT
            );


    private static final VoxelShape COLLISION_SHAPE =
            Shapes.or(
                    BODY,
                    CHIMNEY,
                    FRONT_LEFT_FOOT,
                    FRONT_RIGHT_FOOT,
                    BACK_LEFT_FOOT,
                    BACK_RIGHT_FOOT
            );

    public WitchesOvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.32D;
        double y = pos.getY() + 0.34D + random.nextDouble() * 0.22D;
        double z = pos.getZ() - 0.015D;

        level.addParticle(
                ParticleTypes.SMOKE,
                x,
                y,
                z,
                0.0D,
                0.0D,
                -0.01D
        );

        level.addParticle(
                ParticleTypes.FLAME,
                x,
                y,
                z,
                0.0D,
                0.0D,
                0.0D
        );
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.WITCHES_OVEN_BE.get(), WitchesOvenBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof WitchesOvenBlockEntity oven) {
                NetworkHooks.openScreen(serverPlayer, oven, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof WitchesOvenBlockEntity oven) {
                for (int i = 0; i < WitchesOvenBlockEntity.SLOT_COUNT; i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), oven.getItemHandler().getStackInSlot(i));
                }
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }


    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }


    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return COLLISION_SHAPE;
    }
}
