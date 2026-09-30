package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum ChalkGlyph implements StringRepresentable {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    HEART("heart");

    private static final ChalkGlyph[] REGULAR_GLYPHS = {
            ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE
    };

    private final String name;

    ChalkGlyph(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static ChalkGlyph randomRegular(RandomSource random) {
        return REGULAR_GLYPHS[random.nextInt(REGULAR_GLYPHS.length)];
    }
}
