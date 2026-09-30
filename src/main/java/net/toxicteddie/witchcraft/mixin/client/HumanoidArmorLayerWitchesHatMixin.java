package net.toxicteddie.witchcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.item.client.model.WitchesHatModel;
import net.toxicteddie.witchcraft.item.custom.WitchesHatItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerWitchesHatMixin {
    private WitchesHatModel<LivingEntity> witchcraft$ribbonModel;

    @Inject(method = "renderArmorPiece", at = @At("TAIL"))
    private void witchcraft$renderWitchesHatRibbon(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity livingEntity,
            EquipmentSlot equipmentSlot,
            int packedLight,
            HumanoidModel<?> vanillaArmorModel,
            CallbackInfo ci
    ) {
        if (equipmentSlot != EquipmentSlot.HEAD) {
            return;
        }

        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        if (!(stack.getItem() instanceof WitchesHatItem)) {
            return;
        }

        ResourceLocation ribbonTexture = WitchesHatItem.getRibbonTexture(stack);
        if (ribbonTexture == null) {
            return;
        }

        if (witchcraft$ribbonModel == null) {
            witchcraft$ribbonModel = new WitchesHatModel<>(
                    Minecraft.getInstance()
                            .getEntityModels()
                            .bakeLayer(ModModelLayers.WITCHES_HAT_LAYER)
            );
        }


        ForgeHooksClient.copyModelProperties(vanillaArmorModel, witchcraft$ribbonModel);

        VertexConsumer ribbonBuffer = bufferSource.getBuffer(
                RenderType.armorCutoutNoCull(ribbonTexture)
        );

        witchcraft$ribbonModel.renderToBuffer(
                poseStack,
                ribbonBuffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
