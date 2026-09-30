package net.toxicteddie.witchcraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.client.wand.AltarDrawClientState;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidModel.class)
public abstract class HumanoidModelWandAltarDrawMixin {
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void witchcraft$poseWandAltarDraw(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.getCameraType().isFirstPerson()
                || minecraft.player == null
                || entity != minecraft.player
                || !AltarDrawClientState.active()
                || !(minecraft.player.getMainHandItem().getItem() instanceof WandItem
                || minecraft.player.getOffhandItem().getItem() instanceof WandItem)) {
            return;
        }

        var altar = AltarDrawClientState.altarPos();
        if (altar == null) return;

        Vec3 shoulder = entity.position().add(
                0.0D,
                entity.getBbHeight() * 0.78D,
                0.0D
        );
        Vec3 target = new Vec3(
                altar.getX() + 0.5D,
                altar.getY() + 1.10D,
                altar.getZ() + 0.5D
        );
        Vec3 direction = target.subtract(shoulder);
        if (direction.lengthSqr() < 1.0E-6D) return;

        double horizontal = Math.sqrt(
                direction.x * direction.x + direction.z * direction.z
        );

        float targetPitch = (float)-Math.atan2(direction.y, horizontal);
        float targetYawWorld = (float)Math.atan2(-direction.x, direction.z);
        float bodyYaw = entity.yBodyRot * Mth.DEG_TO_RAD;
        float relativeYaw = Mth.wrapDegrees(
                (targetYawWorld - bodyYaw) * Mth.RAD_TO_DEG
        ) * Mth.DEG_TO_RAD;

        boolean mainWand =
                minecraft.player.getMainHandItem().getItem() instanceof WandItem;
        HumanoidArm castingArm = mainWand
                ? minecraft.player.getMainArm()
                : minecraft.player.getMainArm().getOpposite();

        ModelPart arm = castingArm == HumanoidArm.RIGHT ? rightArm : leftArm;
        float side = castingArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;


        float swirlX = Mth.cos(ageInTicks * 0.11F);
        float swirlY = Mth.sin(ageInTicks * 0.11F);
        float breathe = Mth.sin(ageInTicks * 0.055F + 0.6F);


        arm.xRot = -((float)Math.PI / 2.0F)
                + targetPitch
                + swirlY * 0.022F;

        arm.yRot = relativeYaw
                - 0.085F * side
                + swirlX * 0.020F * side;


        arm.zRot = (-0.24F
                + swirlY * 0.032F
                + breathe * 0.006F) * side;
    }
}
