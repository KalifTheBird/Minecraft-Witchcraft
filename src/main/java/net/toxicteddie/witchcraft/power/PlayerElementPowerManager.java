package net.toxicteddie.witchcraft.power;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.SyncElementPowerPacket;

import java.util.EnumMap;
import java.util.Map;

public final class PlayerElementPowerManager {
    private static final String ROOT_TAG = "witchcraft_element_power";

    private PlayerElementPowerManager() {}

    private static CompoundTag persisted(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) {
            persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return persistent.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static CompoundTag powerTag(Player player) {
        CompoundTag persisted = persisted(player);
        if (!persisted.contains(ROOT_TAG)) {
            persisted.put(ROOT_TAG, new CompoundTag());
        }
        return persisted.getCompound(ROOT_TAG);
    }

    public static int get(Player player, ElementPower element) {
        return Mth.clamp(powerTag(player).getInt(element.id()), 0, ElementPowerConstants.MAX_POWER);
    }

    public static Map<ElementPower, Integer> getAll(Player player) {
        EnumMap<ElementPower, Integer> result = new EnumMap<>(ElementPower.class);
        for (ElementPower element : ElementPower.values()) {
            result.put(element, get(player, element));
        }
        return result;
    }

    public static void set(Player player, ElementPower element, int amount) {
        powerTag(player).putInt(element.id(), Mth.clamp(amount, 0, ElementPowerConstants.MAX_POWER));
        sync(player);
    }

    public static int add(Player player, ElementPower element, int amount) {
        if (amount <= 0) return 0;
        int before = get(player, element);
        set(player, element, before + amount);
        return get(player, element) - before;
    }

    public static int remove(Player player, ElementPower element, int amount) {
        if (amount <= 0) return 0;
        int before = get(player, element);
        set(player, element, before - amount);
        return before - get(player, element);
    }


    public static boolean trySpend(Player player, ElementPower element, int amount) {
        if (amount <= 0) return true;
        if (get(player, element) < amount) return false;
        remove(player, element, amount);
        return true;
    }

    public static void fill(Player player) {
        CompoundTag tag = powerTag(player);
        for (ElementPower element : ElementPower.values()) {
            tag.putInt(element.id(), ElementPowerConstants.MAX_POWER);
        }
        sync(player);
    }

    public static void reset(Player player) {
        CompoundTag tag = powerTag(player);
        for (ElementPower element : ElementPower.values()) {
            tag.putInt(element.id(), 0);
        }
        sync(player);
    }

    public static void regenerateNaturally(Player player) {
        CompoundTag tag = powerTag(player);
        boolean changed = false;
        for (ElementPower element : ElementPower.values()) {
            int current = Mth.clamp(tag.getInt(element.id()), 0, ElementPowerConstants.MAX_POWER);
            if (current < ElementPowerConstants.MAX_POWER) {
                tag.putInt(element.id(), Math.min(ElementPowerConstants.MAX_POWER,
                        current + ElementPowerConstants.REGEN_AMOUNT));
                changed = true;
            }
        }
        if (changed) sync(player);
    }

    public static void copyFrom(Player oldPlayer, Player newPlayer) {
        CompoundTag oldRoot = persisted(oldPlayer);
        if (oldRoot.contains(ROOT_TAG)) {
            persisted(newPlayer).put(ROOT_TAG, oldRoot.getCompound(ROOT_TAG).copy());
        }
        sync(newPlayer);
    }

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new SyncElementPowerPacket(
                    get(player, ElementPower.AIR),
                    get(player, ElementPower.FIRE),
                    get(player, ElementPower.WATER),
                    get(player, ElementPower.EARTH)
            ), serverPlayer);
        }
    }
}
