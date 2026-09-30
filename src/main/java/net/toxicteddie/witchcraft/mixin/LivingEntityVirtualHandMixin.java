package net.toxicteddie.witchcraft.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.toxicteddie.witchcraft.power.PlayerElementSelectionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityVirtualHandMixin {

    @Inject(
            method = "getItemInHand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void witchcraft$virtualEmptyInteractionHand(
            InteractionHand hand,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        Object self = this;

        if (self instanceof Player player
                && hand == InteractionHand.MAIN_HAND
                && PlayerElementSelectionManager.isPowerMode(player)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(
            method = "getMainHandItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void witchcraft$virtualEmptyMainHand(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        Object self = this;

        if (self instanceof Player player
                && PlayerElementSelectionManager.isPowerMode(player)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
