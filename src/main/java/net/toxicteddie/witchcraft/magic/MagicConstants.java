package net.toxicteddie.witchcraft.magic;

public final class MagicConstants {

    public static final int NATURAL_LIMIT = 100;


    public static final int CHARGED_START = 101;
    public static final int OVERCHARGED_START = 200;
    public static final int FESTERING_START = 350;
    public static final int SUPERCRITICAL_START = 600;


    public static final int ABSOLUTE_LIMIT = 1500;

    public static final int REGEN_INTERVAL_TICKS = 10;
    public static final int REGEN_AMOUNT = 1;


    public static final long MINECRAFT_DAY_TICKS = 24000L;
    public static final long PURGE_MAGICAL_EXHAUST_TICKS = 3L * MINECRAFT_DAY_TICKS;
    public static final long SUPERCRITICAL_DEATH_EXHAUST_TICKS = 7L * MINECRAFT_DAY_TICKS;

    private MagicConstants() {}
}
