package net.toxicteddie.witchcraft.path;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PathMessages {

    private PathMessages() {
    }

    public static void sendAltarDenied(Player player) {
        player.displayClientMessage(
                Component.literal(
                        "The altar remains still. "
                                + "Whatever sleeps within does not answer you."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    public static void sendCircleDenied(Player player) {
        player.displayClientMessage(
                Component.literal(
                        "The circle lies silent beneath your feet."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    public static void sendGenericMagicDenied(
            Player player
    ) {
        player.displayClientMessage(
                Component.literal(
                        "Nothing stirs. This magic does not know you."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    public static void sendAltarUnreadable(
            Player player
    ) {
        player.displayClientMessage(
                Component.literal(
                        "The trinkets form no pattern you recognize."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    public static void sendBareAltar(
            Player player
    ) {
        player.displayClientMessage(
                Component.literal(
                        "The bare altar offers no place for an offering."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    public static void sendWrongAltarSlot(
            Player player
    ) {
        player.displayClientMessage(
                Component.literal(
                        "That offering does not belong there."
                ).withStyle(ChatFormatting.GRAY),
                true
        );
    }
}
