package net.toxicteddie.witchcraft.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.path.PlayerPathManager;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.PlayerElementSelectionManager;

public record SelectElementPowerPacket(boolean powerMode, int selectedIndex) {
    public static void encode(SelectElementPowerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.powerMode);
        buffer.writeVarInt(packet.selectedIndex);
    }

    public static SelectElementPowerPacket decode(FriendlyByteBuf buffer) {
        return new SelectElementPowerPacket(buffer.readBoolean(), buffer.readVarInt());
    }

    public static void handle(SelectElementPowerPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;


            PlayerElementSelectionManager.recoverLegacyStashedStack(player);

            if (!PlayerPathManager.isWitch(player)) {
                PlayerElementSelectionManager.reset(player);
                return;
            }

            int index = Math.max(0,
                    Math.min(packet.selectedIndex(), ElementPower.values().length - 1));
            PlayerElementSelectionManager.applySelection(player, packet.powerMode(), index);
        });
        context.setPacketHandled(true);
    }
}
