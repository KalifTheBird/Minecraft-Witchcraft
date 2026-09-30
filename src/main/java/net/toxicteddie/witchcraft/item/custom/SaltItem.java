package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.toxicteddie.witchcraft.block.ModBlocks;
import net.toxicteddie.witchcraft.block.custom.SaltLineBlock;

public class SaltItem extends Item {
    public SaltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (clickedState.is(ModBlocks.SALT_LINE.get())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = clickedPos.above();
        BlockState existingState = level.getBlockState(placePos);
        BlockPlaceContext placeContext = new BlockPlaceContext(context);

        if (!existingState.isAir() && !existingState.canBeReplaced(placeContext)) {
            return InteractionResult.FAIL;
        }

        BlockPos below = placePos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            BlockState saltState = ((SaltLineBlock) ModBlocks.SALT_LINE.get())
                    .getConnectedState(ModBlocks.SALT_LINE.get().defaultBlockState(), level, placePos);

            level.setBlock(placePos, saltState, 3);

            level.playSound(
                    null,
                    placePos,
                    SoundEvents.SAND_PLACE,
                    SoundSource.BLOCKS,
                    0.45F,
                    1.2F
            );

            Player player = context.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
