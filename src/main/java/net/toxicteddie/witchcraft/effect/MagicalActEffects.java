package net.toxicteddie.witchcraft.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SpellImpactEffectPacket;
import net.toxicteddie.witchcraft.sound.ModSounds;


public final class MagicalActEffects {
    private static final float IMPACT_VOLUME = 0.72F;
    private static final float BASE_PITCH = 0.92F;
    private static final float PITCH_VARIATION = 0.08F;

    private MagicalActEffects() {
    }


    public static void impact(ServerPlayer caster) {
        impact(caster, caster.position());
    }


    public static void impact(ServerPlayer caster, BlockPos origin) {
        impact(caster, Vec3.atCenterOf(origin));
    }


    public static void impact(ServerPlayer caster, Vec3 origin) {
        if (caster == null || caster.isRemoved()) return;

        float pitch = BASE_PITCH
                + caster.getRandom().nextFloat() * PITCH_VARIATION;

        caster.serverLevel().playSound(
                null,
                origin.x,
                origin.y,
                origin.z,
                ModSounds.SPELL_IMPACT.get(),
                SoundSource.PLAYERS,
                IMPACT_VOLUME,
                pitch
        );

        ModNetworking.sendToPlayer(new SpellImpactEffectPacket(), caster);
    }
}
