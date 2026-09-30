package net.toxicteddie.witchcraft.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.toxicteddie.witchcraft.block.custom.WitchesCauldronBlock;
import net.toxicteddie.witchcraft.block.entity.WitchesCauldronBlockEntity;
import org.joml.Matrix4f;

public class WitchesCauldronRenderer implements BlockEntityRenderer<WitchesCauldronBlockEntity> {
    private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("minecraft", "block/water_still");


    private static final float MIXING_ITEM_HEIGHT_OFFSET = -0.03F;


    private static final float[] HEIGHTS = {
            0.0F,
            0.25F,
            0.4375F,
            0.625F
    };

    public WitchesCauldronRenderer(BlockEntityRendererProvider.Context context) {
    }


    private static int getSpriteMixedColor(WitchesCauldronBlockEntity blockEntity) {
        List<ItemStack> ingredients = blockEntity.getMixture();
        if (ingredients.isEmpty()) return 0x3F76E4;
        long red = 0, green = 0, blue = 0;
        for (int i = 0; i < ingredients.size(); i++) {
            int color = sampleOpaqueSpritePixel(ingredients.get(i), i * 31 + blockEntity.getBlockPos().hashCode());
            red += (color >> 16) & 255;
            green += (color >> 8) & 255;
            blue += color & 255;
        }
        int count = ingredients.size();
        return ((int)(red / count) << 16) | ((int)(green / count) << 8) | (int)(blue / count);
    }

    private static int sampleOpaqueSpritePixel(ItemStack stack, int seed) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Object model = minecraft.getItemRenderer().getModel(stack, null, null, seed);
            Method particleMethod = model.getClass().getMethod("getParticleIcon");
            Object sprite = particleMethod.invoke(model);
            Method contentsMethod = sprite.getClass().getMethod("contents");
            Object contents = contentsMethod.invoke(sprite);
            Method imageMethod = contents.getClass().getDeclaredMethod("getOriginalImage");
            imageMethod.setAccessible(true);
            Object image = imageMethod.invoke(contents);
            Method widthMethod = image.getClass().getMethod("getWidth");
            Method heightMethod = image.getClass().getMethod("getHeight");
            Method pixelMethod = image.getClass().getMethod("getPixelRGBA", int.class, int.class);
            int width = (int) widthMethod.invoke(image);
            int height = (int) heightMethod.invoke(image);
            int start = Math.floorMod(seed, Math.max(1, width * height));
            for (int offset = 0; offset < width * height; offset++) {
                int index = (start + offset * 17) % (width * height);
                int abgr = (int) pixelMethod.invoke(image, index % width, index / width);
                int alpha = (abgr >>> 24) & 255;
                if (alpha > 32) {
                    int red = abgr & 255;
                    int green = (abgr >>> 8) & 255;
                    int blue = (abgr >>> 16) & 255;
                    return (red << 16) | (green << 8) | blue;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
        net.minecraft.resources.ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        int hash = id == null ? 0x3F76E4 : id.toString().hashCode();
        return 0x303030 | (hash & 0xCFCFCF);
    }

    @Override
    public void render(WitchesCauldronBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();

        if (!state.hasProperty(WitchesCauldronBlock.LEVEL)) {
            return;
        }

        int level = state.getValue(WitchesCauldronBlock.LEVEL);

        if (level <= 0) {
            return;
        }

        float y = HEIGHTS[level];
        float swirl = blockEntity.getAnimationTicks() > 0 ? (float)Math.sin((blockEntity.getAnimationTicks() + partialTick) * 0.45F) * 0.006F : 0.0F;
        y += swirl;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(WATER_TEXTURE);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        float min = 3.0F / 16.0F;
        float max = 13.0F / 16.0F;

        float u0 = sprite.getU(3);
        float u1 = sprite.getU(13);
        float v0 = sprite.getV(3);
        float v1 = sprite.getV(13);

        int color = getSpriteMixedColor(blockEntity);
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = 255;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();


        consumer.vertex(matrix, min, y, max)
                .color(r, g, b, a)
                .uv(u0, v1)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        consumer.vertex(matrix, max, y, max)
                .color(r, g, b, a)
                .uv(u1, v1)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        consumer.vertex(matrix, max, y, min)
                .color(r, g, b, a)
                .uv(u1, v0)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        consumer.vertex(matrix, min, y, min)
                .color(r, g, b, a)
                .uv(u0, v0)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        poseStack.popPose();

        if (blockEntity.isBrewingShowActive()) {
            renderBrewingShow(blockEntity, partialTick, y, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }


    private static void renderBrewingShow(WitchesCauldronBlockEntity blockEntity,
                                          float partialTick,
                                          float liquidY,
                                          PoseStack poseStack,
                                          MultiBufferSource bufferSource,
                                          int packedLight,
                                          int packedOverlay) {
        int duration = Math.max(1, blockEntity.getBrewingShowDuration());
        float remaining = Math.max(0.0F, blockEntity.getBrewingShowTicks() - partialTick);
        float progress = 1.0F - remaining / duration;
        progress = clamp(progress, 0.0F, 1.0F);

        List<ItemStack> ingredients = blockEntity.getMixture();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
        float time = gameTime + partialTick;

        int visibleCount = Math.min(ingredients.size(), 8);
        for (int i = 0; i < visibleCount; i++) {
            ItemStack stack = ingredients.get(i);
            if (stack.isEmpty()) continue;


            float local = clamp((progress - i * 0.018F) / (1.0F - i * 0.018F), 0.0F, 1.0F);
            if (local >= 0.93F) continue;

            float baseAngle = (float)(Math.PI * 2.0 * i / Math.max(1, visibleCount));
            float angle = baseAngle + time * 0.030F + local * 2.15F;
            float radius;
            float height;
            float scale;

            if (local < 0.20F) {


                float t = smooth(local / 0.20F);
                radius = lerp(0.07F, 0.22F, t);
                height = lerp(liquidY - 0.11F, liquidY + MIXING_ITEM_HEIGHT_OFFSET, t);
                scale = lerp(0.31F, 0.53F, t);
            } else if (local < 0.76F) {


                float t = (local - 0.20F) / 0.56F;
                radius = lerp(0.22F, 0.13F, smooth(t));
                float bob = (float)Math.sin(time * 0.075F + i * 1.91F) * 0.018F
                        + (float)Math.sin(time * 0.031F + i * 0.73F) * 0.007F;
                height = liquidY + MIXING_ITEM_HEIGHT_OFFSET + bob;
                scale = 0.53F;
            } else {


                float t = smooth((local - 0.76F) / 0.17F);
                radius = lerp(0.13F, 0.015F, t);
                height = lerp(liquidY + MIXING_ITEM_HEIGHT_OFFSET, liquidY - 0.13F, t);
                scale = lerp(0.53F, 0.14F, t);
            }

            float x = 0.5F + (float)Math.cos(angle) * radius;
            float z = 0.5F + (float)Math.sin(angle) * radius;
            float tilt = (float)Math.sin(time * 0.045F + i * 1.37F) * 7.0F;

            poseStack.pushPose();
            poseStack.translate(x, height, z);
            poseStack.mulPose(Axis.YP.rotation(angle + time * 0.010F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
            poseStack.scale(scale, scale, scale);
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND,
                    packedLight, packedOverlay, poseStack, bufferSource,
                    blockEntity.getLevel(), blockEntity.getBlockPos().hashCode() + i);
            poseStack.popPose();
        }


        if (progress > 0.86F) {
            ItemStack output = blockEntity.getPendingOutput();
            if (!output.isEmpty()) {
                float t = smooth((progress - 0.86F) / 0.14F);
                float bob = (float)Math.sin(time * 0.08F) * 0.018F * t;
                float outputY = lerp(liquidY - 0.035F, liquidY + 0.42F, t) + bob;
                float outputScale = lerp(0.18F, 0.62F, t);

                poseStack.pushPose();
                poseStack.translate(0.5F, outputY, 0.5F);
                poseStack.mulPose(Axis.YP.rotation(time * 0.018F));
                poseStack.scale(outputScale, outputScale, outputScale);
                itemRenderer.renderStatic(output, ItemDisplayContext.GROUND,
                        packedLight, packedOverlay, poseStack, bufferSource,
                        blockEntity.getLevel(), blockEntity.getBlockPos().hashCode() ^ 0x51A7);
                poseStack.popPose();
            }
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private static float smooth(float value) {
        value = clamp(value, 0.0F, 1.0F);
        return value * value * (3.0F - 2.0F * value);
    }
}
