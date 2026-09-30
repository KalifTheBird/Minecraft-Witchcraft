package net.toxicteddie.witchcraft.power;

import java.util.Locale;
import java.util.Optional;

public enum ElementPower {
    AIR("air"),
    FIRE("fire"),
    WATER("water"),
    EARTH("earth");

    private final String id;

    ElementPower(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<ElementPower> fromName(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "air", "wind" -> Optional.of(AIR);
            case "fire", "flame" -> Optional.of(FIRE);
            case "water" -> Optional.of(WATER);
            case "earth", "nature" -> Optional.of(EARTH);
            default -> Optional.empty();
        };
    }
}
