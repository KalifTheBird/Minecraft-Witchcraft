package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.power.telekinesis.TelekinesisManager;

import java.util.function.Supplier;

public final class TelekinesisStartPacket {
    public static void encode(TelekinesisStartPacket packet, FriendlyByteBuf buffer) {}
    public static TelekinesisStartPacket decode(FriendlyByteBuf buffer) { return new TelekinesisStartPacket(); }

    public static void handle(TelekinesisStartPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) context.enqueueWork(() -> TelekinesisManager.start(player));
        context.setPacketHandled(true);
    }
}
