package net.toxicteddie.witchcraft.client.power;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;


@Mod.EventBusSubscriber(
        modid = Witchcraft.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class TelekinesisCastingArmHandler {

    private TelekinesisCastingArmHandler() {}

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || event.getPlayer() != minecraft.player
                || !TelekinesisInputHandler.isMainTelekinesisActive()) {
            return;
        }

        HumanoidArm castingArm = minecraft.player.getMainArm();
        if (event.getArm() != castingArm) return;

        float partialTick = minecraft.getFrameTime();
        Vec3 target = TelekinesisInputHandler.getHeldTargetCenter(partialTick);
        if (target == null) return;

        Vec3 eye = minecraft.player.getEyePosition(partialTick);
        Vec3 toTarget = target.subtract(eye);
        if (toTarget.lengthSqr() < 1.0E-6D) return;
        toTarget = toTarget.normalize();

        Vec3 look = minecraft.player.getLookAngle().normalize();

        double targetYaw = Math.atan2(-toTarget.x, toTarget.z);
        double lookYaw = Math.atan2(-look.x, look.z);
        double yawDelta = wrapRadians(targetYaw - lookYaw);

        double targetPitch = -Math.atan2(
                toTarget.y,
                Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z)
        );
        double lookPitch = -Math.atan2(
                look.y,
                Math.sqrt(look.x * look.x + look.z * look.z)
        );
        double pitchDelta = targetPitch - lookPitch;

        float side = castingArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float time = minecraft.player.tickCount + partialTick;


        float breathe = (float) Math.sin(time * 0.16F);
        float drift = (float) Math.sin(time * 0.085F + 1.1F);


        event.getPoseStack().translate(
                (-0.030F + drift * 0.007F) * side,
                -0.022F + breathe * 0.005F,
                -0.105F
        );

        event.getPoseStack().mulPose(
                Axis.XP.rotationDegrees(
                        -6.0F
                                + (float) Math.toDegrees(pitchDelta) * 0.34F
                                + breathe * 1.6F
                )
        );

        event.getPoseStack().mulPose(
                Axis.YP.rotationDegrees(
                        (
                                -4.0F
                                        + (float) Math.toDegrees(yawDelta) * 0.38F
                                        + drift * 1.2F
                        ) * side
                )
        );

        event.getPoseStack().mulPose(
                Axis.ZP.rotationDegrees((2.0F + breathe * 1.0F) * side)
        );
    }

    private static double wrapRadians(double value) {
        while (value <= -Math.PI) value += Math.PI * 2.0D;
        while (value > Math.PI) value -= Math.PI * 2.0D;
        return value;
    }
}
