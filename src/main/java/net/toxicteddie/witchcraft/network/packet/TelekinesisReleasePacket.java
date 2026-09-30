package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.power.telekinesis.TelekinesisManager;

import java.util.function.Supplier;

public record TelekinesisReleasePacket(boolean allowFling) {
    public static void encode(TelekinesisReleasePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.allowFling);
    }

    public static TelekinesisReleasePacket decode(FriendlyByteBuf buffer) {
        return new TelekinesisReleasePacket(buffer.readBoolean());
    }

    public static void handle(TelekinesisReleasePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) context.enqueueWork(() -> TelekinesisManager.release(player, packet.allowFling));
        context.setPacketHandled(true);
    }
}
