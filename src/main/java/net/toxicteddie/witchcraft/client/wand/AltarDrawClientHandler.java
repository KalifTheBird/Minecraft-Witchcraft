package net.toxicteddie.witchcraft.client.wand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.custom.WitchesAltarBlock;
import net.toxicteddie.witchcraft.item.custom.WandItem;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.AltarDrawActionPacket;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = Witchcraft.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class AltarDrawClientHandler {
    private static final int CURVE_SEGMENTS = 24;
    private static final int TUBE_SIDES = 8;

    private AltarDrawClientHandler() {}

    private static boolean stopSent;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (!AltarDrawClientState.active()) {
            stopSent = false;
            WandTipTracker.clear();
            return;
        }

        if (minecraft.player == null) {
            AltarDrawClientState.clear();
            stopSent = false;
            return;
        }

        boolean holdingWand =
                minecraft.player.getMainHandItem().getItem() instanceof WandItem
                || minecraft.player.getOffhandItem().getItem() instanceof WandItem;

        long window = minecraft.getWindow().getWindow();
        boolean rightMouseDown = window != 0L
                && GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;

        if ((!rightMouseDown || !holdingWand) && !stopSent) {
            stopSent = true;
            ModNetworking.CHANNEL.sendToServer(
                    new AltarDrawActionPacket(AltarDrawActionPacket.Action.STOP)
            );
        }
    }


    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isDrawing(minecraft) || minecraft.player == null) return;

        boolean mainWand =
                minecraft.player.getMainHandItem().getItem() instanceof WandItem;
        boolean offWand =
                minecraft.player.getOffhandItem().getItem() instanceof WandItem;

        if ((mainWand && event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND)
                || (!mainWand && offWand
                && event.getHand() != net.minecraft.world.InteractionHand.OFF_HAND)) {
            return;
        }

        BlockPos altar = AltarDrawClientState.altarPos();
        if (altar == null) return;

        HumanoidArm arm = mainWand
                ? minecraft.player.getMainArm()
                : minecraft.player.getMainArm().getOpposite();

        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float partial = minecraft.getFrameTime();
        float time = minecraft.player.tickCount + partial;


        Vec3 eye = minecraft.player.getEyePosition(partial);
        Vec3 altarTarget = new Vec3(
                altar.getX() + 0.5D,
                altar.getY() + 1.08D,
                altar.getZ() + 0.5D
        );
        Vec3 toAltar = altarTarget.subtract(eye).normalize();
        Vec3 look = minecraft.player.getLookAngle().normalize();

        double altarYaw = Math.atan2(-toAltar.x, toAltar.z);
        double lookYaw = Math.atan2(-look.x, look.z);
        double yawDelta = wrapRadians(altarYaw - lookYaw);

        double altarPitch = -Math.atan2(
                toAltar.y,
                Math.sqrt(toAltar.x * toAltar.x + toAltar.z * toAltar.z)
        );
        double lookPitch = -Math.atan2(
                look.y,
                Math.sqrt(look.x * look.x + look.z * look.z)
        );
        double pitchDelta = altarPitch - lookPitch;

        float yawDegrees = (float)Math.toDegrees(yawDelta);
        float pitchDegrees = (float)Math.toDegrees(pitchDelta);

        yawDegrees = Math.max(-55.0F, Math.min(55.0F, yawDegrees));
        pitchDegrees = Math.max(-45.0F, Math.min(45.0F, pitchDegrees));


        float swirlX = (float)Math.cos(time * 0.11F);
        float swirlY = (float)Math.sin(time * 0.11F);
        float breathe = (float)Math.sin(time * 0.055F + 0.6F);


        event.getPoseStack().translate(
                (-0.055F + swirlX * 0.004F) * side,
                -0.040F + swirlY * 0.003F,
                0.30F + breathe * 0.003F
        );


        event.getPoseStack().mulPose(
                Axis.XP.rotationDegrees(
                        10.0F
                                + pitchDegrees * 0.62F
                                + swirlY * 0.55F
                )
        );

        event.getPoseStack().mulPose(
                Axis.YP.rotationDegrees(
                        (
                                -5.0F
                                        + yawDegrees * 0.62F
                                        + swirlX * 0.45F
                        ) * side
                )
        );

        event.getPoseStack().mulPose(
                Axis.ZP.rotationDegrees(
                        (2.5F + swirlY * 0.65F) * side
                )
        );
    }

    @SubscribeEvent
    public static void renderMagicConduit(RenderLevelStageEvent event) {
        if (event.getStage()
                != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!isDrawing(minecraft)
                || minecraft.level == null
                || minecraft.player == null) {
            return;
        }

        BlockPos altarPos = AltarDrawClientState.altarPos();
        if (altarPos == null) return;

        float partial = event.getPartialTick();
        float growth = smoothStep(
                AltarDrawClientState.connectionProgress(partial)
        );

        Vec3 start = new Vec3(
                altarPos.getX() + 0.5D,
                altarPos.getY() + 1.16D,
                altarPos.getZ() + 0.5D
        );


        boolean firstPerson =
                minecraft.options.getCameraType().isFirstPerson();

        Vec3 capturedTip =
                WandTipTracker.get(firstPerson);


        Vec3 end =
                capturedTip != null
                        ? capturedTip
                        : castingWandTip(
                                minecraft,
                                altarPos,
                                partial
                        );

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();


        BufferBuilder outer = tess.getBuilder();
        outer.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );
        appendFlowTube(
                outer,
                matrix,
                start,
                end,
                partial,
                0.092F,
                128, 164, 255, 64,
                0.16D,
                growth,
                0.0D,
                growth
        );
        tess.end();

        BufferBuilder inner = tess.getBuilder();
        inner.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );
        appendFlowTube(
                inner,
                matrix,
                start,
                end,
                partial,
                0.033F,
                224, 235, 255, 205,
                0.30D,
                growth,
                0.0D,
                growth
        );
        tess.end();


        if (growth >= 0.999F) {
            BufferBuilder current = tess.getBuilder();
            current.begin(
                    VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_COLOR
            );
            appendDirectionalCurrent(
                    current,
                    matrix,
                    start,
                    end,
                    partial
            );
            tess.end();

            renderWandTipFlare(
                    tess,
                    matrix,
                    start,
                    end,
                    partial
            );
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderPulsePiece(
            Tesselator tess,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            float partial,
            double from,
            double to,
            float radius
    ) {
        if (to <= from) return;

        BufferBuilder pulse = tess.getBuilder();
        pulse.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );
        appendFlowTube(
                pulse,
                matrix,
                start,
                end,
                partial,
                radius,
                245, 249, 255, 245,
                0.34D,
                1.0F,
                from,
                to
        );
        tess.end();
    }

    private static void appendDirectionalCurrent(
            BufferBuilder buffer,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            float partial
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        double time =
                (minecraft.level.getGameTime() + partial)
                        * 0.22D;

        Vec3[] previous = buildCurrentRing(
                start,
                end,
                0.0D,
                time
        );

        for (int segment = 1;
             segment <= CURVE_SEGMENTS;
             segment++) {

            double p =
                    segment
                            / (double)CURVE_SEGMENTS;

            Vec3[] current =
                    buildCurrentRing(
                            start,
                            end,
                            p,
                            time
                    );


            double wave =
                    0.5D
                            + 0.5D
                            * Math.sin(
                                    p * Math.PI * 8.0D
                                            + time * 9.0D
                            );

            int alpha =
                    (int)(105
                            + wave * 135);

            int red =
                    (int)(205
                            + wave * 45);
            int green =
                    (int)(220
                            + wave * 32);
            int blue = 255;

            for (int side = 0;
                 side < TUBE_SIDES;
                 side++) {

                int next =
                        (side + 1)
                                % TUBE_SIDES;

                Vec3 v0 = previous[side];
                Vec3 v1 = previous[next];
                Vec3 v2 = current[next];
                Vec3 v3 = current[side];

                buffer.vertex(
                                matrix,
                                (float)v0.x,
                                (float)v0.y,
                                (float)v0.z
                        )
                        .color(
                                red,
                                green,
                                blue,
                                alpha
                        )
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v1.x,
                                (float)v1.y,
                                (float)v1.z
                        )
                        .color(
                                red,
                                green,
                                blue,
                                alpha
                        )
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v2.x,
                                (float)v2.y,
                                (float)v2.z
                        )
                        .color(
                                red,
                                green,
                                blue,
                                alpha
                        )
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v3.x,
                                (float)v3.y,
                                (float)v3.z
                        )
                        .color(
                                red,
                                green,
                                blue,
                                alpha
                        )
                        .endVertex();
            }

            previous = current;
        }
    }

    private static Vec3[] buildCurrentRing(
            Vec3 start,
            Vec3 end,
            double progress,
            double time
    ) {
        double epsilon =
                1.0D / CURVE_SEGMENTS;

        double p0 =
                Math.max(
                        0.0D,
                        progress - epsilon
                );

        double p1 =
                Math.min(
                        1.0D,
                        progress + epsilon
                );

        Vec3 center =
                flowPoint(
                        start,
                        end,
                        progress,
                        time
                );

        Vec3 before =
                flowPoint(
                        start,
                        end,
                        p0,
                        time
                );

        Vec3 after =
                flowPoint(
                        start,
                        end,
                        p1,
                        time
                );

        Vec3 tangent =
                after.subtract(before);

        if (tangent.lengthSqr()
                < 1.0E-8D) {
            tangent =
                    end.subtract(start);
        }

        tangent =
                tangent.normalize();

        Vec3 reference =
                Math.abs(tangent.y)
                        > 0.92D
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

        Vec3 axisA =
                tangent.cross(reference);

        if (axisA.lengthSqr()
                < 1.0E-8D) {
            axisA =
                    new Vec3(
                            1.0D,
                            0.0D,
                            0.0D
                    );
        } else {
            axisA =
                    axisA.normalize();
        }

        Vec3 axisB =
                tangent.cross(axisA);

        if (axisB.lengthSqr()
                < 1.0E-8D) {
            axisB =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            axisB =
                    axisB.normalize();
        }

        double wave =
                0.5D
                        + 0.5D
                        * Math.sin(
                                progress
                                        * Math.PI
                                        * 8.0D
                                        - time
                                        * 9.0D
                        );

        double radius =
                0.026D
                        + wave * 0.018D;

        Vec3[] ring =
                new Vec3[TUBE_SIDES];

        for (int side = 0;
             side < TUBE_SIDES;
             side++) {

            double angle =
                    Math.PI
                            * 2.0D
                            * side
                            / TUBE_SIDES;

            ring[side] =
                    center
                            .add(
                                    axisA.scale(
                                            Math.cos(angle)
                                                    * radius
                                    )
                            )
                            .add(
                                    axisB.scale(
                                            Math.sin(angle)
                                                    * radius
                                    )
                            );
        }

        return ring;
    }

    private static void renderWandTipFlare(
            Tesselator tess,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            float partial
    ) {
        double age = AltarDrawClientState.ageTicks(partial);
        double breathe = 0.5D + 0.5D * Math.sin(age * 0.45D);
        double from = Math.max(
                0.0D,
                0.965D - breathe * 0.010D
        );

        BufferBuilder flare = tess.getBuilder();
        flare.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );
        appendFlowTube(
                flare,
                matrix,
                start,
                end,
                partial,
                (float)(0.060D + breathe * 0.020D),
                255, 255, 255, 235,
                0.34D,
                1.0F,
                from,
                1.0D
        );
        tess.end();
    }

    private static boolean isDrawing(Minecraft minecraft) {
        if (minecraft.player == null
                || !AltarDrawClientState.active()) {
            return false;
        }

        return minecraft.player.getMainHandItem().getItem()
                instanceof WandItem
                || minecraft.player.getOffhandItem().getItem()
                instanceof WandItem;
    }


    private static Vec3 castingWandTip(
            Minecraft minecraft,
            BlockPos altarPos,
            float partialTick
    ) {


        double px =
                minecraft.player.xOld
                        + (minecraft.player.getX() - minecraft.player.xOld)
                        * partialTick;
        double py =
                minecraft.player.yOld
                        + (minecraft.player.getY() - minecraft.player.yOld)
                        * partialTick;
        double pz =
                minecraft.player.zOld
                        + (minecraft.player.getZ() - minecraft.player.zOld)
                        * partialTick;

        Vec3 playerPos =
                new Vec3(px, py, pz);

        Vec3 altarTarget =
                new Vec3(
                        altarPos.getX() + 0.5D,
                        altarPos.getY() + 1.08D,
                        altarPos.getZ() + 0.5D
                );

        boolean mainWand =
                minecraft.player.getMainHandItem().getItem()
                        instanceof WandItem;

        HumanoidArm castingArm =
                mainWand
                        ? minecraft.player.getMainArm()
                        : minecraft.player.getMainArm().getOpposite();

        double side =
                castingArm == HumanoidArm.RIGHT
                        ? 1.0D
                        : -1.0D;

        float bodyYaw =
                Mth.lerp(
                        partialTick,
                        minecraft.player.yBodyRotO,
                        minecraft.player.yBodyRot
                ) * Mth.DEG_TO_RAD;

        Vec3 bodyRight =
                new Vec3(
                        Math.cos(bodyYaw),
                        0.0D,
                        Math.sin(bodyYaw)
                );


        Vec3 shoulder =
                playerPos
                        .add(0.0D, 1.42D, 0.0D)
                        .add(bodyRight.scale(0.235D * side));

        Vec3 towardAltar =
                altarTarget.subtract(shoulder);

        if (towardAltar.lengthSqr() < 1.0E-6D) {
            towardAltar = minecraft.player.getLookAngle();
        } else {
            towardAltar = towardAltar.normalize();
        }


        Vec3 hand =
                shoulder.add(
                        towardAltar.scale(0.70D)
                );


        Vec3 horizontal =
                new Vec3(
                        towardAltar.x,
                        0.0D,
                        towardAltar.z
                );

        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal =
                    new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal =
                    horizontal.normalize();
        }

        Vec3 lateral =
                new Vec3(
                        -horizontal.z,
                        0.0D,
                        horizontal.x
                );

        float time =
                minecraft.player.tickCount
                        + partialTick;


        double swirlX =
                Math.cos(time * 0.11D);
        double swirlY =
                Math.sin(time * 0.11D);

        hand =
                hand
                        .add(
                                lateral.scale(
                                        swirlX
                                                * 0.006D
                                                * side
                                )
                        )
                        .add(
                                0.0D,
                                swirlY * 0.005D,
                                0.0D
                        );


        final double WAND_VISIBLE_LENGTH =
                0.62D;

        Vec3 tip =
                hand.add(
                        towardAltar.scale(
                                WAND_VISIBLE_LENGTH
                        )
                );


        return tip.add(
                0.0D,
                0.015D,
                0.0D
        );
    }


    private static void appendFlowTube(
            BufferBuilder buffer,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            float partialTick,
            float baseRadius,
            int r,
            int g,
            int b,
            int a,
            double flowSpeed,
            float visibleProgress,
            double minProgress,
            double maxProgress
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        double time =
                (minecraft.level.getGameTime() + partialTick)
                        * flowSpeed;

        minProgress = Math.max(0.0D, minProgress);
        maxProgress = Math.min(
                Math.min(1.0D, visibleProgress),
                maxProgress
        );

        if (maxProgress <= minProgress) return;

        int firstSegment = Math.max(
                0,
                (int)Math.floor(
                        minProgress * CURVE_SEGMENTS
                )
        );
        int lastSegment = Math.min(
                CURVE_SEGMENTS,
                (int)Math.ceil(
                        maxProgress * CURVE_SEGMENTS
                )
        );

        double firstP = Math.max(
                minProgress,
                firstSegment / (double)CURVE_SEGMENTS
        );
        Vec3[] previous = buildRing(
                start,
                end,
                firstP,
                time,
                baseRadius
        );
        double previousP = firstP;

        for (int segment = firstSegment + 1;
             segment <= lastSegment;
             segment++) {

            double p = Math.min(
                    maxProgress,
                    segment / (double)CURVE_SEGMENTS
            );
            if (p <= previousP) continue;

            Vec3[] current = buildRing(
                    start,
                    end,
                    p,
                    time,
                    baseRadius
            );

            for (int side = 0;
                 side < TUBE_SIDES;
                 side++) {

                int next =
                        (side + 1) % TUBE_SIDES;

                Vec3 v0 = previous[side];
                Vec3 v1 = previous[next];
                Vec3 v2 = current[next];
                Vec3 v3 = current[side];

                buffer.vertex(
                                matrix,
                                (float)v0.x,
                                (float)v0.y,
                                (float)v0.z
                        )
                        .color(r, g, b, a)
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v1.x,
                                (float)v1.y,
                                (float)v1.z
                        )
                        .color(r, g, b, a)
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v2.x,
                                (float)v2.y,
                                (float)v2.z
                        )
                        .color(r, g, b, a)
                        .endVertex();

                buffer.vertex(
                                matrix,
                                (float)v3.x,
                                (float)v3.y,
                                (float)v3.z
                        )
                        .color(r, g, b, a)
                        .endVertex();
            }

            previous = current;
            previousP = p;
        }
    }

    private static Vec3[] buildRing(
            Vec3 start,
            Vec3 end,
            double progress,
            double time,
            float baseRadius
    ) {
        double epsilon =
                1.0D / CURVE_SEGMENTS;

        double p0 = Math.max(
                0.0D,
                progress - epsilon
        );
        double p1 = Math.min(
                1.0D,
                progress + epsilon
        );

        Vec3 center =
                flowPoint(
                        start,
                        end,
                        progress,
                        time
                );

        Vec3 before =
                flowPoint(
                        start,
                        end,
                        p0,
                        time
                );

        Vec3 after =
                flowPoint(
                        start,
                        end,
                        p1,
                        time
                );

        Vec3 tangent =
                after.subtract(before);

        if (tangent.lengthSqr()
                < 1.0E-8D) {
            tangent =
                    end.subtract(start);
        }

        tangent =
                tangent.normalize();

        Vec3 reference =
                Math.abs(tangent.y)
                        > 0.92D
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

        Vec3 axisA =
                tangent.cross(reference);

        if (axisA.lengthSqr()
                < 1.0E-8D) {
            axisA =
                    new Vec3(
                            1.0D,
                            0.0D,
                            0.0D
                    );
        } else {
            axisA =
                    axisA.normalize();
        }

        Vec3 axisB =
                tangent.cross(axisA);

        if (axisB.lengthSqr()
                < 1.0E-8D) {
            axisB =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            axisB =
                    axisB.normalize();
        }


        double wave =
                0.5D
                        + 0.5D
                        * Math.sin(
                                progress
                                        * Math.PI
                                        * 10.0D
                                        + time
                                        * 7.0D
                        );

        double endpointFade =
                Math.sin(
                        Math.PI * progress
                );

        double radius =
                baseRadius
                        * (
                        1.0D
                                + wave
                                * 0.24D
                                * endpointFade
                );

        Vec3[] ring =
                new Vec3[TUBE_SIDES];

        for (int side = 0;
             side < TUBE_SIDES;
             side++) {

            double angle =
                    Math.PI
                            * 2.0D
                            * side
                            / TUBE_SIDES;

            Vec3 offset =
                    axisA.scale(
                                    Math.cos(angle)
                                            * radius
                            )
                            .add(
                                    axisB.scale(
                                            Math.sin(angle)
                                                    * radius
                                    )
                            );

            ring[side] =
                    center.add(offset);
        }

        return ring;
    }

    private static Vec3 flowPoint(
            Vec3 start,
            Vec3 end,
            double p,
            double time
    ) {
        Vec3 base =
                start.lerp(end, p);

        Vec3 direction =
                end.subtract(start)
                        .normalize();

        Vec3 side =
                direction.cross(
                        new Vec3(
                                0.0D,
                                1.0D,
                                0.0D
                        )
                );

        if (side.lengthSqr()
                < 1.0E-8D) {
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

        Vec3 vertical =
                side.cross(direction);

        if (vertical.lengthSqr()
                < 1.0E-8D) {
            vertical =
                    new Vec3(
                            0.0D,
                            1.0D,
                            0.0D
                    );
        } else {
            vertical =
                    vertical.normalize();
        }

        double envelope =
                Math.sin(
                        Math.PI * p
                );

        double traveling =
                p
                        * Math.PI
                        * 4.0D
                        + time
                        * 3.2D;

        return base
                .add(
                        0.0D,
                        Math.sin(Math.PI * p)
                                * 0.12D,
                        0.0D
                )
                .add(
                        side.scale(
                                Math.sin(traveling)
                                        * 0.035D
                                        * envelope
                        )
                )
                .add(
                        vertical.scale(
                                Math.cos(
                                        traveling
                                                * 0.83D
                                )
                                        * 0.022D
                                        * envelope
                        )
                );
    }

    private static float smoothStep(float value) {
        value = Math.max(
                0.0F,
                Math.min(1.0F, value)
        );
        return value
                * value
                * (3.0F - 2.0F * value);
    }

    private static double fractional(double value) {
        return value - Math.floor(value);
    }

    private static double wrapRadians(double value) {
        while (value <= -Math.PI) {
            value += Math.PI * 2.0D;
        }
        while (value > Math.PI) {
            value -= Math.PI * 2.0D;
        }
        return value;
    }
}
