package net.toxicteddie.witchcraft.ritual;

import net.minecraft.core.BlockPos;

public enum CornerDirection {
    EAST("east", 8, 0),
    SOUTH("south", 0, 8),
    WEST("west", -8, 0),
    NORTH("north", 0, -8);

    private final String key;
    private final int x;
    private final int z;

    CornerDirection(String key, int x, int z) {
        this.key = key;
        this.x = x;
        this.z = z;
    }

    public String key() { return key; }
    public BlockPos offset() { return new BlockPos(x, 0, z); }

    public static final CornerDirection[] CALL_ORDER = {EAST, SOUTH, WEST, NORTH};
    public static final CornerDirection[] UNCALL_ORDER = {NORTH, WEST, SOUTH, EAST};
}
