package net.toxicteddie.witchcraft.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.toxicteddie.witchcraft.knowledge.BookChapter;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeEntry;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeManager;
import net.toxicteddie.witchcraft.knowledge.BookKnowledgeRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public record OpenBookOfShadowsPacket(BlockPos bookPos, String signatureName, boolean legible,
                                      List<ChapterData> chapters, List<EntryData> entries) {
    public record ChapterData(String id, String title, int order) {}
    public record EntryData(String id, String chapter, String title, String type,
                            String summary, List<String> pages) {}

    public static OpenBookOfShadowsPacket forPlayer(ServerPlayer player, BlockPos pos) {
        Set<String> known = BookKnowledgeManager.getKnownEntries(player);
        List<EntryData> visible = new ArrayList<>();
        Set<String> usedChapters = new HashSet<>();

        for (BookKnowledgeEntry entry : BookKnowledgeRegistry.INSTANCE.all()) {
            String id = entry.id().toString();
            if (!known.contains(id)) continue;


            boolean prerequisitesMet = entry.prerequisites().stream().allMatch(known::contains);
            if (!prerequisitesMet) continue;
            visible.add(new EntryData(id, entry.chapter(), entry.title(), entry.type(),
                    entry.summary(), entry.pages()));
            usedChapters.add(entry.chapter());
        }

        Map<String, BookChapter> metadata = new HashMap<>();
        for (BookChapter chapter : BookKnowledgeRegistry.INSTANCE.chapters()) {
            metadata.put(chapter.id(), chapter);
        }

        List<ChapterData> chapters = new ArrayList<>();


        for (BookChapter chapter : BookKnowledgeRegistry.INSTANCE.chapters()) {
            if (!chapter.secret() || usedChapters.contains(chapter.id())) {
                chapters.add(new ChapterData(chapter.id(), chapter.title(), chapter.order()));
            }
        }

        for (String chapterId : usedChapters) {
            if (metadata.containsKey(chapterId)) continue;
            chapters.add(new ChapterData(chapterId, prettify(chapterId), 50));
        }
        chapters.sort(Comparator.comparingInt(ChapterData::order));

        Map<String, Integer> chapterOrder = new HashMap<>();
        for (ChapterData chapter : chapters) chapterOrder.put(chapter.id(), chapter.order());
        visible.sort(Comparator
                .comparingInt((EntryData e) -> chapterOrder.getOrDefault(e.chapter(), 999))
                .thenComparing(EntryData::title));

        return new OpenBookOfShadowsPacket(pos.immutable(), player.getGameProfile().getName(), true,
                List.copyOf(chapters), List.copyOf(visible));
    }


    public static OpenBookOfShadowsPacket gibberish(BlockPos pos) {
        return new OpenBookOfShadowsPacket(pos.immutable(), "", false, List.of(), List.of());
    }

    private static String prettify(String id) {
        String[] words = id.replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    public static void encode(OpenBookOfShadowsPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.bookPos);
        buf.writeUtf(packet.signatureName);
        buf.writeBoolean(packet.legible);
        buf.writeVarInt(packet.chapters.size());
        for (ChapterData chapter : packet.chapters) {
            buf.writeUtf(chapter.id);
            buf.writeUtf(chapter.title);
            buf.writeVarInt(chapter.order);
        }
        buf.writeVarInt(packet.entries.size());
        for (EntryData entry : packet.entries) {
            buf.writeUtf(entry.id);
            buf.writeUtf(entry.chapter);
            buf.writeUtf(entry.title);
            buf.writeUtf(entry.type);
            buf.writeUtf(entry.summary, 32767);
            buf.writeVarInt(entry.pages.size());
            for (String page : entry.pages) buf.writeUtf(page, 32767);
        }
    }

    public static OpenBookOfShadowsPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String signatureName = buf.readUtf();
        boolean legible = buf.readBoolean();
        int chapterCount = buf.readVarInt();
        List<ChapterData> chapters = new ArrayList<>(chapterCount);
        for (int i = 0; i < chapterCount; i++) {
            chapters.add(new ChapterData(buf.readUtf(), buf.readUtf(), buf.readVarInt()));
        }
        int entryCount = buf.readVarInt();
        List<EntryData> entries = new ArrayList<>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            String id = buf.readUtf();
            String chapter = buf.readUtf();
            String title = buf.readUtf();
            String type = buf.readUtf();
            String summary = buf.readUtf(32767);
            int pageCount = buf.readVarInt();
            List<String> pages = new ArrayList<>(pageCount);
            for (int p = 0; p < pageCount; p++) pages.add(buf.readUtf(32767));
            entries.add(new EntryData(id, chapter, title, type, summary, List.copyOf(pages)));
        }
        return new OpenBookOfShadowsPacket(pos, signatureName, legible, List.copyOf(chapters), List.copyOf(entries));
    }

    public static void handle(OpenBookOfShadowsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> net.toxicteddie.witchcraft.client.book.BookOfShadowsClient.open(packet)));
        context.setPacketHandled(true);
    }
}
