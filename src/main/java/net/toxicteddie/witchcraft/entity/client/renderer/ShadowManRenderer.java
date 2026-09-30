package net.toxicteddie.witchcraft.entity.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.client.model.ShadowManModel;
import net.toxicteddie.witchcraft.entity.custom.ShadowManEntity;

public class ShadowManRenderer extends MobRenderer<ShadowManEntity, ShadowManModel<ShadowManEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/shadow_man.png");

    public ShadowManRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadowManModel<>(context.bakeLayer(ModModelLayers.SHADOW_MAN_LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowManEntity entity) {
        return TEXTURE;
    }
}
