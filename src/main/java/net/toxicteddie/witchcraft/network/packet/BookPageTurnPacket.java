package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;

import java.util.function.Supplier;


public record BookPageTurnPacket(BlockPos bookPos, int direction) {
    public static void encode(BookPageTurnPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.bookPos);
        buf.writeByte(packet.direction < 0 ? -1 : 1);
    }

    public static BookPageTurnPacket decode(FriendlyByteBuf buf) {
        return new BookPageTurnPacket(buf.readBlockPos(), buf.readByte() < 0 ? -1 : 1);
    }

    public static void handle(BookPageTurnPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;


            if (player.distanceToSqr(packet.bookPos.getX() + 0.5D,
                    packet.bookPos.getY() + 0.5D, packet.bookPos.getZ() + 0.5D) <= 100.0D
                    && player.level().getBlockEntity(packet.bookPos) instanceof BookOfShadowsBlockEntity book
                    && book.canRead(player)
                    && book.isOpenOrTransitioning()) {
                book.turnReaderPage(packet.direction);
                return;
            }


            BookOfShadowsFollowingEntity following =
                    BookOfShadowsFollowingEntity.findReadableBook(player.level(), player, packet.bookPos);
            if (following != null) {
                following.turnReaderPage(packet.direction);
            }
        });
        context.setPacketHandled(true);
    }
}
