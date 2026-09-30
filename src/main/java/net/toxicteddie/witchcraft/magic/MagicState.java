package net.toxicteddie.witchcraft.magic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;


public enum MagicState {
    EXHAUSTED("Exhausted", ChatFormatting.GRAY),
    RESTING("Resting", ChatFormatting.DARK_PURPLE),
    CHARGED("Charged", ChatFormatting.LIGHT_PURPLE),
    OVERCHARGED("Overcharged", ChatFormatting.GOLD),
    FESTERING("Festering", ChatFormatting.DARK_RED),
    SUPERCRITICAL("Supercritical", ChatFormatting.RED);

    private final String displayName;
    private final ChatFormatting color;

    MagicState(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public Component getDisplayComponent() {
        return Component.literal(displayName).withStyle(color);
    }

    public static MagicState fromAmount(int amount) {
        if (amount < MagicConstants.NATURAL_LIMIT) {
            return EXHAUSTED;
        }
        if (amount == MagicConstants.NATURAL_LIMIT) {
            return RESTING;
        }
        if (amount < MagicConstants.OVERCHARGED_START) {
            return CHARGED;
        }
        if (amount < MagicConstants.FESTERING_START) {
            return OVERCHARGED;
        }
        if (amount < MagicConstants.SUPERCRITICAL_START) {
            return FESTERING;
        }
        return SUPERCRITICAL;
    }
}
