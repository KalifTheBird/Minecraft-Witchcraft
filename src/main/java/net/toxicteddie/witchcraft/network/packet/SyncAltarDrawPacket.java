package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.client.wand.AltarDrawClientState;

import java.util.function.Supplier;

public record SyncAltarDrawPacket(boolean active, BlockPos altarPos) {

    public static void encode(SyncAltarDrawPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.active());
        if (message.active()) {
            buffer.writeBlockPos(message.altarPos());
        }
    }

    public static SyncAltarDrawPacket decode(FriendlyByteBuf buffer) {
        boolean active = buffer.readBoolean();
        return new SyncAltarDrawPacket(
                active,
                active ? buffer.readBlockPos() : BlockPos.ZERO
        );
    }

    public static void handle(
            SyncAltarDrawPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (message.active()) {
                AltarDrawClientState.begin(message.altarPos());
            } else {
                AltarDrawClientState.clear();
            }
        });
        context.setPacketHandled(true);
    }
}
