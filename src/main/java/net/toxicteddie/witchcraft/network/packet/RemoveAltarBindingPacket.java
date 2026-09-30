package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.block.entity.WitchesAltarBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public record RemoveAltarBindingPacket(BlockPos altarPos, UUID witchUuid) {
    public static void encode(RemoveAltarBindingPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.altarPos);
        buffer.writeUUID(packet.witchUuid);
    }

    public static RemoveAltarBindingPacket decode(FriendlyByteBuf buffer) {
        return new RemoveAltarBindingPacket(buffer.readBlockPos(), buffer.readUUID());
    }

    public static void handle(RemoveAltarBindingPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || sender.distanceToSqr(
                    packet.altarPos.getX() + 0.5D,
                    packet.altarPos.getY() + 0.5D,
                    packet.altarPos.getZ() + 0.5D
            ) > 64.0D) {
                return;
            }

            if (sender.level().getBlockEntity(packet.altarPos) instanceof WitchesAltarBlockEntity altar) {
                altar.removeBoundWitch(sender, packet.witchUuid);
            }
        });
        context.setPacketHandled(true);
    }
}
