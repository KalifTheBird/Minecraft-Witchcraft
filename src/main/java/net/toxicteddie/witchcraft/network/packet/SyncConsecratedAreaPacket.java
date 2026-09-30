package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.client.ClientConsecratedAreaData;
import java.util.function.Supplier;

public record SyncConsecratedAreaPacket(boolean inside) {
    public static void encode(SyncConsecratedAreaPacket packet, FriendlyByteBuf buffer) { buffer.writeBoolean(packet.inside()); }
    public static SyncConsecratedAreaPacket decode(FriendlyByteBuf buffer) { return new SyncConsecratedAreaPacket(buffer.readBoolean()); }
    public static void handle(SyncConsecratedAreaPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientConsecratedAreaData.setInside(packet.inside()));
        context.setPacketHandled(true);
    }
}
