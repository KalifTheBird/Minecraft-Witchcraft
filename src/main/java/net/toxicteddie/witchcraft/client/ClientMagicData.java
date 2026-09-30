package net.toxicteddie.witchcraft.client;

import net.minecraft.util.Mth;
import net.toxicteddie.witchcraft.magic.MagicConstants;
import net.toxicteddie.witchcraft.magic.MagicState;

public final class ClientMagicData {
    private static int magic;
    private static boolean witch;

    private ClientMagicData() {}

    public static int getMagic() { return magic; }
    public static boolean isWitch() { return witch; }

    public static void update(int amount, boolean isWitch) {
        magic = Mth.clamp(amount, 0, MagicConstants.ABSOLUTE_LIMIT);
        witch = isWitch;
    }

    public static MagicState getState() { return MagicState.fromAmount(magic); }
}
