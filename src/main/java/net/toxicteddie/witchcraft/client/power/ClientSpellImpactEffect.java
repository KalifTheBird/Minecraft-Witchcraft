package net.toxicteddie.witchcraft.client.power;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;


@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientSpellImpactEffect {
    private static final int DURATION_TICKS = 17;

    private static final ResourceLocation STRONG_SHADER =
            new ResourceLocation(Witchcraft.MOD_ID, "shaders/post/spell_echo_strong.json");
    private static final ResourceLocation MEDIUM_SHADER =
            new ResourceLocation(Witchcraft.MOD_ID, "shaders/post/spell_echo_medium.json");
    private static final ResourceLocation SOFT_SHADER =
            new ResourceLocation(Witchcraft.MOD_ID, "shaders/post/spell_echo_soft.json");

    private static int ticksRemaining;
    private static ShaderStage loadedStage = ShaderStage.NONE;
    private static boolean ownsPostEffect;

    private ClientSpellImpactEffect() {
    }

    public static void trigger() {

        ticksRemaining = DURATION_TICKS;
        loadedStage = ShaderStage.NONE;
        updateShaderStage();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) return;

        if (ticksRemaining > 0) {
            ticksRemaining--;
            updateShaderStage();
        } else {
            stopOwnedShader();
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (ticksRemaining <= 0) return;

        double age = DURATION_TICKS - ticksRemaining + event.getPartialTick();
        float fade = intensity((float) event.getPartialTick());


        double firstHit = gaussian(age, 1.35D, 0.82D);
        double rebound = gaussian(age, 3.85D, 1.15D);
        double secondPulse = gaussian(age, 6.65D, 1.45D);
        double settle = gaussian(age, 10.20D, 2.10D);

        double yawOffset = (firstHit * 1.80D - rebound * 1.05D
                + secondPulse * 0.42D - settle * 0.12D) * fade;
        double pitchOffset = (-firstHit * 0.56D + rebound * 0.40D
                - secondPulse * 0.16D) * fade;
        double rollOffset = (firstHit * 2.35D - rebound * 1.38D
                + secondPulse * 0.58D - settle * 0.16D) * fade;

        event.setYaw((float) (event.getYaw() + yawOffset));
        event.setPitch((float) (event.getPitch() + pitchOffset));
        event.setRoll((float) (event.getRoll() + rollOffset));
    }

    private static void updateShaderStage() {
        if (ticksRemaining <= 0) {
            stopOwnedShader();
            return;
        }

        ShaderStage wanted = ticksRemaining >= 12
                ? ShaderStage.STRONG
                : ticksRemaining >= 6
                ? ShaderStage.MEDIUM
                : ShaderStage.SOFT;

        if (wanted == loadedStage) return;

        Minecraft minecraft = Minecraft.getInstance();


        if (ownsPostEffect) {
            minecraft.gameRenderer.shutdownEffect();
            ownsPostEffect = false;
        }

        minecraft.gameRenderer.loadEffect(shaderFor(wanted));
        ownsPostEffect = true;
        loadedStage = wanted;
    }

    private static ResourceLocation shaderFor(ShaderStage stage) {
        return switch (stage) {
            case STRONG -> STRONG_SHADER;
            case MEDIUM -> MEDIUM_SHADER;
            default -> SOFT_SHADER;
        };
    }

    private static void stopOwnedShader() {
        if (ownsPostEffect) {
            Minecraft.getInstance().gameRenderer.shutdownEffect();
        }
        ownsPostEffect = false;
        loadedStage = ShaderStage.NONE;
    }

    private static float intensity(float partialTick) {
        float remaining = Math.max(0.0F, ticksRemaining - partialTick);
        float normalized = remaining / DURATION_TICKS;
        return normalized * normalized * (3.0F - 2.0F * normalized);
    }

    private static double gaussian(double value, double center, double width) {
        double distance = (value - center) / width;
        return Math.exp(-(distance * distance));
    }

    private enum ShaderStage {
        NONE,
        STRONG,
        MEDIUM,
        SOFT
    }
}
