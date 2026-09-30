package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.client.power.ClientSpellImpactEffect;

import java.util.function.Supplier;


public final class SpellImpactEffectPacket {
    public SpellImpactEffectPacket() {}

    public static void encode(SpellImpactEffectPacket packet, FriendlyByteBuf buffer) {
    }

    public static SpellImpactEffectPacket decode(FriendlyByteBuf buffer) {
        return new SpellImpactEffectPacket();
    }

    public static void handle(SpellImpactEffectPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSpellImpactEffect.trigger()
        ));
        context.setPacketHandled(true);
    }
}
