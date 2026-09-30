package net.toxicteddie.witchcraft.client.book;
import java.util.HashMap;
import java.util.Map;

public final class BookCreaturePreviewState {
    private static final Map<String, Float> ROTATION = new HashMap<>();
    private BookCreaturePreviewState() {}
    public static float rotation(String id){ return ROTATION.getOrDefault(id,0F); }
    public static void rotate(String id,double delta){ ROTATION.put(id, rotation(id)+(float)delta*18F); }
}
