package net.toxicteddie.witchcraft.knowledge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;


public final class BookKnowledgeManager {
    private static final String ROOT = "WitchcraftBookKnowledge";
    private static final String KNOWN = "KnownEntries";
    private static final String INITIATED = "Initiated";

    private BookKnowledgeManager() {}


    private static final Map<String, String> LEGACY_IDS = legacyIds();

    private static Map<String, String> legacyIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        ids.put("witchcraft:the_book_of_shadows", "witchcraft:book_of_shadows/the_book_of_shadows");
        ids.put("witchcraft:blood_binding", "witchcraft:book_of_shadows/blood_binding");
        ids.put("witchcraft:knowledge", "witchcraft:book_of_shadows/knowledge");
        ids.put("witchcraft:beginning/signing_in_blood", "witchcraft:path_of_night/signing_in_blood");
        ids.put("witchcraft:beginning/the_shadow_man", "witchcraft:path_of_night/the_shadow_man");
        ids.put("witchcraft:beginning/your_first_night", "witchcraft:path_of_night/your_first_night");
        ids.put("witchcraft:witchcraft/magical_states", "witchcraft:path_of_night/magical_states");
        ids.put("witchcraft:witchcraft/body_magic", "witchcraft:witchcraft/body_magic/body_magic");
        ids.put("witchcraft:witchcraft/wands", "witchcraft:items/wands");
        ids.put("witchcraft:witchcraft/altar", "witchcraft:blocks/altar");
        ids.put("witchcraft:green/belladonna", "witchcraft:natural_world/belladonna");
        ids.put("witchcraft:green/elder", "witchcraft:natural_world/elder");
        ids.put("witchcraft:green/hemlock", "witchcraft:natural_world/hemlock");
        ids.put("witchcraft:green/mandrake", "witchcraft:natural_world/mandrake");
        ids.put("witchcraft:green/rowan", "witchcraft:natural_world/rowan");
        ids.put("witchcraft:preparations/dark_unction", "witchcraft:brews/dark_unction");
        ids.put("witchcraft:preparations/milk_of_the_mother", "witchcraft:brews/milk_of_the_mother");
        ids.put("witchcraft:preparations/pale_moonlight", "witchcraft:brews/pale_moonlight");
        ids.put("witchcraft:preparations/tinctures", "witchcraft:brews/tinctures");
        ids.put("witchcraft:preparations/potions", "witchcraft:brews/potions");
        ids.put("witchcraft:circles/four_corners", "witchcraft:witchcraft/circle_magic/four_corners");
        ids.put("witchcraft:circles/rite_of_night", "witchcraft:witchcraft/white_magic/rite_of_night");
        ids.put("witchcraft:circles/temperance", "witchcraft:witchcraft/grey_magic/temperance");
        ids.put("witchcraft:chronicle/first_circle", "witchcraft:witchcraft/circle_magic/first_circle");
        ids.put("witchcraft:creatures/demons", "witchcraft:entities/demons");
        ids.put("witchcraft:creatures/imps", "witchcraft:entities/imps");
        ids.put("witchcraft:forbidden/earths_veins", "witchcraft:witchcraft/circle_magic/earths_veins");
        ids.put("witchcraft:forbidden/transmutation", "witchcraft:witchcraft/grey_magic/transmutation");
        ids.put("witchcraft:forbidden/hollowing", "witchcraft:witchcraft/black_magic/hollowing");
        ids.put("witchcraft:altar", "witchcraft:blocks/altar");
        ids.put("witchcraft:belladonna", "witchcraft:natural_world/belladonna");
        ids.put("witchcraft:body_magic", "witchcraft:witchcraft/body_magic/body_magic");
        ids.put("witchcraft:dark_unction", "witchcraft:brews/dark_unction");
        ids.put("witchcraft:demons", "witchcraft:entities/demons");
        ids.put("witchcraft:elder", "witchcraft:natural_world/elder");
        ids.put("witchcraft:first_circle", "witchcraft:witchcraft/circle_magic/first_circle");
        ids.put("witchcraft:four_corners", "witchcraft:witchcraft/circle_magic/four_corners");
        ids.put("witchcraft:hemlock", "witchcraft:natural_world/hemlock");
        ids.put("witchcraft:imps", "witchcraft:entities/imps");
        ids.put("witchcraft:magical_states", "witchcraft:path_of_night/magical_states");
        ids.put("witchcraft:mandrake", "witchcraft:natural_world/mandrake");
        ids.put("witchcraft:milk_of_the_mother", "witchcraft:brews/milk_of_the_mother");
        ids.put("witchcraft:pale_moonlight", "witchcraft:brews/pale_moonlight");
        ids.put("witchcraft:rite_of_night", "witchcraft:witchcraft/white_magic/rite_of_night");
        ids.put("witchcraft:rowan", "witchcraft:natural_world/rowan");
        ids.put("witchcraft:signing_in_blood", "witchcraft:path_of_night/signing_in_blood");
        ids.put("witchcraft:temperance", "witchcraft:witchcraft/grey_magic/temperance");
        ids.put("witchcraft:the_shadow_man", "witchcraft:path_of_night/the_shadow_man");
        ids.put("witchcraft:wands", "witchcraft:items/wands");
        ids.put("witchcraft:your_first_night", "witchcraft:path_of_night/your_first_night");
        return Map.copyOf(ids);
    }


    private static CompoundTag data(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        if (!persisted.contains(ROOT, Tag.TAG_COMPOUND)) {
            persisted.put(ROOT, new CompoundTag());
        }
        return persisted.getCompound(ROOT);
    }

    public static boolean isInitiated(Player player) {
        return data(player).getBoolean(INITIATED);
    }

    public static void setInitiated(Player player, boolean initiated) {
        data(player).putBoolean(INITIATED, initiated);
    }

    public static Set<String> getKnownEntries(Player player) {
        LinkedHashSet<String> entries = new LinkedHashSet<>();
        ListTag list = data(player).getList(KNOWN, Tag.TAG_STRING);
        boolean migrated = false;
        for (int i = 0; i < list.size(); i++) {
            String stored = list.getString(i);
            String canonical = LEGACY_IDS.getOrDefault(stored, stored);
            entries.add(canonical);
            migrated |= !canonical.equals(stored);
        }


        if (isInitiated(player)) {
            migrated |= entries.add("witchcraft:path_of_night/the_witch");
            migrated |= entries.add("witchcraft:path_of_night/magical_states");
            migrated |= entries.add("witchcraft:path_of_night/familiars");
        }
        if (migrated) write(player, entries);
        return entries;
    }

    public static boolean knows(Player player, String id) {
        return getKnownEntries(player).contains(id);
    }

    public static boolean unlock(Player player, String id) {
        Set<String> known = getKnownEntries(player);
        if (!known.add(id)) return false;
        write(player, known);
        return true;
    }

    public static boolean lock(Player player, String id) {
        Set<String> known = getKnownEntries(player);
        if (!known.remove(id)) return false;
        write(player, known);
        return true;
    }

    public static int unlockAll(Player player, Collection<String> ids) {
        Set<String> known = getKnownEntries(player);
        int before = known.size();
        known.addAll(ids);
        write(player, known);
        return known.size() - before;
    }

    public static void reset(Player player) {
        CompoundTag root = data(player);
        root.remove(KNOWN);
        root.putBoolean(INITIATED, false);
    }

    public static void unlockInitiationEntries(Player player) {
        setInitiated(player, true);
        unlock(player, "witchcraft:book_of_shadows/the_book_of_shadows");
        unlock(player, "witchcraft:book_of_shadows/blood_binding");
        unlock(player, "witchcraft:book_of_shadows/knowledge");
        unlock(player, "witchcraft:path_of_night/signing_in_blood");
        unlock(player, "witchcraft:path_of_night/the_shadow_man");
        unlock(player, "witchcraft:path_of_night/the_witch");
        unlock(player, "witchcraft:path_of_night/magical_states");
        unlock(player, "witchcraft:path_of_night/familiars");
        unlock(player, "witchcraft:path_of_night/your_first_night");
    }

    private static void write(Player player, Collection<String> entries) {
        ListTag list = new ListTag();
        for (String id : entries) list.add(StringTag.valueOf(id));
        data(player).put(KNOWN, list);
    }
}
