package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.book.BookContentLayout;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;

import java.util.function.Supplier;

public record BookButtonClickPacket(boolean entity, BlockPos pos, int entityId, int targetPage) {
    public static BookButtonClickPacket forBlock(BlockPos pos, int target) {
        return new BookButtonClickPacket(false, pos, -1, target);
    }
    public static BookButtonClickPacket forEntity(int id, int target) {
        return new BookButtonClickPacket(true, BlockPos.ZERO, id, target);
    }
    public static void encode(BookButtonClickPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.entity); buf.writeBlockPos(msg.pos); buf.writeVarInt(msg.entityId); buf.writeVarInt(msg.targetPage);
    }
    public static BookButtonClickPacket decode(FriendlyByteBuf buf) {
        return new BookButtonClickPacket(buf.readBoolean(), buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }
    public static void handle(BookButtonClickPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ServerPlayer player = ctx.getSender();
        ctx.enqueueWork(() -> {
            if (player == null || msg.targetPage < 0) return;
            if (msg.entity) {
                var e = player.level().getEntity(msg.entityId);
                if (!(e instanceof BookOfShadowsFollowingEntity book) || player.distanceToSqr(book) > 49.0D) return;
                if (!book.canReadBoundBook(player) || !book.isReaderActive()) return;
                var pages = BookContentLayout.build(OpenBookOfShadowsPacket.forPlayer(player, book.getContentSourcePos()));
                if (validTarget(pages, book.getReaderPage(), msg.targetPage)) book.jumpReaderPage(msg.targetPage);
            } else {
                if (player.distanceToSqr(msg.pos.getX()+0.5D, msg.pos.getY()+0.5D, msg.pos.getZ()+0.5D) > 49.0D) return;
                if (!(player.level().getBlockEntity(msg.pos) instanceof BookOfShadowsBlockEntity book)) return;
                if (!book.canRead(player) || !book.isReaderActive()) return;
                var pages = BookContentLayout.build(OpenBookOfShadowsPacket.forPlayer(player, msg.pos));
                if (validTarget(pages, book.getReaderPage(), msg.targetPage)) book.jumpReaderPage(msg.targetPage);
            }
        });
        ctx.setPacketHandled(true);
    }
    private static boolean validTarget(java.util.List<BookContentLayout.Page> pages, int spread, int target) {
        if (target < 0 || target >= pages.size()) return false;


        BookContentLayout.Page targetPage = pages.get(target);
        if (target == 2 && "Index".equals(targetPage.title())) return true;


        for (int p = spread; p <= spread + 1; p++) {
            if (p < 0 || p >= pages.size()) continue;
            for (BookContentLayout.Button b : pages.get(p).buttons()) {
                if (b.targetPage() == target) return true;
            }
        }
        return false;
    }
}
