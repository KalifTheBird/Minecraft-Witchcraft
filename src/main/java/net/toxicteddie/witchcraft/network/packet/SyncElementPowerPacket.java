package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.client.ClientElementPowerData;

import java.util.function.Supplier;

public record SyncElementPowerPacket(int air, int fire, int water, int earth) {
    public static void encode(SyncElementPowerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.air);
        buffer.writeVarInt(packet.fire);
        buffer.writeVarInt(packet.water);
        buffer.writeVarInt(packet.earth);
    }

    public static SyncElementPowerPacket decode(FriendlyByteBuf buffer) {
        return new SyncElementPowerPacket(
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()
        );
    }

    public static void handle(SyncElementPowerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientElementPowerData.update(
                packet.air, packet.fire, packet.water, packet.earth
        ));
        context.setPacketHandled(true);
    }
}
