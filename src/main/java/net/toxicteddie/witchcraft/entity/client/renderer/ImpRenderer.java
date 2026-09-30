package net.toxicteddie.witchcraft.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.client.model.ImpModel;
import net.toxicteddie.witchcraft.entity.client.model.MandrakeModel;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.custom.ImpEntity;
import net.toxicteddie.witchcraft.entity.custom.MandrakeEntity;

public class ImpRenderer extends MobRenderer<ImpEntity, ImpModel<ImpEntity>> {

    public ImpRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new ImpModel<>(pContext.bakeLayer(ModModelLayers.IMP_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(ImpEntity p_115812_) {
        return new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/imp_0.png");
    }

    @Override
    public void render(ImpEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }


}
