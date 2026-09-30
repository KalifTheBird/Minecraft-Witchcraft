package net.toxicteddie.witchcraft.book;

import net.toxicteddie.witchcraft.network.packet.OpenBookOfShadowsPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BookContentLayout {
    public enum ButtonKind { VEIL, CHAPTER, ENTRY, RETURN, BACK, FORWARD }
    public record Button(String label, int targetPage, ButtonKind kind) {}
    public record Page(String id, String type, String title, String body, String footer, int inkColor, List<Button> buttons) {}

    private record PendingButton(String label, String targetKey, ButtonKind kind) {}
    private record PendingPage(String id, String type, String title, String body, String footer, int inkColor, List<PendingButton> buttons) {}

    private static final int NORMAL_INK = 0xFF2B211C;
    private static final int BLOOD_INK = 0xFF781E27;

    private static final int BUTTONS_PER_INDEX_LEAF = 4;


    private static final int BODY_WIDTH_PIXELS = 78;
    private static final int BODY_LINES_WITH_TITLE = 9;
    private static final int BODY_LINES_NO_TITLE = 11;

    private BookContentLayout() {}

    public static List<Page> build(OpenBookOfShadowsPacket packet) {
        if (!packet.legible()) {
            String gibberish = "Vael thren ossera\nMorae vel nethra\nSeth ir vael corven\nThrenna mor aethel";
            List<Page> pages = new ArrayList<>();
            for (int i = 0; i < 128; i++) pages.add(new Page("", "gibberish", "", gibberish, "", NORMAL_INK, List.of()));
            return List.copyOf(pages);
        }

        List<PendingPage> pending = new ArrayList<>();
        Map<String, Integer> targets = new HashMap<>();


        targets.put("signature", 0);
        pending.add(page("", "", "", NORMAL_INK));
        pending.add(page("Book of Shadows",
                packet.signatureName().isBlank()
                        ? "This book belongs to:\n\n................................"
                        : "This book belongs to:\n\n................................\n" + packet.signatureName(),
                packet.signatureName().isBlank() ? "" : "Signed in blood", BLOOD_INK));


        targets.put("contents", pending.size());
        List<OpenBookOfShadowsPacket.ChapterData> chapters = packet.chapters();
        for (int leaf = 0; leaf < 2; leaf++) {
            List<PendingButton> buttons = new ArrayList<>();
            int start = leaf * BUTTONS_PER_INDEX_LEAF;
            for (int i = start; i < Math.min(start + BUTTONS_PER_INDEX_LEAF, chapters.size()); i++) {
                var chapter = chapters.get(i);
                buttons.add(button(chapter.title(), "chapter:" + chapter.id(), ButtonKind.CHAPTER));
            }
            pending.add(new PendingPage("", "index_nav", "Index", "",
                    leaf == 0 ? "What is learned shall be written." : "What is forgotten may yet be remembered.",
                    NORMAL_INK, List.copyOf(buttons)));
        }


        for (var chapter : chapters) {
            padSpread(pending);
            targets.put("chapter:" + chapter.id(), pending.size());

            List<OpenBookOfShadowsPacket.EntryData> entries = orderedEntries(packet.entries(), chapter.id());
            List<OpenBookOfShadowsPacket.EntryData> navEntries = subsectionEntries(entries, chapter.id());
            List<PendingButton> links = new ArrayList<>();
            for (var entry : navEntries) {
                links.add(button(subsectionLabel(entry, chapter.id()), "entry:" + entry.id(), ButtonKind.ENTRY));
            }

            pending.add(new PendingPage("", "chapter_nav", chapter.title(), "", "", NORMAL_INK, List.copyOf(links)));
            List<String> introLeaves = splitForLeaves(chapterIntroduction(chapter.id()), true);
            if (introLeaves.isEmpty()) introLeaves = List.of("");
            pending.add(new PendingPage("", "chapter_intro", chapter.title(), introLeaves.get(0), "", NORMAL_INK, List.of()));
            for (int intro = 1; intro < introLeaves.size(); intro++) {
                pending.add(new PendingPage("", "chapter_intro", "", introLeaves.get(intro), "", NORMAL_INK, List.of()));
            }


            for (var entry : entries) {
                targets.put("entry:" + entry.id(), pending.size());
                List<String> sourcePages = entry.pages().isEmpty()
                        ? List.of(entry.summary())
                        : entry.pages();
                StringBuilder complete = new StringBuilder();
                if (!entry.summary().isBlank()) complete.append(entry.summary());
                for (String source : sourcePages) {
                    if (source == null || source.isBlank() || source.equals(entry.summary())) continue;
                    if (!complete.isEmpty()) complete.append("\n\n");
                    complete.append(source);
                }

                List<String> leaves = splitForLeaves(complete.toString(), true);
                if (leaves.isEmpty()) leaves = List.of("");
                for (int i = 0; i < leaves.size(); i++) {
                    String title = i == 0 ? displayEntryTitle(entry, chapter.id()) : "";
                    pending.add(entryPage(entry.id(), entry.type(), title, leaves.get(i), prettifyType(entry.type()), NORMAL_INK));
                }
            }
            padSpread(pending);
        }

        List<Page> resolved = new ArrayList<>(pending.size());
        for (PendingPage page : pending) {
            List<Button> buttons = new ArrayList<>();
            for (PendingButton button : page.buttons()) {
                Integer target = targets.get(button.targetKey());
                if (target != null && target >= 0 && target < pending.size()) {
                    buttons.add(new Button(button.label(), target, button.kind()));
                }
            }
            int pageNumber = resolved.size() + 1;
            String roman = toRoman(pageNumber);
            String footer = page.footer().isBlank() ? roman : page.footer() + "  •  " + roman;
            resolved.add(new Page(page.id(), page.type(), page.title(), page.body(), footer, page.inkColor(), List.copyOf(buttons)));
        }

        if ((resolved.size() & 1) != 0) {
            int pageNumber = resolved.size() + 1;
            resolved.add(new Page("", "blank", "", "", toRoman(pageNumber), NORMAL_INK, List.of()));
        }


        List<Page> withNavigation = new ArrayList<>(resolved.size());
        for (int i = 0; i < resolved.size(); i++) {
            Page pg = resolved.get(i);
            List<Button> controls = new ArrayList<>(pg.buttons());
            int spread = i & ~1;

            boolean hasBack = controls.stream().anyMatch(b -> b.kind() == ButtonKind.BACK);
            boolean hasForward = controls.stream().anyMatch(b -> b.kind() == ButtonKind.FORWARD);

            if (spread > 0 && !hasBack)
                controls.add(new Button("Back", Math.max(0, spread - 2), ButtonKind.BACK));
            if (spread + 2 < resolved.size() && !hasForward)
                controls.add(new Button("Forward", spread + 2, ButtonKind.FORWARD));

            withNavigation.add(new Page(pg.id(), pg.type(), pg.title(), pg.body(), pg.footer(),
                    pg.inkColor(), List.copyOf(controls)));
        }
        return List.copyOf(withNavigation);
    }

    public static int buttonTarget(List<Page> pages, int pageIndex, int row) {
        if (pageIndex < 0 || pageIndex >= pages.size() || row < 0) return -1;
        List<Button> buttons = pages.get(pageIndex).buttons();
        return row < buttons.size() ? buttons.get(row).targetPage() : -1;
    }


    private static List<String> splitForLeaves(String text, boolean firstLeafHasTitle) {
        if (text == null || text.isBlank()) return List.of("");
        List<String> wrapped = wrapToLines(text, BODY_WIDTH_PIXELS);
        List<String> leaves = new ArrayList<>();
        int cursor = 0;
        boolean first = true;
        while (cursor < wrapped.size()) {
            int capacity = (first && firstLeafHasTitle) ? BODY_LINES_WITH_TITLE : BODY_LINES_NO_TITLE;
            int end = Math.min(wrapped.size(), cursor + capacity);
            leaves.add(String.join("\n", wrapped.subList(cursor, end)));
            cursor = end;
            first = false;
        }
        return List.copyOf(leaves);
    }

    private static List<String> wrapToLines(String text, int maxPixels) {
        List<String> out = new ArrayList<>();
        String normalized = text.replace("\r", "");
        for (String paragraph : normalized.split("\n", -1)) {
            if (paragraph.isBlank()) {
                if (!out.isEmpty() && !out.get(out.size() - 1).isEmpty()) out.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && textWidth(candidate) > maxPixels) {
                    out.add(line.toString());
                    line.setLength(0);
                }
                if (textWidth(word) > maxPixels) {

                    StringBuilder part = new StringBuilder();
                    for (int i = 0; i < word.length(); i++) {
                        char c = word.charAt(i);
                        if (!part.isEmpty() && textWidth(part.toString() + c) > maxPixels) {
                            out.add(part.toString());
                            part.setLength(0);
                        }
                        part.append(c);
                    }
                    line.append(part);
                } else {
                    if (!line.isEmpty()) line.append(' ');
                    line.append(word);
                }
            }
            if (!line.isEmpty()) out.add(line.toString());
        }
        return out;
    }


    private static int textWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') width += 4;
            else if ("ilI.,'!:;|".indexOf(c) >= 0) width += 2;
            else if ("ft()[]{}".indexOf(c) >= 0) width += 4;
            else if ("mwMW@#%&".indexOf(c) >= 0) width += 7;
            else width += 6;
        }
        return width;
    }

    private static List<OpenBookOfShadowsPacket.EntryData> orderedEntries(
            List<OpenBookOfShadowsPacket.EntryData> all, String chapter) {
        List<OpenBookOfShadowsPacket.EntryData> entries = new ArrayList<>(all.stream()
                .filter(e -> e.chapter().equals(chapter)).toList());
        List<String> preferred = switch (chapter) {
            case "book_of_shadows" -> List.of("the_book_of_shadows", "blood_binding", "knowledge");
            case "path_of_night" -> List.of("the_shadow_man", "the_witch", "magical_states", "familiars", "signing_in_blood", "your_first_night");
            case "witchcraft" -> List.of("body_magic/overview", "body_magic/body_magic", "energy_flow/overview",
                    "elemental_influence/overview", "circle_magic/overview", "circle_magic/first_circle", "circle_magic/four_corners",
                    "white_magic/overview", "white_magic/rite_of_night", "grey_magic/overview", "grey_magic/temperance",
                    "grey_magic/transmutation", "black_magic/overview", "black_magic/hollowing", "infernal_magic/overview",
                    "spellcraft/overview", "divination/overview");
            case "natural_world" -> List.of("herbs_and_flowers", "mandrake", "belladonna", "hemlock", "trees_and_woods", "elder", "rowan", "the_celestials");
            case "brews" -> List.of("tinctures", "dark_unction", "pale_moonlight", "milk_of_the_mother", "potions");
            default -> List.of();
        };
        entries.sort(java.util.Comparator.<OpenBookOfShadowsPacket.EntryData>comparingInt(e -> {
            String id = e.id();
            int slash = id.indexOf(':');
            String path = slash >= 0 ? id.substring(slash + 1) : id;
            String prefix = chapter + "/";
            if (path.startsWith(prefix)) path = path.substring(prefix.length());
            int idx = preferred.indexOf(path);
            return idx < 0 ? 1000 : idx;
        }).thenComparing(OpenBookOfShadowsPacket.EntryData::title));
        return List.copyOf(entries);
    }

    private static List<OpenBookOfShadowsPacket.EntryData> subsectionEntries(
            List<OpenBookOfShadowsPacket.EntryData> entries, String chapter) {
        List<String> wanted = switch (chapter) {
            case "book_of_shadows" -> List.of("The Book of Shadows", "Blood Binding", "Knowledge");
            case "path_of_night" -> List.of("The Shadow Man", "The Witch", "States of Magic", "Familiars");
            case "witchcraft" -> List.of("Body Magic", "Energy Flow", "Elemental Influence", "Circle Magic");
            case "natural_world" -> List.of("Herbs & Flowers", "Trees & Woods", "The Celestials");
            case "brews" -> List.of("Tinctures", "Potions");
            default -> List.of();
        };
        List<OpenBookOfShadowsPacket.EntryData> out = new ArrayList<>();
        for (String title : wanted) {
            entries.stream().filter(e -> e.title().equals(title)).findFirst().ifPresent(out::add);
        }


        if (out.isEmpty() && (chapter.equals("items") || chapter.equals("blocks") || chapter.equals("entities"))) {
            out.addAll(entries);
        }
        return List.copyOf(out);
    }

    private static String subsectionLabel(OpenBookOfShadowsPacket.EntryData entry, String chapter) {
        if (chapter.equals("book_of_shadows") && entry.title().equals("The Book of Shadows")) return "Book of Shadows Item";
        if (chapter.equals("witchcraft") && entry.title().equals("Body Magic")) return "The Body";
        if (chapter.equals("witchcraft") && entry.title().equals("Energy Flow")) return "The Flow of Energy";
        if (chapter.equals("witchcraft") && entry.title().equals("Elemental Influence")) return "The Influence of Elements";
        return entry.title();
    }

    private static String displayEntryTitle(OpenBookOfShadowsPacket.EntryData entry, String chapter) {
        return subsectionLabel(entry, chapter);
    }

    private static String chapterIntroduction(String chapter) {
        return switch (chapter) {
            case "book_of_shadows" -> "Every witch keeps a record of their craft. Within these pages lie the knowledge you have been given, the things you have discovered, and the workings you have made your own. What is learned shall be written. What is forgotten may yet be remembered.";
            case "path_of_night" -> "The Path of Night begins with a choice. To walk it is to become something other than what you were, and to learn that every gift of magic leaves its mark upon the witch who carries it.";
            case "witchcraft" -> "Witchcraft is not one act, but a practice: power held in the body, energy set into motion, influence extended into the elements, and intention given shape through the circle.";
            case "natural_world" -> "Magic does not begin with the witch. It grows in root and branch, follows the heavens, and sleeps within the living world. A careful witch learns first to observe, then to gather.";
            case "brews" -> "Some workings begin long before the spell is cast. Herbs are steeped, essences drawn out, and ingredients prepared until their hidden virtues may be put to use.";
            case "items" -> "A witch's tools are more than possessions. Some focus a working, some preserve it, and some remember the hands and blood that have claimed them.";
            case "blocks" -> "The craft leaves its mark upon a witch's surroundings. Altars, cauldrons, ovens, and other workings turn ordinary space into a place where magic may be gathered and shaped.";
            case "entities" -> "Not every being touched by magic is human, and not every presence belongs wholly to this world. Learn their names, their natures, and the caution each deserves.";
            default -> "";
        };
    }

    private static String prettifyType(String type) {
        if (type == null || type.isBlank()) return "Working";
        String[] words = type.replace('_', ' ').split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static String toRoman(int number) {
        if (number <= 0) return "";
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                out.append(numerals[i]);
                number -= values[i];
            }
        }
        return out.toString();
    }

    private static void padSpread(List<PendingPage> pages) {
        if ((pages.size() & 1) != 0) pages.add(page("", "", "", NORMAL_INK));
    }

    private static PendingPage page(String title, String body, String footer, int ink, PendingButton... buttons) {
        return new PendingPage("", "text", title, body, footer, ink, List.of(buttons));
    }

    private static PendingPage page(String title, String body, String footer, int ink, List<PendingButton> buttons) {
        return new PendingPage("", "text", title, body, footer, ink, List.copyOf(buttons));
    }

    private static PendingPage entryPage(String id, String type, String title, String body, String footer,
                                         int ink, PendingButton... buttons) {
        return new PendingPage(id, type, title, body, footer, ink, List.of(buttons));
    }

    private static PendingPage entryPage(String id, String type, String title, String body, String footer,
                                         int ink, List<PendingButton> buttons) {
        return new PendingPage(id, type, title, body, footer, ink, List.copyOf(buttons));
    }

    private static PendingButton button(String label, String target, ButtonKind kind) {
        return new PendingButton(label, target, kind);
    }
}
