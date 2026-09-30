package net.toxicteddie.witchcraft.power;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public final class PlayerElementSelectionManager {
    private static final String ROOT = "WitchcraftElementSelection";
    private static final String POWER_MODE = "PowerMode";
    private static final String SELECTED = "Selected";


    private static final String LEGACY_STASHED_STACK = "StashedStack";
    private static final String LEGACY_STASHED_SLOT = "StashedSlot";

    private PlayerElementSelectionManager() {}

    public static void applySelection(ServerPlayer player, boolean powerMode, int selectedIndex) {
        recoverLegacyStashedStack(player);

        CompoundTag tag = data(player);
        tag.putBoolean(POWER_MODE, powerMode);
        tag.putInt(SELECTED, Mth.clamp(selectedIndex, 0, ElementPower.values().length - 1));
    }


    public static void set(Player player, boolean powerMode, int selectedIndex) {
        if (player instanceof ServerPlayer serverPlayer) {
            applySelection(serverPlayer, powerMode, selectedIndex);
            return;
        }

        CompoundTag tag = data(player);
        tag.putBoolean(POWER_MODE, powerMode);
        tag.putInt(SELECTED, Mth.clamp(selectedIndex, 0, ElementPower.values().length - 1));
    }


    public static void recoverLegacyStashedStack(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (!tag.contains(LEGACY_STASHED_STACK, CompoundTag.TAG_COMPOUND)) {
            tag.remove(LEGACY_STASHED_SLOT);
            return;
        }

        ItemStack stored = ItemStack.of(tag.getCompound(LEGACY_STASHED_STACK));
        int slot = Mth.clamp(tag.getInt(LEGACY_STASHED_SLOT), 0, 8);

        if (!stored.isEmpty()) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current.isEmpty()) {
                player.getInventory().setItem(slot, stored);
            } else if (!player.getInventory().add(stored)) {
                player.drop(stored, false);
            }
        }

        tag.remove(LEGACY_STASHED_STACK);
        tag.remove(LEGACY_STASHED_SLOT);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }


    public static void reset(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            recoverLegacyStashedStack(serverPlayer);
        }

        CompoundTag tag = data(player);
        tag.putBoolean(POWER_MODE, false);
        tag.putInt(SELECTED, 0);
    }

    public static boolean isPowerMode(Player player) {
        return data(player).getBoolean(POWER_MODE);
    }

    public static int selectedIndex(Player player) {
        return Mth.clamp(data(player).getInt(SELECTED), 0, ElementPower.values().length - 1);
    }

    public static ElementPower selectedElement(Player player) {
        return ElementPower.values()[selectedIndex(player)];
    }

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, CompoundTag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }
}
