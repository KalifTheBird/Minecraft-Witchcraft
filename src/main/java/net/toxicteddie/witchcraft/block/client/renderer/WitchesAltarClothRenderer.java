package net.toxicteddie.witchcraft.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.custom.WitchesAltarBlock;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class WitchesAltarClothRenderer
        implements BlockEntityRenderer<WitchesAltarBlockEntity> {

    private static final ResourceLocation CLOTH_MODEL =
            new ResourceLocation(
                    Witchcraft.MOD_ID,
                    "block/witches_altar_cloth"
            );

    private static final ResourceLocation REPLACEABLE_TEXTURE =
            new ResourceLocation(
                    "minecraft",
                    "block/red_wool"
            );


    private static final Vec3[] DISPLAY_POSITIONS = {


            new Vec3(0.10D, 1.00D, 0.75D),
            new Vec3(0.90D, 1.00D, 0.75D),


            new Vec3(0.50D, 1.05D, 0.30D),
            new Vec3(0.80D, 1.05D, 0.43D),
            new Vec3(1.25D, 1.05D, 0.60D),
            new Vec3(0.20D, 1.05D, 0.43D),
            new Vec3(-0.25D, 1.05D, 0.60D)
    };

    public WitchesAltarClothRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            WitchesAltarBlockEntity altar,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (!altar.hasCarpet()
                || altar.getLevel() == null) {
            return;
        }

        Direction facing =
                altar.getBlockState()
                        .getValue(
                                WitchesAltarBlock.FACING
                        );


        poseStack.pushPose();

        poseStack.translate(
                0.5D,
                0.0D,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        getFacingRotation(facing)
                )
        );

        poseStack.translate(
                -0.5D,
                0.0D,
                -0.5D
        );


        if (facing == Direction.EAST
                || facing == Direction.WEST) {

            poseStack.translate(
                    0.0D,
                    0.0D,
                    -0.125D
            );
        }

        renderCloth(
                altar,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();


        renderDisplayedObjects(
                altar,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
    }


    private static void renderCloth(
            WitchesAltarBlockEntity altar,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        BlockState carpetState =
                altar.getCarpetState();

        TextureAtlasSprite carpetSprite =
                AltarCarpetSpriteHelper.findSprite(
                        carpetState
                );

        BakedModel clothModel =
                Minecraft.getInstance()
                        .getModelManager()
                        .getModel(CLOTH_MODEL);

        int tint =
                getCarpetTint(
                        altar,
                        carpetState
                );

        float red =
                ((tint >> 16) & 255) / 255.0F;

        float green =
                ((tint >> 8) & 255) / 255.0F;

        float blue =
                (tint & 255) / 255.0F;

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        RenderType.entityCutout(
                                InventoryMenu.BLOCK_ATLAS
                        )
                );

        renderModel(
                clothModel,
                carpetSprite,
                poseStack,
                consumer,
                packedLight,
                red,
                green,
                blue
        );
    }

    private static void renderModel(
            BakedModel model,
            TextureAtlasSprite carpetSprite,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            float red,
            float green,
            float blue
    ) {
        RandomSource random =
                RandomSource.create(42L);

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);

            List<BakedQuad> quads =
                    model.getQuads(
                            null,
                            direction,
                            random,
                            ModelData.EMPTY,
                            null
                    );

            for (BakedQuad quad : quads) {
                renderQuad(
                        quad,
                        carpetSprite,
                        poseStack,
                        consumer,
                        packedLight,
                        red,
                        green,
                        blue
                );
            }
        }

        random.setSeed(42L);

        List<BakedQuad> generalQuads =
                model.getQuads(
                        null,
                        null,
                        random,
                        ModelData.EMPTY,
                        null
                );

        for (BakedQuad quad : generalQuads) {
            renderQuad(
                    quad,
                    carpetSprite,
                    poseStack,
                    consumer,
                    packedLight,
                    red,
                    green,
                    blue
            );
        }
    }

    private static void renderQuad(
            BakedQuad quad,
            TextureAtlasSprite carpetSprite,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            float red,
            float green,
            float blue
    ) {
        int[] vertices =
                quad.getVertices();

        TextureAtlasSprite originalSprite =
                quad.getSprite();

        boolean replaceTexture =
                originalSprite.contents()
                        .name()
                        .equals(
                                REPLACEABLE_TEXTURE
                        );

        TextureAtlasSprite targetSprite =
                replaceTexture
                        ? carpetSprite
                        : originalSprite;

        float colorRed =
                replaceTexture
                        ? red
                        : 1.0F;

        float colorGreen =
                replaceTexture
                        ? green
                        : 1.0F;

        float colorBlue =
                replaceTexture
                        ? blue
                        : 1.0F;

        PoseStack.Pose pose =
                poseStack.last();

        Matrix4f poseMatrix =
                pose.pose();

        Matrix3f normalMatrix =
                pose.normal();

        Direction direction =
                quad.getDirection();

        float normalX =
                direction.getStepX();

        float normalY =
                direction.getStepY();

        float normalZ =
                direction.getStepZ();

        int stride =
                vertices.length / 4;

        for (int vertex = 0;
             vertex < 4;
             vertex++) {

            int offset =
                    vertex * stride;

            float x =
                    Float.intBitsToFloat(
                            vertices[offset]
                    );

            float y =
                    Float.intBitsToFloat(
                            vertices[offset + 1]
                    );

            float z =
                    Float.intBitsToFloat(
                            vertices[offset + 2]
                    );

            float originalU =
                    Float.intBitsToFloat(
                            vertices[offset + 4]
                    );

            float originalV =
                    Float.intBitsToFloat(
                            vertices[offset + 5]
                    );

            float mappedU =
                    originalU;

            float mappedV =
                    originalV;

            if (replaceTexture) {
                float relativeU =
                        inverseLerp(
                                originalSprite.getU0(),
                                originalSprite.getU1(),
                                originalU
                        );

                float relativeV =
                        inverseLerp(
                                originalSprite.getV0(),
                                originalSprite.getV1(),
                                originalV
                        );

                mappedU =
                        Mth.lerp(
                                relativeU,
                                targetSprite.getU0(),
                                targetSprite.getU1()
                        );

                mappedV =
                        Mth.lerp(
                                relativeV,
                                targetSprite.getV0(),
                                targetSprite.getV1()
                        );
            }

            consumer.vertex(
                            poseMatrix,
                            x,
                            y,
                            z
                    )
                    .color(
                            colorRed,
                            colorGreen,
                            colorBlue,
                            1.0F
                    )
                    .uv(
                            mappedU,
                            mappedV
                    )
                    .overlayCoords(
                            OverlayTexture.NO_OVERLAY
                    )
                    .uv2(packedLight)
                    .normal(
                            normalMatrix,
                            normalX,
                            normalY,
                            normalZ
                    )
                    .endVertex();
        }
    }


    private static void renderDisplayedObjects(
            WitchesAltarBlockEntity altar,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing =
                altar.getBlockState()
                        .getValue(
                                WitchesAltarBlock.FACING
                        );

        poseStack.pushPose();


        poseStack.translate(
                0.5D,
                0.0D,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        getFacingRotation(facing)
                )
        );

        poseStack.translate(
                -0.5D,
                0.0D,
                -0.5D
        );

        for (int slot = 0;
             slot < WitchesAltarBlockEntity.SLOT_COUNT;
             slot++) {

            ItemStack stack =
                    altar.getDisplayedItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            poseStack.pushPose();

            Vec3 position =
                    getDisplayPositionForFacing(
                            DISPLAY_POSITIONS[slot],
                            facing
                    );

            poseStack.translate(
                    position.x,
                    position.y,
                    position.z
            );

            if (WitchesAltarBlockEntity
                    .isCandleSlot(slot)
                    && isRenderedCandle(stack)) {

                renderCandle(
                        stack,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
            } else {
                renderTrinket(
                        altar,
                        slot,
                        stack,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }


    private static Vec3 getDisplayPositionForFacing(
            Vec3 position,
            Direction facing
    ) {
        if (facing == Direction.EAST
                || facing == Direction.WEST) {
            return new Vec3(
                    position.x,
                    position.y,
                    1.0D - position.z
            );
        }

        return position;
    }

    private static boolean isRenderedCandle(
            ItemStack stack
    ) {
        return stack.getItem()
                instanceof BlockItem blockItem
                && blockItem.getBlock()
                instanceof CandleBlock;
    }

    private static void renderCandle(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (!(stack.getItem()
                instanceof BlockItem blockItem)) {
            return;
        }

        BlockState candleState =
                blockItem.getBlock()
                        .defaultBlockState();


        poseStack.translate(
                -0.5D,
                0.0D,
                -0.5D
        );

        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        candleState,
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
    }

    private static void renderTrinket(
            WitchesAltarBlockEntity altar,
            int slot,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        float rotation =
                altar.getTrinketAngle(slot) * 45.0F;

        if (usesBlockItemModel(stack)) {


            poseStack.mulPose(
                    Axis.YP.rotationDegrees(rotation)
            );


            poseStack.translate(
                    0.0D,
                    getBlockItemHeightOffset(stack),
                    0.0D
            );

            poseStack.scale(
                    0.45F,
                    0.45F,
                    0.45F
            );
        } else {


            poseStack.mulPose(
                    Axis.XP.rotationDegrees(90.0F)
            );


            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(rotation)
            );

            poseStack.scale(
                    0.30F,
                    0.30F,
                    0.30F
            );
        }

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        packedLight,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        altar.getLevel(),
                        (int) altar.getBlockPos().asLong()
                );
    }
    private static double getBlockItemHeightOffset(
            ItemStack stack
    ) {
        if (!(stack.getItem()
                instanceof BlockItem blockItem)) {
            return 0.0D;
        }


        if (blockItem.getBlock()
                instanceof AbstractSkullBlock) {
            return 0.08D;
        }


        return 0.06D;
    }

    private static boolean usesBlockItemModel(
            ItemStack stack
    ) {
        return stack.getItem()
                instanceof BlockItem;
    }


    private static float getFacingRotation(
            Direction facing
    ) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float inverseLerp(
            float start,
            float end,
            float value
    ) {
        float difference =
                end - start;

        if (Math.abs(difference)
                < 0.00001F) {
            return 0.0F;
        }

        return (value - start)
                / difference;
    }

    private static int getCarpetTint(
            WitchesAltarBlockEntity altar,
            BlockState carpetState
    ) {
        BlockColors blockColors =
                Minecraft.getInstance()
                        .getBlockColors();

        int tint =
                blockColors.getColor(
                        carpetState,
                        altar.getLevel(),
                        altar.getBlockPos(),
                        0
                );

        return tint == -1
                ? 0xFFFFFF
                : tint;
    }

    @Override
    public boolean shouldRenderOffScreen(
            WitchesAltarBlockEntity altar
    ) {
        return true;
    }
}
