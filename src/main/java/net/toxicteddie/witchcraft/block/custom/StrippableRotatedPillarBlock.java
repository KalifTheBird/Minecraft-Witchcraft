package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class StrippableRotatedPillarBlock extends RotatedPillarBlock {
    private final Supplier<? extends Block> strippedBlock;

    public StrippableRotatedPillarBlock(Properties properties, Supplier<? extends Block> strippedBlock) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context,
                                                      ToolAction toolAction, boolean simulate) {
        if (toolAction != ToolActions.AXE_STRIP) {
            return super.getToolModifiedState(state, context, toolAction, simulate);
        }

        Direction.Axis axis = state.getValue(AXIS);
        return strippedBlock.get().defaultBlockState().setValue(AXIS, axis);
    }
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

}
