package net.toxicteddie.witchcraft.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.toxicteddie.witchcraft.block.custom.BookOfShadowsBlock;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;


public class WandItem extends Item {
    private final WandWoodType woodType;

    public WandItem(WandWoodType woodType, Properties properties) {
        super(properties);
        this.woodType = woodType;
    }

    public WandWoodType woodType() {
        return woodType;
    }


    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return level.getBlockState(pos).getBlock() instanceof BookOfShadowsBlock
                || super.doesSneakBypassUse(stack, level, pos, player);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            BookOfShadowsFollowingEntity commandBook =
                    BookOfShadowsFollowingEntity.findCommandBook(context.getLevel(), player);

            if (commandBook != null) {
                boolean accepted = commandBook.commandToSurface(
                        player,
                        context.getClickedPos(),
                        context.getClickedFace());

                if (accepted) {
                    return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
                }

                if (!context.getLevel().isClientSide) {
                    player.displayClientMessage(
                            Component.literal("The book cannot hover over that surface."),
                            true);
                }
                return InteractionResult.FAIL;
            }
        }

        return super.useOn(context);
    }


    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public Component getName(ItemStack stack) {
        return woodType.wandDisplayName();
    }
}
