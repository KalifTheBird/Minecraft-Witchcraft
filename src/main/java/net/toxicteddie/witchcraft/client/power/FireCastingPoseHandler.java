package net.toxicteddie.witchcraft.client.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;


@Mod.EventBusSubscriber(
        modid = Witchcraft.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class FireCastingPoseHandler {

    private static float savedXRot;
    private static float savedYRot;
    private static float savedZRot;
    private static boolean armPoseSaved;

    private FireCastingPoseHandler() {}

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || event.getEntity() != minecraft.player
                || !FirePowerInputHandler.isPrimaryFlameBlastActive()) {
            armPoseSaved = false;
            return;
        }

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        HumanoidArm mainArm = minecraft.player.getMainArm();

        var arm = mainArm == HumanoidArm.RIGHT
                ? model.rightArm
                : model.leftArm;

        savedXRot = arm.xRot;
        savedYRot = arm.yRot;
        savedZRot = arm.zRot;
        armPoseSaved = true;


        float pitch = minecraft.player.getXRot() * ((float) Math.PI / 180.0F);

        arm.xRot = -1.48F + pitch * 0.55F;
        arm.yRot = mainArm == HumanoidArm.RIGHT ? -0.16F : 0.16F;
        arm.zRot = mainArm == HumanoidArm.RIGHT ? -0.045F : 0.045F;
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!armPoseSaved
                || minecraft.player == null
                || event.getEntity() != minecraft.player) {
            return;
        }

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        HumanoidArm mainArm = minecraft.player.getMainArm();

        var arm = mainArm == HumanoidArm.RIGHT
                ? model.rightArm
                : model.leftArm;

        arm.xRot = savedXRot;
        arm.yRot = savedYRot;
        arm.zRot = savedZRot;
        armPoseSaved = false;
    }


    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !FirePowerInputHandler.isPrimaryFlameBlastActive()) {
            return;
        }

        InteractionHand mainHand = minecraft.player.getMainArm() == HumanoidArm.RIGHT
                ? InteractionHand.MAIN_HAND
                : InteractionHand.MAIN_HAND;

        if (event.getHand() != mainHand) return;

        PoseStack pose = event.getPoseStack();
        boolean right = minecraft.player.getMainArm() == HumanoidArm.RIGHT;
        float side = right ? 1.0F : -1.0F;


        pose.translate(-0.12F * side, -0.10F, -0.36F);
        pose.mulPose(Axis.XP.rotationDegrees(-18.0F));
        pose.mulPose(Axis.YP.rotationDegrees(-10.0F * side));
        pose.mulPose(Axis.ZP.rotationDegrees(3.0F * side));
    }
}
