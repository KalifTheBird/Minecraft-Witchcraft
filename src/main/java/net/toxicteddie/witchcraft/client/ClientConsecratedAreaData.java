package net.toxicteddie.witchcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;


public final class ClientConsecratedAreaData {
    private static boolean inside;

    private ClientConsecratedAreaData() {
    }

    public static boolean isInside() {
        return inside;
    }


    public static void setInside(boolean value) {
        boolean enteredConsecratedArea = value && !inside;
        inside = value;

        if (!enteredConsecratedArea || !ClientMagicData.isWitch()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }


        minecraft.player.playSound(
                SoundEvents.AMETHYST_BLOCK_CHIME,
                0.55F,
                1.55F
        );
    }

    public static void clear() {
        inside = false;
    }
}
