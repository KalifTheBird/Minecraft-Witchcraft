package net.toxicteddie.witchcraft.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.custom.MortarAndPestleBlock;
import net.toxicteddie.witchcraft.block.entity.MortarAndPestleBlockEntity;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.client.model.PestleModel;

public class MortarAndPestleRenderer
        implements BlockEntityRenderer<MortarAndPestleBlockEntity> {

    private static final ResourceLocation PESTLE_TEXTURE =
            new ResourceLocation(
                    Witchcraft.MOD_ID,
                    "textures/block/pestle.png"
            );

    private static final double REST_X = -0.46D;
    private static final double REST_Y = 0.0D;
    private static final double REST_Z = 0.0D;

    private static final float REST_TILT_Z =
            -22.5F;

    private static final float UPRIGHT_CORRECTION =
            20.0F;

    private static final float FULL_LIFT_HEIGHT =
            0.135F;

    private static final float RELEASE_LIFT_HEIGHT =
            0.050F;


    private static final float CENTER_SHIFT_X =
            0.095F;

    private static final float GRIND_ROCK_X =
            1.55F;

    private static final float GRIND_ROCK_Z =
            1.75F;

    private static final float GRIND_TWIST_Y =
            4.5F;

    private static final float GRIND_PRESS_DEPTH =
            0.004F;


    private static final float TICKS_PER_GRIND_CIRCLE =
            10.0F;


    private static final float INGREDIENT_REST_SCALE = 0.255F;
    private static final float INGREDIENT_FINAL_SCALE = 0.070F;
    private static final float INGREDIENT_REST_RADIUS = 0.115F;
    private static final float INGREDIENT_GRIND_RADIUS = 0.095F;
    private static final float INGREDIENT_FINAL_RADIUS = 0.025F;
    private static final float INGREDIENT_REST_Y = 0.405F;
    private static final float INGREDIENT_GRIND_Y = 0.398F;
    private static final float INGREDIENT_ORBIT_TICKS = 38.0F;


    private static final float INGREDIENT_BASE_ROTATION_X = 270.0F;

    private static final float[] INGREDIENT_REST_TILT_Z = {
            -61.0F,
            61.0F,
            -58.0F
    };

    private static final float[] INGREDIENT_REST_TILT_X = {
            -74.0F,
            74.0F,
            76.0F
    };

    private static final float[] INGREDIENT_REST_TILT_Y = {
            14.0F,
            -14.0F,
            2.0F
    };


    private static final float[] INGREDIENT_GRIND_TILT_Z = {
            -45.0F,
            45.0F,
            -45.0F
    };

    private static final float[] INGREDIENT_GRIND_TILT_X = {
            -54.0F,
            54.0F,
            58.0F
    };

    private static final float[] INGREDIENT_GRIND_TILT_Y = {
            10.0F,
            -10.0F,
            0.0F
    };

    private static final float[] INGREDIENT_REST_X = {
            0.108F,
            -0.108F,
            0.004F
    };

    private static final float[] INGREDIENT_REST_Z = {
            0.063F,
            0.063F,
            -0.116F
    };

    private static final float[] INGREDIENT_REST_Y_OFFSET = {
            -0.006F,
            -0.006F,
            -0.008F
    };

    private static final float[] INGREDIENT_BASE_ANGLES = {
            210.0F,
            330.0F,
            90.0F
    };

    private final PestleModel model;

    public MortarAndPestleRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        this.model =
                new PestleModel(
                        context.bakeLayer(
                                ModModelLayers.PESTLE_LAYER
                        )
                );
    }

    @Override
    public void render(
            MortarAndPestleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        renderIngredients(
                blockEntity,
                partialTick,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.pushPose();

        poseStack.translate(
                0.5D,
                1.5D,
                0.5D
        );

        poseStack.scale(
                1.0F,
                -1.0F,
                -1.0F
        );

        rotateToFacing(
                poseStack,
                blockEntity.getBlockState()
        );

        poseStack.translate(
                REST_X,
                REST_Y,
                REST_Z
        );

        AnimationPose animationPose =
                blockEntity.isGrinding()
                        ? calculateAnimationPose(
                        blockEntity,
                        partialTick
                )
                        : AnimationPose.REST;

        poseStack.translate(
                animationPose.offsetX(),
                animationPose.offsetY(),
                0.0D
        );


        poseStack.mulPose(
                Axis.ZP.rotationDegrees(
                        REST_TILT_Z
                )
        );


        this.model.setRotationDegrees(
                animationPose.rotationX(),
                animationPose.rotationY(),
                animationPose.rotationZ()
        );

        this.model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(
                        RenderType.entityCutoutNoCull(
                                PESTLE_TEXTURE
                        )
                ),
                packedLight,
                packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        this.model.resetRotation();

        poseStack.popPose();
    }

    private static void renderIngredients(
            MortarAndPestleBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing =
                blockEntity
                        .getBlockState()
                        .getValue(
                                MortarAndPestleBlock.FACING
                        );

        float setupProgress =
                blockEntity.isGrinding()
                        ? smootherStep(
                        blockEntity.getSetupProgress(partialTick)
                )
                        : 0.0F;

        float activeProgress =
                blockEntity.isGrinding()
                        ? blockEntity.getActiveGrindingProgress(
                        partialTick
                )
                        : 0.0F;

        float finishProgress =
                blockEntity.isGrinding()
                        ? smootherStep(
                        blockEntity.getFinishProgress(partialTick)
                )
                        : 0.0F;

        float activeElapsed =
                Math.max(
                        0.0F,
                        blockEntity.getGrindingElapsedTicks(partialTick)
                                - MortarAndPestleBlockEntity
                                .LIFT_AND_SETUP_TICKS
                );


        float orbitDegrees =
                activeElapsed
                        * 360.0F
                        / INGREDIENT_ORBIT_TICKS;

        float grindRadius =
                Mth.lerp(
                        activeProgress,
                        INGREDIENT_GRIND_RADIUS,
                        INGREDIENT_FINAL_RADIUS
                );

        float radius =
                Mth.lerp(
                        setupProgress,
                        INGREDIENT_REST_RADIUS,
                        grindRadius
                );

        radius =
                Mth.lerp(
                        finishProgress,
                        radius,
                        0.0F
                );

        float grindingScale =
                Mth.lerp(
                        activeProgress,
                        INGREDIENT_REST_SCALE,
                        INGREDIENT_FINAL_SCALE
                );

        float itemScale =
                Mth.lerp(
                        setupProgress,
                        INGREDIENT_REST_SCALE,
                        grindingScale
                );

        itemScale =
                Mth.lerp(
                        finishProgress,
                        itemScale,
                        0.0F
                );

        float itemY =
                Mth.lerp(
                        setupProgress,
                        INGREDIENT_REST_Y,
                        INGREDIENT_GRIND_Y
                );

        itemY -= finishProgress * 0.018F;

        poseStack.pushPose();

        poseStack.translate(
                0.5D,
                0.0D,
                0.5D
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        getFacingDegrees(facing)
                )
        );

        for (int slot = 0;
             slot < MortarAndPestleBlockEntity.SLOT_COUNT;
             slot++) {

            ItemStack stack =
                    blockEntity.getDisplayedItem(slot);

            if (stack.isEmpty() || itemScale <= 0.004F) {
                continue;
            }

            float restAngle =
                    INGREDIENT_BASE_ANGLES[slot];

            float positionAngle =
                    restAngle
                            + orbitDegrees
                            * setupProgress
                            * (1.0F - finishProgress);

            float radians =
                    positionAngle * (Mth.PI / 180.0F);


            float orbitX =
                    -Mth.cos(radians) * radius;

            float orbitZ =
                    -Mth.sin(radians) * radius;

            float x =
                    Mth.lerp(
                            setupProgress,
                            INGREDIENT_REST_X[slot],
                            orbitX
                    );

            float z =
                    Mth.lerp(
                            setupProgress,
                            INGREDIENT_REST_Z[slot],
                            orbitZ
                    );

            float adjustedY =
                    itemY
                            + INGREDIENT_REST_Y_OFFSET[slot]
                            * (1.0F - setupProgress);

            poseStack.pushPose();

            poseStack.translate(
                    x,
                    adjustedY,
                    z
            );


            poseStack.mulPose(
                    Axis.XP.rotationDegrees(
                            INGREDIENT_BASE_ROTATION_X
                    )
            );

            float tiltY =
                    Mth.lerp(
                            setupProgress,
                            INGREDIENT_REST_TILT_Y[slot],
                            INGREDIENT_GRIND_TILT_Y[slot]
                    );

            float tiltZ =
                    Mth.lerp(
                            setupProgress,
                            INGREDIENT_REST_TILT_Z[slot],
                            INGREDIENT_GRIND_TILT_Z[slot]
                    );

            float tiltX =
                    Mth.lerp(
                            setupProgress,
                            INGREDIENT_REST_TILT_X[slot],
                            INGREDIENT_GRIND_TILT_X[slot]
                    );

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(tiltY)
            );

            poseStack.mulPose(
                    Axis.ZP.rotationDegrees(tiltZ)
            );

            poseStack.mulPose(
                    Axis.XP.rotationDegrees(tiltX)
            );

            poseStack.scale(
                    itemScale,
                    itemScale,
                    itemScale
            );

            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(
                            stack,
                            ItemDisplayContext.FIXED,
                            packedLight,
                            packedOverlay,
                            poseStack,
                            bufferSource,
                            blockEntity.getLevel(),
                            (int) (
                                    blockEntity
                                            .getBlockPos()
                                            .asLong()
                                            + slot
                            )
                    );

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static AnimationPose calculateAnimationPose(
            MortarAndPestleBlockEntity blockEntity,
            float partialTick
    ) {
        float elapsed =
                blockEntity.getGrindingElapsedTicks(
                        partialTick
                );

        if (elapsed
                < MortarAndPestleBlockEntity
                .LIFT_AND_SETUP_TICKS) {

            float setupProgress =
                    elapsed
                            / MortarAndPestleBlockEntity
                            .LIFT_AND_SETUP_TICKS;

            return calculateSetupPose(
                    setupProgress
            );
        }

        float activeGrindingEnd =
                MortarAndPestleBlockEntity
                        .LIFT_AND_SETUP_TICKS
                        + MortarAndPestleBlockEntity
                        .ACTIVE_GRINDING_TICKS;

        if (elapsed < activeGrindingEnd) {
            float activeElapsed =
                    elapsed
                            - MortarAndPestleBlockEntity
                            .LIFT_AND_SETUP_TICKS;

            return calculateActiveGrindingPose(
                    activeElapsed
            );
        }

        float finishElapsed =
                elapsed
                        - activeGrindingEnd;

        float finishProgress =
                finishElapsed
                        / MortarAndPestleBlockEntity
                        .FINISH_AND_RETURN_TICKS;

        return calculateFinishPose(
                Mth.clamp(
                        finishProgress,
                        0.0F,
                        1.0F
                )
        );
    }


    private static AnimationPose calculateSetupPose(
            float progress
    ) {
        progress =
                Mth.clamp(
                        progress,
                        0.0F,
                        1.0F
                );


        float liftEnd =
                0.16F / 0.42F;

        float straightenEnd =
                0.28F / 0.42F;

        float offsetX = 0.0F;
        float offsetY = 0.0F;

        float rotationX = 0.0F;
        float rotationY = 0.0F;
        float rotationZ = 0.0F;


        if (progress < liftEnd) {
            float phase =
                    progress / liftEnd;

            float eased =
                    easeInOutSine(phase);

            offsetY =
                    -FULL_LIFT_HEIGHT
                            * eased;

            float centerPhase =
                    Mth.clamp(
                            (phase - 0.45F) / 0.55F,
                            0.0F,
                            1.0F
                    );

            offsetX =
                    CENTER_SHIFT_X
                            * smootherStep(centerPhase);
        }


        else if (progress < straightenEnd) {
            float phase =
                    (progress - liftEnd)
                            / (straightenEnd - liftEnd);

            float eased =
                    easeInOutSine(phase);

            float hoverArc =
                    Mth.sin(
                            phase * Mth.PI
                    );

            offsetX =
                    CENTER_SHIFT_X;

            offsetY =
                    -FULL_LIFT_HEIGHT
                            - hoverArc * 0.005F;

            rotationX =
                    hoverArc * 0.20F;

            rotationY =
                    hoverArc * 0.75F;

            rotationZ =
                    UPRIGHT_CORRECTION
                            * eased;
        }


        else {
            float phase =
                    (progress - straightenEnd)
                            / (1.0F - straightenEnd);

            float eased =
                    easeInOutSine(phase);

            float contactArc =
                    Mth.sin(
                            phase * Mth.PI
                    );

            offsetX =
                    CENTER_SHIFT_X;

            offsetY =
                    Mth.lerp(
                            eased,
                            -FULL_LIFT_HEIGHT,
                            0.002F
                    );

            rotationX =
                    contactArc * 0.20F;

            rotationY =
                    contactArc * 0.35F;

            rotationZ =
                    UPRIGHT_CORRECTION;
        }

        return new AnimationPose(
                offsetX,
                offsetY,
                rotationX,
                rotationY,
                rotationZ
        );
    }


    private static AnimationPose calculateActiveGrindingPose(
            float activeElapsedTicks
    ) {
        float phase =
                Mth.clamp(
                        activeElapsedTicks
                                / MortarAndPestleBlockEntity
                                .ACTIVE_GRINDING_TICKS,
                        0.0F,
                        1.0F
                );


        float envelope =
                smoothGrindingEnvelope(phase);


        float angle =
                activeElapsedTicks
                        * Mth.TWO_PI
                        / TICKS_PER_GRIND_CIRCLE;

        float circularX =
                Mth.cos(angle);

        float circularZ =
                Mth.sin(angle);

        float pressure =
                0.5F
                        - 0.5F
                        * Mth.cos(angle);

        float offsetX =
                CENTER_SHIFT_X;

        float offsetY =
                0.002F
                        + pressure
                        * GRIND_PRESS_DEPTH
                        * envelope;

        float rotationX =
                circularX
                        * GRIND_ROCK_X
                        * envelope;

        float rotationY =
                Mth.sin(
                        angle * 1.25F
                )
                        * GRIND_TWIST_Y
                        * envelope;

        float rotationZ =
                UPRIGHT_CORRECTION
                        + circularZ
                        * GRIND_ROCK_Z
                        * envelope;

        return new AnimationPose(
                offsetX,
                offsetY,
                rotationX,
                rotationY,
                rotationZ
        );
    }


    private static AnimationPose calculateFinishPose(
            float progress
    ) {


        if (progress < 0.50F) {
            float phase =
                    progress / 0.50F;

            float eased =
                    easeInOutSine(phase);

            float remaining =
                    1.0F - eased;

            float angle =
                    phase * Mth.TWO_PI;

            float offsetX =
                    CENTER_SHIFT_X;

            float offsetY =
                    Mth.lerp(
                            eased,
                            0.002F,
                            -RELEASE_LIFT_HEIGHT
                    );

            float rotationX =
                    Mth.cos(angle)
                            * 0.75F
                            * remaining;

            float rotationY =
                    Mth.sin(angle)
                            * 1.8F
                            * remaining;

            float rotationZ =
                    UPRIGHT_CORRECTION
                            + Mth.sin(angle)
                            * 0.85F
                            * remaining;

            return new AnimationPose(
                    offsetX,
                    offsetY,
                    rotationX,
                    rotationY,
                    rotationZ
            );
        }


        float secondHalf =
                (progress - 0.50F) / 0.50F;

        if (secondHalf < 0.55F) {
            float phase =
                    secondHalf / 0.55F;

            float eased =
                    easeInOutSine(phase);

            float hoverArc =
                    Mth.sin(
                            phase * Mth.PI
                    );

            float offsetX =
                    Mth.lerp(
                            eased,
                            CENTER_SHIFT_X,
                            0.0F
                    );

            float offsetY =
                    -RELEASE_LIFT_HEIGHT
                            - hoverArc * 0.002F;

            float rotationX =
                    hoverArc * 0.10F;

            float rotationY =
                    hoverArc * 0.25F;

            float rotationZ =
                    Mth.lerp(
                            eased,
                            UPRIGHT_CORRECTION,
                            0.0F
                    );

            return new AnimationPose(
                    offsetX,
                    offsetY,
                    rotationX,
                    rotationY,
                    rotationZ
            );
        }

        float phase =
                (secondHalf - 0.55F)
                        / 0.45F;

        float eased =
                easeInOutSine(phase);

        float settle =
                Mth.sin(
                        phase * Mth.PI
                )
                        * (1.0F - phase);

        return new AnimationPose(
                0.0F,
                Mth.lerp(
                        eased,
                        -RELEASE_LIFT_HEIGHT,
                        0.0F
                ) - settle * 0.001F,
                settle * 0.08F,
                0.0F,
                -settle * 0.12F
        );
    }

    private static float smoothGrindingEnvelope(
            float phase
    ) {
        phase =
                Mth.clamp(
                        phase,
                        0.0F,
                        1.0F
                );


        float rampIn =
                smootherStep(
                        Mth.clamp(
                                phase / 0.10F,
                                0.0F,
                                1.0F
                        )
                );

        float rampOut =
                smootherStep(
                        Mth.clamp(
                                (1.0F - phase) / 0.10F,
                                0.0F,
                                1.0F
                        )
                );

        return Math.min(
                rampIn,
                rampOut
        );
    }

    private static float smootherStep(
            float value
    ) {
        value =
                Mth.clamp(
                        value,
                        0.0F,
                        1.0F
                );

        return value
                * value
                * value
                * (
                value
                        * (
                        value * 6.0F
                                - 15.0F
                )
                        + 10.0F
        );
    }

    private static float easeInOutSine(
            float value
    ) {
        value =
                Mth.clamp(
                        value,
                        0.0F,
                        1.0F
                );

        return -(
                Mth.cos(
                        Mth.PI * value
                ) - 1.0F
        ) / 2.0F;
    }

    private static void rotateToFacing(
            PoseStack poseStack,
            BlockState state
    ) {
        if (!state.hasProperty(
                MortarAndPestleBlock.FACING
        )) {
            return;
        }

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        getFacingDegrees(
                                state.getValue(
                                        MortarAndPestleBlock.FACING
                                )
                        )
                )
        );
    }

    private static float getFacingDegrees(
            Direction facing
    ) {
        return switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    private record AnimationPose(
            float offsetX,
            float offsetY,
            float rotationX,
            float rotationY,
            float rotationZ
    ) {
        private static final AnimationPose REST =
                new AnimationPose(
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F,
                        0.0F
                );
    }
}
