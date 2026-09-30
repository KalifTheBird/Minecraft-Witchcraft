package net.toxicteddie.witchcraft.path;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum PlayerPath {

    LIGHT(
            "light",
            "Path of Light",
            "Human",
            ChatFormatting.GOLD
    ),

    NIGHT(
            "night",
            "Path of Night",
            "Witch",
            ChatFormatting.DARK_PURPLE
    ),

    BLOOD(
            "blood",
            "Path of Blood",
            "Vampire",
            ChatFormatting.DARK_RED
    ),

    WILD(
            "wild",
            "Path of the Wild",
            "Werewolf",
            ChatFormatting.DARK_GREEN
    );

    private final String serializedName;
    private final String displayName;
    private final String followerName;
    private final ChatFormatting color;

    PlayerPath(
            String serializedName,
            String displayName,
            String followerName,
            ChatFormatting color
    ) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.followerName = followerName;
        this.color = color;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFollowerName() {
        return this.followerName;
    }

    public ChatFormatting getColor() {
        return this.color;
    }

    public Component getDisplayComponent() {
        return Component.literal(this.displayName)
                .withStyle(this.color);
    }

    public Component getFollowerComponent() {
        return Component.literal(this.followerName)
                .withStyle(this.color);
    }

    public static PlayerPath fromName(String name) {
        if (name == null || name.isBlank()) {
            return LIGHT;
        }

        String normalized =
                name.toLowerCase(Locale.ROOT);

        for (PlayerPath path : values()) {
            if (path.serializedName.equals(normalized)) {
                return path;
            }
        }

        return LIGHT;
    }
}
