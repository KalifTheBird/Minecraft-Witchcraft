package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.client.ClientMagicData;

import java.util.function.Supplier;

public record SyncMagicPacket(int magic, boolean witch) {
    public static void encode(SyncMagicPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.magic);
        buffer.writeBoolean(packet.witch);
    }

    public static SyncMagicPacket decode(FriendlyByteBuf buffer) {
        return new SyncMagicPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(SyncMagicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientMagicData.update(packet.magic, packet.witch));
        context.setPacketHandled(true);
    }
}
