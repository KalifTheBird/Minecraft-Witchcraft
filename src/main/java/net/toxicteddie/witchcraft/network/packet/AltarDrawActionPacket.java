package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.wand.AltarDrawManager;

import java.util.function.Supplier;

public record AltarDrawActionPacket(Action action) {

    public enum Action {
        STOP
    }

    public static void encode(AltarDrawActionPacket message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action());
    }

    public static AltarDrawActionPacket decode(FriendlyByteBuf buffer) {
        return new AltarDrawActionPacket(buffer.readEnum(Action.class));
    }

    public static void handle(
            AltarDrawActionPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        context.enqueueWork(() -> {
            if (player != null && message.action() == Action.STOP) {
                AltarDrawManager.stop(player);
            }
        });

        context.setPacketHandled(true);
    }
}
