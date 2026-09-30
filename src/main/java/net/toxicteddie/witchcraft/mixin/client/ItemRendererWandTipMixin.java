package net.toxicteddie.witchcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.client.wand.AltarDrawClientState;
import net.toxicteddie.witchcraft.client.wand.WandTipTracker;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemRenderer.class)
public abstract class ItemRendererWandTipMixin {


    private static final float TIP_X = 8.0F / 16.0F;
    private static final float TIP_Y = 8.0F / 16.0F;
    private static final float TIP_Z = 2.80F / 16.0F;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void witchcraft$captureRenderedWandTip(
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHanded,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || !AltarDrawClientState.active()
                || !(stack.getItem() instanceof WandItem)) {
            return;
        }

        boolean heldContext =
                displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        if (!heldContext) {
            return;
        }


        PoseStack tipStack = new PoseStack();


        tipStack.mulPoseMatrix(
                new Matrix4f(
                        poseStack.last().pose()
                )
        );


        model.applyTransform(
                displayContext,
                tipStack,
                leftHanded
        );


        tipStack.translate(
                -0.5F,
                -0.5F,
                -0.5F
        );

        Vector4f tip =
                new Vector4f(
                        TIP_X,
                        TIP_Y,
                        TIP_Z,
                        1.0F
                );

        tipStack.last()
                .pose()
                .transform(tip);

        Camera camera =
                minecraft.gameRenderer
                        .getMainCamera();


        Vector3f left =
                new Vector3f(
                        camera.getLeftVector()
                );

        Vector3f up =
                new Vector3f(
                        camera.getUpVector()
                );

        Vector3f forward =
                new Vector3f(
                        camera.getLookVector()
                );

        Vec3 cameraPos =
                camera.getPosition();

        double worldX =
                cameraPos.x
                        + (-left.x) * tip.x
                        + up.x * tip.y
                        + (-forward.x) * tip.z;

        double worldY =
                cameraPos.y
                        + (-left.y) * tip.x
                        + up.y * tip.y
                        + (-forward.y) * tip.z;

        double worldZ =
                cameraPos.z
                        + (-left.z) * tip.x
                        + up.z * tip.y
                        + (-forward.z) * tip.z;

        WandTipTracker.update(
                displayContext,
                new Vec3(
                        worldX,
                        worldY,
                        worldZ
                )
        );
    }
}
