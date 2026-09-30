package net.toxicteddie.witchcraft.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.client.model.DemonModel;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.custom.DemonEntity;

public class DemonRenderer extends MobRenderer<DemonEntity, DemonModel<DemonEntity>> {

    public DemonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new DemonModel<>(pContext.bakeLayer(ModModelLayers.DEMON_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(DemonEntity p_115812_) {
        return new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/demon_0.png");
    }

    @Override
    public void render(DemonEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }


}
