package net.toxicteddie.witchcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.client.wand.AltarDrawClientState;
import net.toxicteddie.witchcraft.client.wand.WandTipTracker;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererWandTipMixin {


    private static final float TIP_X = 8.0F / 16.0F;
    private static final float TIP_Y = 8.0F / 16.0F;
    private static final float TIP_Z = 2.80F / 16.0F;

    @Inject(
            method = "renderItem",
            at = @At("HEAD")
    )
    private void witchcraft$captureWandTip(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHanded,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || entity != minecraft.player
                || !AltarDrawClientState.active()
                || !(stack.getItem() instanceof WandItem)) {
            return;
        }

        boolean handContext =
                displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        if (!handContext) return;


        poseStack.pushPose();

        BakedModel model =
                minecraft.getItemRenderer().getModel(
                        stack,
                        entity.level(),
                        entity,
                        entity.getId()
                );

        model = model.applyTransform(
                displayContext,
                poseStack,
                leftHanded
        );


        poseStack.translate(
                -0.5F,
                -0.5F,
                -0.5F
        );

        Matrix4f matrix =
                poseStack.last().pose();

        Vector4f tip =
                new Vector4f(
                        TIP_X,
                        TIP_Y,
                        TIP_Z,
                        1.0F
                );

        matrix.transform(tip);


        var camera =
                minecraft.gameRenderer
                        .getMainCamera();

        Vec3 cameraPos =
                camera.getPosition();

        Vector3f cameraRelative =
                new Vector3f(
                        tip.x,
                        tip.y,
                        tip.z
                );


        org.joml.Quaternionf toWorld =
                new org.joml.Quaternionf(
                        camera.rotation()
                ).conjugate();

        cameraRelative.rotate(
                toWorld
        );

        WandTipTracker.update(
                displayContext,
                new Vec3(
                        cameraPos.x + cameraRelative.x(),
                        cameraPos.y + cameraRelative.y(),
                        cameraPos.z + cameraRelative.z()
                )
        );

        poseStack.popPose();
    }
}
