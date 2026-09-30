package net.toxicteddie.witchcraft.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.block.entity.ChalkGlyphBlockEntity;
import net.toxicteddie.witchcraft.particle.ModParticles;
import net.toxicteddie.witchcraft.ritual.RitualPatterns;
import org.joml.Matrix4f;


public class ChalkGlyphRenderer implements BlockEntityRenderer<ChalkGlyphBlockEntity> {
    private static final float OFFERING_SCALE = 0.53F;
    private static final double INITIAL_LIFT_OFFSET = 0.25D;


    private static final int BEAM_SEGMENTS = 30;
    private static final float OUTER_BEAM_WIDTH = 0.060F;
    private static final float INNER_BEAM_WIDTH = 0.024F;
    private static final double SIDE_WAVE = 0.17D;
    private static final double VERTICAL_WAVE = 0.095D;
    private static final double ARC_HEIGHT = 0.62D;
    private static final double BEAM_SPEED = 0.095D;


    private static final double WITCH_BEAM_END_X = 0.0D;
    private static final double WITCH_BEAM_END_Y = 1.30D;
    private static final double WITCH_BEAM_END_Z = 0.0D;

    private static final double GLYPH_BEAM_END_X = 0.50D;
    private static final double GLYPH_BEAM_END_Y = 0.04D;
    private static final double GLYPH_BEAM_END_Z = 0.50D;

    public ChalkGlyphRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ChalkGlyphBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!be.isRitualActive()) return;

        double ritualTime = be.getRitualTicks() + partialTick;
        long gameTime = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
        double t = gameTime + partialTick;

        if (be.isTemperanceRite()) {
            spawnRitualFlameParticles(be, partialTick);
            if (be.isTemperanceExtractionActive()) {
                renderTemperanceBeam(be, partialTick, pose, buffers);
            }
        }

        if (be.isHollowingRite()) {
            spawnHollowingCandleFlames(be, partialTick);
            renderHollowingThreads(be, partialTick, pose, buffers);
            spawnHollowingStrainParticles(be, partialTick);
        }
        if (be.isCornerRite()) {
            spawnCornerRitualFlames(be, partialTick);
        }
        if (be.isTransmutationRitual()) {
            spawnTransmutationRitualFlames(be, partialTick);
        }
        float earthPickaxeManifest = be.isEarthVeinsRitual()
                ? be.getEarthVeinsPickaxeArrivalProgress(partialTick) : 0.0F;
        if (be.isEarthVeinsRitual() && earthPickaxeManifest > 0.0F && be.isEarthVeinsPickaxeVisible()) {
            renderEarthVeinsPickaxe(be, partialTick, pose, buffers, light, overlay);
        }

        for (int i = 0; i < be.getOfferings().size(); i++) {


            if (be.isEarthVeinsRitual() && i == 4 && earthPickaxeManifest > 0.0F) continue;
            ItemStack stack = be.getOfferings().get(i);
            if (stack.isEmpty()) continue;

            int poofTick = be.getOfferingPoofTick(i);

            double totalProgress = clamp(ritualTime / Math.max(1.0D, poofTick - 2.0D));
            double seed = i * 2.413D + (be.getBlockPos().asLong() & 255L) * 0.017D;


            double startX = be.getOfferingStartX(i);
            double startY = be.getOfferingStartY(i);
            double startZ = be.getOfferingStartZ(i);


            double liftDuration = 34.0D;
            double liftProgress = smoothStep(clamp(ritualTime / liftDuration));
            double travelProgress = smoothStep(clamp(
                    (ritualTime - liftDuration) / Math.max(1.0D, poofTick - liftDuration - 2.0D)));

            double x = lerp(startX, 0.0D, travelProgress);
            double z = lerp(startZ, 0.0D, travelProgress);

            double hoverHeight = (be.isTemperanceRite() ? 1.05D : 1.20D) + i * 0.08D;
            double y = lerp(startY + INITIAL_LIFT_OFFSET, hoverHeight, liftProgress);


            double bobEnvelope = liftProgress * (1.0D - smoothStep(clamp((totalProgress - 0.88D) / 0.12D)));
            double bob = Math.sin(t * 0.075D + seed) * 0.055D
                    + Math.sin(t * 0.027D + seed * 1.8D) * 0.018D;
            y += bob * bobEnvelope;

            pose.pushPose();
            pose.translate(0.5D + x, y, 0.5D + z);
            pose.mulPose(Axis.YP.rotationDegrees((float) t * (0.65F + i * 0.12F)));
            pose.mulPose(Axis.XP.rotationDegrees(
                    (float) Math.sin(t * 0.030D + seed) * 5.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(
                    (float) Math.cos(t * 0.026D + seed) * 4.0F));

            float pulse = 1.0F + (float) Math.sin(Math.PI * totalProgress) * 0.10F;
            pose.scale(OFFERING_SCALE * pulse, OFFERING_SCALE * pulse, OFFERING_SCALE * pulse);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                    be.getLevel(), (int) (be.getBlockPos().asLong() + i));
            pose.popPose();
        }
    }


    private static void renderEarthVeinsPickaxe(
            ChalkGlyphBlockEntity be, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack pickaxe = be.getEarthVeinsPickaxe();
        if (pickaxe.isEmpty()) return;

        double t = (be.getLevel() == null ? 0L : be.getLevel().getGameTime()) + partialTick;
        float manifest = be.getEarthVeinsPickaxeArrivalProgress(partialTick);
        float orbitGrowth = (float) smoothStep(be.getEarthVeinsWorkProgress(partialTick));
        float finish = (float) smoothStep(be.getEarthVeinsFinishProgress(partialTick));
        float finishElapsed = be.getEarthVeinsFinishElapsed(partialTick);
        if (manifest <= 0.0F) return;


        double orbitRadius = 2.85D * orbitGrowth * (1.0D - finish);
        double orbitSpeed = 0.018D;
        double orbitAngle;
        if (finishElapsed > 0.0F) {
            double finishStartTime = t - finishElapsed;
            double closingTravel = finishElapsed * (1.0D - 0.5D * finish);
            orbitAngle = finishStartTime * orbitSpeed + closingTravel * orbitSpeed;
        } else {
            orbitAngle = t * orbitSpeed;
        }
        double x = Math.cos(orbitAngle) * orbitRadius;
        double z = Math.sin(orbitAngle) * orbitRadius;

        double baseHeight = 1.95D;
        double primaryBob = Math.sin(t * 0.115D) * 0.21D;
        double secondaryBob = Math.sin(t * 0.047D + 1.35D) * 0.065D;


        double y = baseHeight + (primaryBob + secondaryBob) * (0.82D + 0.18D * orbitGrowth);

        float materializeScale = (float) smoothStep(manifest);
        float pulse = 1.0F + (float) Math.sin(Math.PI * manifest) * 0.12F;


        float scale = 0.86F * materializeScale * pulse;

        pose.pushPose();
        pose.translate(0.5D + x, y, 0.5D + z);
        pose.mulPose(Axis.YP.rotationDegrees((float) t * 0.62F));
        pose.mulPose(Axis.ZP.rotationDegrees(-8.0F
                + (float) Math.sin(t * 0.055D) * 4.5F));
        pose.mulPose(Axis.XP.rotationDegrees(4.0F
                + (float) Math.cos(t * 0.043D) * 3.0F));
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                pickaxe, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                be.getLevel(), (int) be.getBlockPos().asLong());
        pose.popPose();
    }

    private static void spawnRitualFlameParticles(
            ChalkGlyphBlockEntity be,
            float partialTick
    ) {
        if (!(be.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel level)) return;

        float rise = be.getTemperanceCandleRiseProgress(partialTick);
        if (rise <= 0.001F) return;

        Vec3 glyphCenter = Vec3.atCenterOf(be.getBlockPos());
        BlockPos[] candles = be.getGrayCandlePositions();

        for (int index = 0; index < candles.length; index++) {
            BlockPos candle = candles[index];
            if (candle == null) continue;

            Vec3 wick = Vec3.atLowerCornerOf(candle).add(0.5D, 0.40D, 0.5D);
            Vec3 inward = new Vec3(
                    glyphCenter.x - wick.x,
                    0.0D,
                    glyphCenter.z - wick.z
            );
            if (inward.lengthSqr() > 1.0E-6D) inward = inward.normalize();

            level.addParticle(
                    ModParticles.RITUAL_FLAME.get(),
                    wick.x + inward.x * 0.035D * rise,
                    wick.y,
                    wick.z + inward.z * 0.035D * rise,
                    rise,
                    index * 3.0D,
                    0.0D
            );
        }
    }

    private static void spawnTransmutationRitualFlames(
            ChalkGlyphBlockEntity be,
            float partialTick
    ) {
        if (!(be.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel level)) return;

        float progress = be.getTransmutationLargeFlameProgress(partialTick);
        if (progress <= 0.001F) return;

        BlockPos[] candles = be.getTransmutationCandlePositions();
        for (int index = 0; index < candles.length; index++) {
            BlockPos candle = candles[index];
            if (candle == null) continue;


            float scale = progress * 1.10F;
            level.addParticle(
                    ModParticles.RITUAL_FLAME.get(),
                    candle.getX() + 0.5D,
                    candle.getY() + 0.40D,
                    candle.getZ() + 0.5D,
                    scale,
                    index * 1.35D,
                    0.0D
            );
        }
    }

    private static void spawnCornerRitualFlames(ChalkGlyphBlockEntity be, float partialTick) {
        if (!(be.getLevel() instanceof net.minecraft.client.multiplayer.ClientLevel level)) return;
        BlockPos[] candles = be.getCornerCandlePositions();
        for (int index = 0; index < candles.length; index++) {
            BlockPos candle = candles[index];
            if (candle == null) continue;
            float progress = be.getCornerFlameProgress(index, partialTick);
            if (progress <= 0.001F) continue;
            level.addParticle(
                    ModParticles.RITUAL_FLAME.get(),
                    candle.getX() + 0.5D,
                    candle.getY() + 0.40D,
                    candle.getZ() + 0.5D,
                    progress,
                    index * 2.0D,
                    0.0D
            );
        }
    }


    private static void spawnHollowingCandleFlames(
            ChalkGlyphBlockEntity be,
            float partialTick
    ) {
        if (!(be.getLevel()
                instanceof net.minecraft.client.multiplayer.ClientLevel level)) {
            return;
        }

        float flame =
                be.getHollowingCandleFlareProgress(
                        partialTick
                );

        if (flame <= 0.001F) {
            return;
        }

        float dying =
                be.getHollowingCandleDyingProgress(
                        partialTick
                );

        long gameTime =
                level.getGameTime();

        for (int index = 0;
             index < RitualPatterns.HOLLOWING_CANDLE_RING.length;
             index++) {

            int[] offset =
                    RitualPatterns.HOLLOWING_CANDLE_RING[index];

            BlockPos candle =
                    be.getBlockPos().offset(
                            offset[0],
                            0,
                            offset[1]
                    );

            BlockState state =
                    level.getBlockState(candle);

            if (!state.is(Blocks.BLACK_CANDLE)) {
                continue;
            }


            if (state.hasProperty(CandleBlock.LIT)
                    && !state.getValue(CandleBlock.LIT)
                    && dying > 0.0F) {
                continue;
            }


            float stagger =
                    0.94F
                            + 0.06F
                            * (float)Math.sin(
                                    gameTime * 0.075D
                                            + index * 0.73D
                            );

            float scale =
                    Mth.clamp(
                            flame * stagger,
                            0.0F,
                            1.0F
                    );

            level.addParticle(
                    ModParticles.RITUAL_FLAME.get(),
                    candle.getX() + 0.5D,
                    candle.getY() + 0.40D,
                    candle.getZ() + 0.5D,
                    scale,
                    index * 0.81D,
                    0.0D
            );
        }
    }


    private static void renderHollowingThreads(
            ChalkGlyphBlockEntity be,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers
    ) {
        if (be.getLevel() == null
                || be.getHollowingTargetUuid() == null) {
            return;
        }

        Player target =
                be.getLevel()
                        .getPlayerByUUID(
                                be.getHollowingTargetUuid()
                        );

        if (target == null) {
            return;
        }

        int activeLines =
                be.getHollowingActiveLineCount(
                        partialTick
                );

        if (activeLines <= 0) {
            return;
        }

        BlockPos[] stones =
                be.getHollowingGlimmerstonePositions();

        Vec3 blockOrigin =
                Vec3.atLowerCornerOf(
                        be.getBlockPos()
                );


        Vec3 witchWorld =
                target.getPosition(partialTick)
                        .add(0.0D, 1.12D, 0.0D);

        Vec3 start =
                witchWorld.subtract(
                        blockOrigin
                );

        double time =
                (be.getLevel().getGameTime()
                        + partialTick)
                        * 0.080D;

        float corruption =
                be.getHollowingCorruptionProgress(
                        partialTick
                );

        Matrix4f matrix =
                pose.last().pose();

        VertexConsumer consumer =
                buffers.getBuffer(
                        RenderType.lightning()
                );

        for (int index = 0;
             index < activeLines
                     && index < stones.length;
             index++) {

            BlockPos stonePos =
                    stones[index];

            Vec3 endWorld =
                    Vec3.atLowerCornerOf(
                            stonePos
                    ).add(
                            0.5D,
                            0.58D,
                            0.5D
                    );

            Vec3 end =
                    endWorld.subtract(
                            blockOrigin
                    );

            float reveal =
                    be.getHollowingLineRevealProgress(
                            index,
                            partialTick
                    );

            if (reveal <= 0.001F) {
                continue;
            }

            Vec3 delta =
                    end.subtract(start);

            if (delta.lengthSqr()
                    < 1.0E-5D) {
                continue;
            }


            int segments =
                    Math.max(
                            3,
                            (int)Math.ceil(
                                    reveal * 28.0D
                            )
                    );

            Vec3[] points =
                    new Vec3[
                            segments + 1
                    ];

            Vec3 forward =
                    delta.normalize();

            Vec3 reference =
                    Math.abs(forward.y) > 0.92D
                            ? new Vec3(
                                    1.0D,
                                    0.0D,
                                    0.0D
                            )
                            : new Vec3(
                                    0.0D,
                                    1.0D,
                                    0.0D
                            );

            Vec3 side =
                    forward.cross(reference);

            if (side.lengthSqr()
                    < 1.0E-6D) {
                side =
                        new Vec3(
                                1.0D,
                                0.0D,
                                0.0D
                        );
            } else {
                side =
                        side.normalize();
            }

            Vec3 up =
                    side.cross(forward)
                            .normalize();

            double linePhase =
                    index * 1.73D;

            for (int i = 0;
                 i <= segments;
                 i++) {

                double local =
                        i / (double)segments;

                double progress =
                        local * reveal;

                Vec3 base =
                        start.add(
                                delta.scale(
                                        progress
                                )
                        );

                double envelope =
                        Math.sin(
                                Math.PI
                                        * progress
                        );


                double calmWave =
                        Math.sin(
                                progress
                                        * Math.PI
                                        * 4.0D
                                        - time
                                        + linePhase
                        )
                                * 0.045D
                                * envelope;

                double verticalWave =
                        Math.cos(
                                progress
                                        * Math.PI
                                        * 3.0D
                                        - time
                                        * 0.74D
                                        + linePhase
                        )
                                * 0.025D
                                * envelope;

                double corruptJitter =
                        Math.sin(
                                progress
                                        * Math.PI
                                        * 13.0D
                                        + time
                                        * 2.35D
                                        + linePhase
                        )
                                * 0.070D
                                * corruption
                                * envelope;

                double corruptJitterY =
                        Math.cos(
                                progress
                                        * Math.PI
                                        * 9.0D
                                        - time
                                        * 1.95D
                                        + linePhase
                        )
                                * 0.050D
                                * corruption
                                * envelope;


                double sag =
                        -0.10D
                                * 4.0D
                                * progress
                                * (1.0D - progress);

                points[i] =
                        base
                                .add(
                                        side.scale(
                                                calmWave
                                                        + corruptJitter
                                        )
                                )
                                .add(
                                        up.scale(
                                                verticalWave
                                                        + corruptJitterY
                                        )
                                )
                                .add(
                                        0.0D,
                                        sag,
                                        0.0D
                                );
            }


            int outerR =
                    (int)Mth.lerp(
                            corruption,
                            10.0F,
                            86.0F
                    );
            int outerG =
                    (int)Mth.lerp(
                            corruption,
                            10.0F,
                            3.0F
                    );
            int outerB =
                    (int)Mth.lerp(
                            corruption,
                            13.0F,
                            6.0F
                    );

            int innerR =
                    (int)Mth.lerp(
                            corruption,
                            42.0F,
                            150.0F
                    );
            int innerG =
                    (int)Mth.lerp(
                            corruption,
                            42.0F,
                            8.0F
                    );
            int innerB =
                    (int)Mth.lerp(
                            corruption,
                            48.0F,
                            12.0F
                    );

            float strain =
                    index / 3.0F;

            float outerWidth =
                    0.052F
                            + strain * 0.006F
                            + corruption * 0.010F;

            float innerWidth =
                    0.020F
                            + strain * 0.003F
                            - corruption * 0.004F;

            renderTube(
                    consumer,
                    matrix,
                    points,
                    outerWidth,
                    8,
                    outerR,
                    outerG,
                    outerB,
                    (int)Mth.lerp(
                            corruption,
                            178.0F,
                            205.0F
                    )
            );

            renderTube(
                    consumer,
                    matrix,
                    points,
                    Math.max(
                            0.010F,
                            innerWidth
                    ),
                    8,
                    innerR,
                    innerG,
                    innerB,
                    (int)Mth.lerp(
                            corruption,
                            220.0F,
                            235.0F
                    )
            );
        }
    }


    private static void spawnHollowingStrainParticles(
            ChalkGlyphBlockEntity be,
            float partialTick
    ) {
        if (!(be.getLevel()
                instanceof net.minecraft.client.multiplayer.ClientLevel level)) {
            return;
        }

        int activeLines =
                be.getHollowingActiveLineCount(
                        partialTick
                );

        if (activeLines <= 0) {
            return;
        }


        long gameTime =
                level.getGameTime();

        float corruption =
                be.getHollowingCorruptionProgress(
                        partialTick
                );

        BlockPos[] stones =
                be.getHollowingGlimmerstonePositions();

        for (int index = 0;
             index < activeLines
                     && index < stones.length;
             index++) {

            BlockPos stone =
                    stones[index];

            if ((gameTime + index * 3L) % 5L == 0L) {
                level.addParticle(
                        corruption > 0.45F
                                ? net.minecraft.core.particles.ParticleTypes.ASH
                                : net.minecraft.core.particles.ParticleTypes.END_ROD,
                        stone.getX()
                                + 0.5D
                                + (level.random.nextDouble() - 0.5D)
                                * 0.28D,
                        stone.getY()
                                + 0.48D
                                + level.random.nextDouble()
                                * 0.30D,
                        stone.getZ()
                                + 0.5D
                                + (level.random.nextDouble() - 0.5D)
                                * 0.28D,
                        0.0D,
                        0.008D
                                + corruption * 0.012D,
                        0.0D
                );
            }
        }
    }


    private static void renderTemperanceBeam(
            ChalkGlyphBlockEntity be,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers
    ) {
        if (be.getLevel() == null || be.getTemperanceTargetUuid() == null) return;

        Player target = be.getLevel().getPlayerByUUID(be.getTemperanceTargetUuid());
        if (target == null) return;

        Vec3 blockOrigin = Vec3.atLowerCornerOf(be.getBlockPos());
        Vec3 startWorld = target.getPosition(partialTick).add(
                WITCH_BEAM_END_X, WITCH_BEAM_END_Y, WITCH_BEAM_END_Z);
        Vec3 endWorld = blockOrigin.add(
                GLYPH_BEAM_END_X, GLYPH_BEAM_END_Y, GLYPH_BEAM_END_Z);

        Vec3 start = startWorld.subtract(blockOrigin);
        Vec3 end = endWorld.subtract(blockOrigin);
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() < 1.0E-4D) return;

        Vec3 forward = delta.normalize();
        Vec3 reference = Math.abs(forward.y) > 0.93D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 side = forward.cross(reference).normalize();
        Vec3 up = side.cross(forward).normalize();
        double time = (be.getLevel().getGameTime() + partialTick) * BEAM_SPEED;


        float connection = be.getTemperanceExtractionProgress(partialTick);
        double revealEnd = connection * connection * (3.0D - 2.0D * connection);
        float detachRaw = be.getTemperanceBeamDetachProgress(partialTick);
        double detach = detachRaw * detachRaw * (3.0D - 2.0D * detachRaw);
        double visibleStart = detach;
        double visibleEnd = revealEnd;
        if (visibleEnd - visibleStart <= 0.002D) return;

        int visibleSegments = Math.max(2, (int) Math.ceil((visibleEnd - visibleStart) * BEAM_SEGMENTS));
        Vec3[] points = new Vec3[visibleSegments + 1];

        for (int i = 0; i <= visibleSegments; i++) {
            double local = i / (double) visibleSegments;
            double progress = Mth.lerp(local, visibleStart, visibleEnd);
            double envelope = Math.sin(Math.PI * progress);
            Vec3 base = start.add(delta.scale(progress));

            double sideWave = Math.sin(progress * Math.PI * 5.0D - time)
                    * SIDE_WAVE * envelope;
            double verticalWave = Math.cos(progress * Math.PI * 3.0D - time * 0.82D)
                    * VERTICAL_WAVE * envelope;
            double fineWave = Math.sin(progress * Math.PI * 11.0D + time * 0.47D)
                    * 0.025D * envelope;
            double arc = ARC_HEIGHT * 4.0D * progress * (1.0D - progress);


            double distanceToHeart = 1.0D - progress;
            double spiralRadius = 0.24D * detach * distanceToHeart;
            double spiralAngle = progress * Math.PI * 10.0D - time * 2.15D
                    + detach * Math.PI * 3.0D;

            points[i] = base
                    .add(side.scale(sideWave + fineWave + Math.cos(spiralAngle) * spiralRadius))
                    .add(up.scale(verticalWave + Math.sin(spiralAngle) * spiralRadius))
                    .add(0.0D, arc, 0.0D);
        }

        Matrix4f matrix = pose.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        float collapseScale = (float) (1.0D - detach * 0.48D);
        int outerAlpha = (int) Mth.lerp((float) detach, 105.0F, 62.0F);
        int innerAlpha = (int) Mth.lerp((float) detach, 245.0F, 178.0F);

        renderTube(consumer, matrix, points, OUTER_BEAM_WIDTH * collapseScale, 8,
                112, 137, 160, outerAlpha);
        renderTube(consumer, matrix, points, INNER_BEAM_WIDTH * collapseScale, 8,
                224, 240, 250, innerAlpha);
    }

    private static void renderTube(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3[] points,
            float radius,
            int sides,
            int red, int green, int blue, int alpha
    ) {
        Vec3[][] rings = new Vec3[points.length][sides];

        Vec3 previousNormal = null;

        for (int i = 0; i < points.length; i++) {
            Vec3 tangent;
            if (i == 0) {
                tangent = points[1].subtract(points[0]);
            } else if (i == points.length - 1) {
                tangent = points[i].subtract(points[i - 1]);
            } else {
                tangent = points[i + 1].subtract(points[i - 1]);
            }

            if (tangent.lengthSqr() < 1.0E-6D) {
                tangent = new Vec3(0.0D, 1.0D, 0.0D);
            }
            tangent = tangent.normalize();

            Vec3 normal;
            if (previousNormal == null) {
                Vec3 reference = Math.abs(tangent.y) > 0.92D
                        ? new Vec3(1.0D, 0.0D, 0.0D)
                        : new Vec3(0.0D, 1.0D, 0.0D);
                normal = tangent.cross(reference).normalize();
            } else {


                normal = previousNormal.subtract(
                        tangent.scale(previousNormal.dot(tangent))
                );
                if (normal.lengthSqr() < 1.0E-6D) {
                    normal = tangent.cross(new Vec3(0.0D, 1.0D, 0.0D));
                    if (normal.lengthSqr() < 1.0E-6D) {
                        normal = tangent.cross(new Vec3(1.0D, 0.0D, 0.0D));
                    }
                }
                normal = normal.normalize();
            }

            Vec3 binormal = tangent.cross(normal).normalize();
            previousNormal = normal;

            for (int sideIndex = 0; sideIndex < sides; sideIndex++) {
                double angle = Mth.TWO_PI * sideIndex / sides;
                Vec3 offset = normal.scale(Math.cos(angle) * radius)
                        .add(binormal.scale(Math.sin(angle) * radius));
                rings[i][sideIndex] = points[i].add(offset);
            }
        }

        for (int i = 0; i < points.length - 1; i++) {
            for (int sideIndex = 0; sideIndex < sides; sideIndex++) {
                int nextSide = (sideIndex + 1) % sides;

                vertex(consumer, matrix, rings[i][sideIndex], red, green, blue, alpha);
                vertex(consumer, matrix, rings[i + 1][sideIndex], red, green, blue, alpha);
                vertex(consumer, matrix, rings[i + 1][nextSide], red, green, blue, alpha);
                vertex(consumer, matrix, rings[i][nextSide], red, green, blue, alpha);
            }
        }
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 point,
            int red, int green, int blue, int alpha
    ) {
        consumer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static double smoothStep(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    @Override
    public int getViewDistance() {


        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen(ChalkGlyphBlockEntity be) {
        return true;
    }
}
