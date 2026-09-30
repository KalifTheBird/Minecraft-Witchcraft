package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.util.StringRepresentable;

public enum ChalkType implements StringRepresentable {
    WHITE("white", 0xF4F1E8),
    BLACK("black", 0x18141A),
    INFERNAL("infernal", 0x3C1A23),
    ENDER("ender", 0x36125F),
    GOLD("gold", 0xFFD76A);

    private final String name;
    private final int tintColor;

    ChalkType(String name, int tintColor) {
        this.name = name;
        this.tintColor = tintColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getTintColor() {
        return tintColor;
    }
}
