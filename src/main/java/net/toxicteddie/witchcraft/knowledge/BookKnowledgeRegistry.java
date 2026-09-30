package net.toxicteddie.witchcraft.knowledge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.toxicteddie.witchcraft.Witchcraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class BookKnowledgeRegistry extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final ResourceLocation CHAPTERS_FILE =
            new ResourceLocation(Witchcraft.MOD_ID, "book_of_shadows/chapters.json");
    public static final BookKnowledgeRegistry INSTANCE = new BookKnowledgeRegistry();

    private volatile Map<ResourceLocation, BookKnowledgeEntry> entries = Map.of();
    private volatile List<BookChapter> chapters = List.of();

    private BookKnowledgeRegistry() {
        super(GSON, "book_of_shadows/entries");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, BookKnowledgeEntry> loaded = new LinkedHashMap<>();
        jsons.forEach((id, element) -> {
            if (!element.isJsonObject()) return;
            JsonObject json = element.getAsJsonObject();
            String chapter = string(json, "chapter", "misc");
            String title = string(json, "title", id.getPath());
            String type = string(json, "type", "entry");
            boolean secret = json.has("secret") && json.get("secret").getAsBoolean();
            String summary = string(json, "summary", "");
            loaded.put(id, new BookKnowledgeEntry(id, chapter, title, type, secret, summary,
                    strings(json.getAsJsonArray("pages")), strings(json.getAsJsonArray("prerequisites"))));
        });
        entries = Collections.unmodifiableMap(loaded);
        chapters = loadChapters(manager);
    }

    private static List<BookChapter> loadChapters(ResourceManager manager) {
        List<BookChapter> result = new ArrayList<>();
        manager.getResource(CHAPTERS_FILE).ifPresent(resource -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.open(), StandardCharsets.UTF_8))) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray array = root.getAsJsonArray("chapters");
                if (array != null) {
                    for (JsonElement element : array) {
                        if (!element.isJsonObject()) continue;
                        JsonObject json = element.getAsJsonObject();
                        String id = string(json, "id", "misc");
                        String title = string(json, "title", id);
                        int order = json.has("order") ? json.get("order").getAsInt() : 0;
                        boolean secret = json.has("secret") && json.get("secret").getAsBoolean();
                        result.add(new BookChapter(id, title, order, secret));
                    }
                }
            } catch (Exception ignored) {

            }
        });
        result.sort(Comparator.comparingInt(BookChapter::order));
        return List.copyOf(result);
    }

    public Collection<BookKnowledgeEntry> all() { return entries.values(); }
    public Collection<String> ids() { return entries.keySet().stream().map(ResourceLocation::toString).toList(); }
    public BookKnowledgeEntry get(ResourceLocation id) { return entries.get(id); }
    public List<BookChapter> chapters() { return chapters; }

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static List<String> strings(JsonArray array) {
        if (array == null) return List.of();
        List<String> result = new ArrayList<>();
        for (JsonElement element : array) result.add(element.getAsString());
        return List.copyOf(result);
    }
}
