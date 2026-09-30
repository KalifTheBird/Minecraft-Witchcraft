package net.toxicteddie.witchcraft.magic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SyncMagicPacket;
import net.toxicteddie.witchcraft.path.PlayerPathManager;

public final class PlayerMagicManager {
    private static final String MAGIC_TAG = "WitchcraftMagic";
    private static final String MAGICAL_EXHAUST_UNTIL_TAG = "WitchcraftMagicalExhaustUntil";

    private PlayerMagicManager() {}

    private static CompoundTag getPersistedData(Player player) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains(Player.PERSISTED_NBT_TAG, CompoundTag.TAG_COMPOUND)) {
            playerData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return playerData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static int getMagic(Player player) {
        return Mth.clamp(getPersistedData(player).getInt(MAGIC_TAG), 0, MagicConstants.ABSOLUTE_LIMIT);
    }

    public static void setMagic(Player player, int amount) {
        int clamped = Mth.clamp(amount, 0, MagicConstants.ABSOLUTE_LIMIT);
        getPersistedData(player).putInt(MAGIC_TAG, clamped);
        sync(player);
    }

    public static int addMagic(Player player, int amount) {
        if (amount <= 0 || !PlayerPathManager.isWitch(player)) return 0;
        int before = getMagic(player);
        setMagic(player, before + amount);
        return getMagic(player) - before;
    }

    public static int removeMagic(Player player, int amount) {
        if (amount <= 0) return 0;
        int before = getMagic(player);
        setMagic(player, before - amount);
        return before - getMagic(player);
    }


    public static boolean trySpendMagic(Player player, int cost) {
        if (cost <= 0) return true;
        if (!PlayerPathManager.isWitch(player)) return false;

        int removed = removeMagic(player, cost);
        return removed >= cost;
    }

    public static void regenerateNaturally(Player player) {
        if (!PlayerPathManager.isWitch(player)) return;
        if (isMagicallyExhausted(player)) {
            if (getMagic(player) != 0) setMagic(player, 0);
            return;
        }
        int current = getMagic(player);
        if (current < MagicConstants.NATURAL_LIMIT) {
            setMagic(player, Math.min(MagicConstants.NATURAL_LIMIT, current + MagicConstants.REGEN_AMOUNT));
        }
    }


    public static boolean isMagicallyExhausted(Player player) {
        long until = getPersistedData(player).getLong(MAGICAL_EXHAUST_UNTIL_TAG);
        if (until <= 0L) return false;

        long now = currentWorldDayTime(player);
        if (now >= until) {
            getPersistedData(player).remove(MAGICAL_EXHAUST_UNTIL_TAG);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Your connection to magic begins to return."),
                        true
                );
            }
            return false;
        }
        return true;
    }

    public static long getMagicalExhaustTicksRemaining(Player player) {
        long until = getPersistedData(player).getLong(MAGICAL_EXHAUST_UNTIL_TAG);
        return Math.max(0L, until - currentWorldDayTime(player));
    }


    public static void clearMagicalExhaust(Player player) {
        getPersistedData(player).remove(MAGICAL_EXHAUST_UNTIL_TAG);
        sync(player);
    }

    public static void beginMagicalExhaust(Player player, long durationTicks) {
        long duration = Math.max(0L, durationTicks);
        getPersistedData(player).putLong(
                MAGICAL_EXHAUST_UNTIL_TAG,
                currentWorldDayTime(player) + duration
        );
        setMagic(player, 0);
    }


    public static void beginPurgeExhaust(Player player) {
        beginMagicalExhaust(player, MagicConstants.PURGE_MAGICAL_EXHAUST_TICKS);
    }


    public static void beginSupercriticalDeathExhaust(Player player) {
        beginMagicalExhaust(player, MagicConstants.SUPERCRITICAL_DEATH_EXHAUST_TICKS);
    }

    private static long currentWorldDayTime(Player player) {
        if (player.getServer() != null) {
            return player.getServer().overworld().getDayTime();
        }
        return player.level().getDayTime();
    }

    public static MagicState getState(Player player) {
        return MagicState.fromAmount(getMagic(player));
    }

    public static boolean isOvercharged(Player player) {
        return getMagic(player) >= MagicConstants.OVERCHARGED_START;
    }

    public static boolean isExhausted(Player player) {
        return getMagic(player) < MagicConstants.NATURAL_LIMIT;
    }

    public static boolean isFestering(Player player) {
        return getMagic(player) >= MagicConstants.FESTERING_START;
    }

    public static boolean isSupercritical(Player player) {
        return getMagic(player) >= MagicConstants.SUPERCRITICAL_START;
    }

    public static void clearMagic(Player player) {
        setMagic(player, 0);
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new SyncMagicPacket(getMagic(player), PlayerPathManager.isWitch(player)), serverPlayer);
        }
    }
}
