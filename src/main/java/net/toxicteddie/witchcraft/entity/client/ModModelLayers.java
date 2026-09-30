package net.toxicteddie.witchcraft.entity.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;

public class ModModelLayers {
    public static final ModelLayerLocation BOOK_OF_SHADOWS_LAYER =
            new ModelLayerLocation(new ResourceLocation(Witchcraft.MOD_ID, "book_of_shadows"), "main");

    public static final ModelLayerLocation BOOK_OF_SHADOWS_PAPER_LAYER =
            new ModelLayerLocation(new ResourceLocation(Witchcraft.MOD_ID, "book_of_shadows_paper"), "main");

    public static final ModelLayerLocation BOOK_OF_SHADOWS_METAL_LAYER =
            new ModelLayerLocation(new ResourceLocation(Witchcraft.MOD_ID, "book_of_shadows_metal"), "main");

    public static final ModelLayerLocation MANDRAKE_LAYER = new ModelLayerLocation(
            new ResourceLocation(Witchcraft.MOD_ID, "mandrake_layer"), "main");

    public static final ModelLayerLocation DEMON_LAYER = new ModelLayerLocation(
            new ResourceLocation(Witchcraft.MOD_ID, "demon_layer"), "main");

    public static final ModelLayerLocation SHADOW_MAN_LAYER = new ModelLayerLocation(
            new ResourceLocation(Witchcraft.MOD_ID, "shadow_man"), "main");

    public static final ModelLayerLocation IMP_LAYER = new ModelLayerLocation(
            new ResourceLocation(Witchcraft.MOD_ID, "imp_layer"), "main");


    public static final ModelLayerLocation SATCHEL_LAYER =
            new ModelLayerLocation(new ResourceLocation(Witchcraft.MOD_ID, "satchel"), "main");

    public static final ModelLayerLocation WITCHES_HAT_LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(
                            Witchcraft.MOD_ID,
                            "witches_hat"
                    ),
                    "main"
            );

    public static final ModelLayerLocation PESTLE_LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(
                            Witchcraft.MOD_ID,
                            "pestle_layer"
                    ),
                    "main"
            );
}
