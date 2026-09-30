package net.toxicteddie.witchcraft.block.custom;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum WitchesAltarPart implements StringRepresentable {
    CENTER("center"),
    LEFT("left"),
    RIGHT("right");

    private final String serializedName;

    WitchesAltarPart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.serializedName;
    }
}
