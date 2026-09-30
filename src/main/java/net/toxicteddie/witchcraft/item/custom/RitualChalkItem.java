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
import net.toxicteddie.witchcraft.block.custom.ChalkGlyph;
import net.toxicteddie.witchcraft.block.custom.ChalkGlyphBlock;
import net.toxicteddie.witchcraft.block.custom.ChalkType;

public class RitualChalkItem extends Item {
    private final ChalkType type;

    public RitualChalkItem(ChalkType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos placePos;

        if (clickedState.is(ModBlocks.CHALK_GLYPH.get())) {
            placePos = clickedPos;
        } else {
            if (context.getClickedFace() != Direction.UP) {
                return InteractionResult.PASS;
            }

            placePos = clickedPos.above();
        }

        BlockState existingState = level.getBlockState(placePos);
        BlockPlaceContext placeContext = new BlockPlaceContext(context);

        if (!existingState.isAir()
                && !existingState.canBeReplaced(placeContext)
                && !existingState.is(ModBlocks.CHALK_GLYPH.get())) {
            return InteractionResult.FAIL;
        }

        BlockPos below = placePos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            ChalkGlyph glyph = type == ChalkType.GOLD
                    ? ChalkGlyph.HEART
                    : ChalkGlyph.randomRegular(level.random);

            BlockState chalkState = ModBlocks.CHALK_GLYPH.get().defaultBlockState()
                    .setValue(ChalkGlyphBlock.TYPE, type)
                    .setValue(ChalkGlyphBlock.GLYPH, glyph)
                    .setValue(ChalkGlyphBlock.FACING, context.getHorizontalDirection());

            level.setBlock(placePos, chalkState, 3);

            level.playSound(
                    null,
                    placePos,
                    SoundEvents.WOOL_PLACE,
                    SoundSource.BLOCKS,
                    0.45F,
                    type == ChalkType.GOLD ? 1.35F : 1.0F
            );

            Player player = context.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().hurtAndBreak(1, player,
                        brokenPlayer -> brokenPlayer.broadcastBreakEvent(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public ChalkType getType() {
        return type;
    }
}
