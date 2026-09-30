package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.power.telekinesis.TelekinesisManager;

import java.util.function.Supplier;

public record AirPowerActionPacket(Action action) {
    public enum Action { PULL, SECONDARY_PRESS, SECONDARY_RELEASE }

    public static void encode(AirPowerActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static AirPowerActionPacket decode(FriendlyByteBuf buffer) {
        return new AirPowerActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(AirPowerActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> {
                switch (packet.action) {
                    case PULL -> TelekinesisManager.pull(player);
                    case SECONDARY_PRESS -> TelekinesisManager.beginSecondary(player);
                    case SECONDARY_RELEASE -> TelekinesisManager.releaseSecondary(player);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
