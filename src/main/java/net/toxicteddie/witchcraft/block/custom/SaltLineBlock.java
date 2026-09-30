package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.toxicteddie.witchcraft.entity.custom.DemonEntity;
import net.toxicteddie.witchcraft.entity.custom.ImpEntity;

public class SaltLineBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;


    private static final VoxelShape CENTER_SHAPE = Block.box(6, 0, 6, 10, 0.1, 10);
    private static final VoxelShape NORTH_SHAPE = Block.box(6, 0, 0, 10, 0.1, 6);
    private static final VoxelShape EAST_SHAPE = Block.box(10, 0, 6, 16, 0.1, 10);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6, 0, 10, 10, 0.1, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 6, 6, 0.1, 10);


    private static final VoxelShape CENTER_BARRIER_SHAPE = Block.box(5, 0, 5, 11, 16, 11);
    private static final VoxelShape NORTH_BARRIER_SHAPE = Block.box(5, 0, 0, 11, 16, 6);
    private static final VoxelShape EAST_BARRIER_SHAPE = Block.box(10, 0, 5, 16, 16, 11);
    private static final VoxelShape SOUTH_BARRIER_SHAPE = Block.box(5, 0, 10, 11, 16, 16);
    private static final VoxelShape WEST_BARRIER_SHAPE = Block.box(0, 0, 5, 6, 16, 11);

    public SaltLineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectedState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    public BlockState getConnectedState(BlockState state, BlockGetter level, BlockPos pos) {
        return state
                .setValue(NORTH, connectsTo(level, pos.north()))
                .setValue(EAST, connectsTo(level, pos.east()))
                .setValue(SOUTH, connectsTo(level, pos.south()))
                .setValue(WEST, connectsTo(level, pos.west()));
    }

    private boolean connectsTo(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(this);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (direction.getAxis().isHorizontal()) {
            return getConnectedState(state, level, currentPos);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CENTER_SHAPE;

        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }

        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }

        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }

        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }

        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof LivingEntity livingEntity
                && (
                livingEntity.getMobType() == MobType.UNDEAD
                        || livingEntity instanceof DemonEntity
                        || livingEntity instanceof ImpEntity
        )) {

            return getSaltBarrierShape(state);
        }


        return Shapes.empty();
    }

    private VoxelShape getSaltBarrierShape(BlockState state) {
        VoxelShape shape = CENTER_BARRIER_SHAPE;

        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_BARRIER_SHAPE);
        }

        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_BARRIER_SHAPE);
        }

        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_BARRIER_SHAPE);
        }

        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_BARRIER_SHAPE);
        }

        return shape;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
