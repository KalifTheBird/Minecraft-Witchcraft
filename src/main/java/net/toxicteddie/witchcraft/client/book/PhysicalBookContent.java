package net.toxicteddie.witchcraft.client.book;

import net.minecraft.core.BlockPos;
import net.toxicteddie.witchcraft.book.BookContentLayout;
import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class PhysicalBookContent {
    public record Page(String id, String type, String title, String body, String footer, int inkColor,
                       List<BookContentLayout.Button> buttons) {}

    private static final Map<BlockPos, List<Page>> BOOKS = new HashMap<>();
    private PhysicalBookContent() {}

    public static void accept(OpenBookOfShadowsPacket packet) {
        List<Page> pages = BookContentLayout.build(packet).stream()
                .map(p -> new Page(p.id(), p.type(), p.title(), p.body(), p.footer(), p.inkColor(), p.buttons()))
                .toList();
        BOOKS.put(packet.bookPos().immutable(), List.copyOf(pages));
    }

    public static Page page(BlockPos pos, int index) {
        List<Page> pages = BOOKS.get(pos);
        if (pages == null || index < 0 || index >= pages.size()) return null;
        return pages.get(index);
    }
}
