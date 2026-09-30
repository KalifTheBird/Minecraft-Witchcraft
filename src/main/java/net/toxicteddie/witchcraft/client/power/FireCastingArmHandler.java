package net.toxicteddie.witchcraft.client.power;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
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
public final class FireCastingArmHandler {

    private FireCastingArmHandler() {}

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || event.getPlayer() != minecraft.player
                || !FirePowerInputHandler.isPrimaryFlameBlastActive()) {
            return;
        }

        HumanoidArm castingArm = minecraft.player.getMainArm();
        if (event.getArm() != castingArm) return;

        float side = castingArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;


        float time = minecraft.player.tickCount
                + minecraft.getFrameTime();


        float breathe = (float) Math.sin(time * 0.16F);
        float drift = (float) Math.sin(time * 0.085F + 1.10F);
        float pulse = (float) Math.sin(time * 0.31F + 0.45F);

        event.getPoseStack().translate(
                (-0.032F + drift * 0.007F) * side,
                -0.023F + breathe * 0.005F,
                -0.108F + pulse * 0.004F
        );

        event.getPoseStack().mulPose(
                Axis.XP.rotationDegrees(
                        -6.5F
                                + breathe * 1.7F
                                + pulse * 0.7F
                )
        );
        event.getPoseStack().mulPose(
                Axis.YP.rotationDegrees(
                        (-4.4F + drift * 1.35F) * side
                )
        );
        event.getPoseStack().mulPose(
                Axis.ZP.rotationDegrees(
                        (2.1F + breathe * 1.0F + pulse * 0.4F) * side
                )
        );
    }
}
