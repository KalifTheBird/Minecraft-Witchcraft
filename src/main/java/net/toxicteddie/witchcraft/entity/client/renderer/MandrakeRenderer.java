package net.toxicteddie.witchcraft.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.client.model.MandrakeModel;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.custom.MandrakeEntity;

public class MandrakeRenderer extends MobRenderer<MandrakeEntity, MandrakeModel<MandrakeEntity>> {

    public MandrakeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MandrakeModel<>(pContext.bakeLayer(ModModelLayers.MANDRAKE_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(MandrakeEntity p_115812_) {
        return new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/mandrake.png");
    }

    @Override
    public void render(MandrakeEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }


}
