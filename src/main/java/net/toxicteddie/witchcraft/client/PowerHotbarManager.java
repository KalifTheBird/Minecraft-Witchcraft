package net.toxicteddie.witchcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SelectElementPowerPacket;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.PlayerElementSelectionManager;


public final class PowerHotbarManager {
    public static final int FIRST_VANILLA_SLOT = 0;
    public static final int LAST_VANILLA_SLOT = 8;

    private static final ElementPower[] ORDER = {
            ElementPower.AIR,
            ElementPower.FIRE,
            ElementPower.WATER,
            ElementPower.EARTH
    };

    private static boolean powerMode;
    private static int selectedPowerSlot;

    private PowerHotbarManager() {}

    public static boolean isPowerMode() {
        return powerMode;
    }

    public static int getSelectedPowerSlot() {
        return Mth.clamp(selectedPowerSlot, 0, ORDER.length - 1);
    }

    public static ElementPower getSelectedElement() {
        return ORDER[getSelectedPowerSlot()];
    }


    public static boolean handleScroll(Minecraft minecraft, int direction) {
        if (minecraft.player == null || direction == 0) return false;

        if (!powerMode) {
            if (direction > 0
                    && minecraft.player.getInventory().selected == LAST_VANILLA_SLOT) {
                enterPowerMode();
                return true;
            }
            return false;
        }

        int selected = getSelectedPowerSlot();

        if (direction > 0) {

            selectPowerSlot((selected + 1) % ORDER.length);
        } else if (selected > 0) {
            selectPowerSlot(selected - 1);
        } else {

            exitPowerMode();
            minecraft.player.getInventory().selected = LAST_VANILLA_SLOT;
        }

        return true;
    }


    public static void handleVanillaHotbarKey() {
        exitPowerMode();
    }

    public static void enterPowerMode() {
        setState(true, 0);
    }

    public static void selectPowerSlot(int index) {
        setState(true, index);
    }

    public static void exitPowerMode() {
        setState(false, selectedPowerSlot);
    }

    public static void reset() {
        setState(false, 0);
    }

    private static void setState(boolean newPowerMode, int newSelectedPowerSlot) {
        int clampedSlot = Mth.clamp(newSelectedPowerSlot, 0, ORDER.length - 1);
        boolean changed = powerMode != newPowerMode
                || selectedPowerSlot != clampedSlot;

        powerMode = newPowerMode;
        selectedPowerSlot = clampedSlot;

        Minecraft minecraft = Minecraft.getInstance();


        if (minecraft.player != null) {
            PlayerElementSelectionManager.set(
                    minecraft.player,
                    powerMode,
                    selectedPowerSlot
            );
        }

        if (changed && minecraft.getConnection() != null) {
            ModNetworking.CHANNEL.sendToServer(
                    new SelectElementPowerPacket(powerMode, selectedPowerSlot)
            );
        }
    }
}
