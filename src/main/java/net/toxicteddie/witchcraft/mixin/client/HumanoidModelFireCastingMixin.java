package net.toxicteddie.witchcraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.toxicteddie.witchcraft.client.power.FirePowerInputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidModel.class)
public abstract class HumanoidModelFireCastingMixin {

    @Shadow public ModelPart head;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void witchcraft$applyFireCastingArm(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();


        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }


        if (minecraft.player == null
                || entity != minecraft.player
                || !FirePowerInputHandler.isPrimaryFlameBlastActive()) {
            return;
        }

        HumanoidArm dominant = minecraft.player.getMainArm();
        ModelPart arm = dominant == HumanoidArm.RIGHT ? rightArm : leftArm;


        float side = dominant == HumanoidArm.RIGHT ? 1.0F : -1.0F;


        float breathe = Mth.sin(ageInTicks * 0.16F);
        float drift = Mth.sin(ageInTicks * 0.085F + 1.10F);
        float pulse = Mth.sin(ageInTicks * 0.31F + 0.45F);


        arm.xRot = -((float) Math.PI / 2.0F)
                + head.yRot * side
                + drift * 0.012F;

        arm.yRot = -head.xRot * side
                - 0.085F * side
                + breathe * 0.010F * side
                + pulse * 0.004F * side;

        arm.zRot = ((float) Math.PI / 2.0F) * side
                + breathe * 0.006F * side;


        arm.x += (0.075F + drift * 0.012F) * side;
        arm.y += breathe * 0.012F;
    }
}
