package net.toxicteddie.witchcraft.path;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerPathManager {

    private static final String PATH_TAG =
            "WitchcraftPlayerPath";

    private PlayerPathManager() {
    }


    private static CompoundTag getPersistedData(
            Player player
    ) {
        CompoundTag playerData =
                player.getPersistentData();

        if (!playerData.contains(
                Player.PERSISTED_NBT_TAG,
                CompoundTag.TAG_COMPOUND
        )) {
            playerData.put(
                    Player.PERSISTED_NBT_TAG,
                    new CompoundTag()
            );
        }

        return playerData.getCompound(
                Player.PERSISTED_NBT_TAG
        );
    }

    public static PlayerPath getPath(Player player) {
        CompoundTag persistedData =
                getPersistedData(player);

        if (!persistedData.contains(PATH_TAG)) {
            return PlayerPath.LIGHT;
        }

        return PlayerPath.fromName(
                persistedData.getString(PATH_TAG)
        );
    }

    public static void setPath(
            Player player,
            PlayerPath path
    ) {
        if (path == null) {
            path = PlayerPath.LIGHT;
        }

        getPersistedData(player).putString(
                PATH_TAG,
                path.getSerializedName()
        );
    }


    public static void clearPath(Player player) {
        setPath(
                player,
                PlayerPath.LIGHT
        );
    }

    public static boolean isPath(
            Player player,
            PlayerPath path
    ) {
        return getPath(player) == path;
    }

    public static boolean isHuman(Player player) {
        return isPath(
                player,
                PlayerPath.LIGHT
        );
    }

    public static boolean isWitch(Player player) {
        return isPath(
                player,
                PlayerPath.NIGHT
        );
    }

    public static boolean isVampire(Player player) {
        return isPath(
                player,
                PlayerPath.BLOOD
        );
    }

    public static boolean isWerewolf(Player player) {
        return isPath(
                player,
                PlayerPath.WILD
        );
    }

    public static boolean canUseAltarMagic(
            Player player
    ) {
        return isWitch(player);
    }

    public static boolean canUseCircleMagic(
            Player player
    ) {
        return isWitch(player);
    }
}
