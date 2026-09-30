package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.power.fire.FirePowerManager;

import java.util.function.Supplier;

public record FirePowerActionPacket(Action action) {
    public enum Action {
        PRIMARY_PRESS,
        PRIMARY_RELEASE,
        STORM_LIGHTNING,
        SECONDARY_PRESS,
        SECONDARY_RELEASE,
        RADIAL_IGNITE_PRESS,
        RADIAL_IGNITE_RELEASE
    }

    public static void encode(FirePowerActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static FirePowerActionPacket decode(FriendlyByteBuf buffer) {
        return new FirePowerActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(FirePowerActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> {
                switch (packet.action) {
                    case PRIMARY_PRESS -> FirePowerManager.beginPrimary(player);
                    case PRIMARY_RELEASE -> FirePowerManager.releasePrimary(player);
                    case STORM_LIGHTNING -> FirePowerManager.stormLightning(player);
                    case SECONDARY_PRESS -> FirePowerManager.beginSecondary(player);
                    case SECONDARY_RELEASE -> FirePowerManager.releaseSecondary(player);
                    case RADIAL_IGNITE_PRESS -> FirePowerManager.beginRadialIgnite(player);
                    case RADIAL_IGNITE_RELEASE -> FirePowerManager.releaseRadialIgnite(player);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
