package net.toxicteddie.witchcraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.client.power.TelekinesisInputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidModel.class)
public abstract class HumanoidModelTelekinesisCastingMixin {

    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void witchcraft$pointArmAtTelekinesisTarget(
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
                || !TelekinesisInputHandler.isMainTelekinesisActive()) {
            return;
        }

        Vec3 target = TelekinesisInputHandler.getHeldTargetCenter(
                minecraft.getFrameTime()
        );
        if (target == null) return;

        Vec3 shoulder = entity.position().add(
                0.0D,
                entity.getBbHeight() * 0.78D,
                0.0D
        );
        Vec3 direction = target.subtract(shoulder);
        if (direction.lengthSqr() < 1.0E-6D) return;

        double horizontal = Math.sqrt(
                direction.x * direction.x + direction.z * direction.z
        );

        float targetPitch = (float) -Math.atan2(direction.y, horizontal);
        float targetYawWorld = (float) Math.atan2(-direction.x, direction.z);
        float bodyYaw = entity.yBodyRot * Mth.DEG_TO_RAD;
        float relativeYaw = Mth.wrapDegrees(
                (targetYawWorld - bodyYaw) * Mth.RAD_TO_DEG
        ) * Mth.DEG_TO_RAD;

        HumanoidArm dominant = minecraft.player.getMainArm();
        ModelPart arm = dominant == HumanoidArm.RIGHT ? rightArm : leftArm;
        float side = dominant == HumanoidArm.RIGHT ? 1.0F : -1.0F;

        float breathe = Mth.sin(ageInTicks * 0.16F);
        float drift = Mth.sin(ageInTicks * 0.085F + 1.1F);


        arm.xRot = -((float) Math.PI / 2.0F)
                + relativeYaw * side
                + drift * 0.007F;

        arm.yRot = -targetPitch * side
                - 0.08F * side
                + breathe * 0.008F * side;

        arm.zRot = ((float) Math.PI / 2.0F) * side
                + breathe * 0.005F * side;
    }
}
