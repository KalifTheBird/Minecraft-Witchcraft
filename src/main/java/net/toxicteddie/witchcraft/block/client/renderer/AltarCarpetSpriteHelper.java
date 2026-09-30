package net.toxicteddie.witchcraft.block.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public final class AltarCarpetSpriteHelper {

    private AltarCarpetSpriteHelper() {
    }

    public static TextureAtlasSprite findSprite(
            BlockState carpetState
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        BakedModel model =
                minecraft.getBlockRenderer()
                        .getBlockModel(carpetState);

        RandomSource random =
                RandomSource.create(42L);

        List<BakedQuad> topQuads =
                model.getQuads(
                        carpetState,
                        Direction.UP,
                        random,
                        ModelData.EMPTY,
                        null
                );

        if (!topQuads.isEmpty()) {
            return topQuads.get(0).getSprite();
        }

        random.setSeed(42L);

        List<BakedQuad> generalQuads =
                model.getQuads(
                        carpetState,
                        null,
                        random,
                        ModelData.EMPTY,
                        null
                );

        if (!generalQuads.isEmpty()) {
            return generalQuads
                    .get(0)
                    .getSprite();
        }

        return model.getParticleIcon(
                ModelData.EMPTY
        );
    }
}
