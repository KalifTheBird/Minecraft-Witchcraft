package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class StrippableLogBlock extends RotatedPillarBlock {

    private final Supplier<? extends Block> strippedBlock;

    public StrippableLogBlock(
            Properties properties,
            Supplier<? extends Block> strippedBlock
    ) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(
            BlockState state,
            UseOnContext context,
            ToolAction toolAction,
            boolean simulate
    ) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return strippedBlock.get()
                    .defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS));
        }

        return super.getToolModifiedState(
                state,
                context,
                toolAction,
                simulate
        );
    }

    @Override
    public int getFlammability(
            BlockState state,
            BlockGetter level,
            net.minecraft.core.BlockPos pos,
            Direction direction
    ) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(
            BlockState state,
            BlockGetter level,
            net.minecraft.core.BlockPos pos,
            Direction direction
    ) {
        return 5;
    }
}
