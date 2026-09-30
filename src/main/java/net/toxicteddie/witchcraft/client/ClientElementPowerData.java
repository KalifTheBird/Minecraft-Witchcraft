package net.toxicteddie.witchcraft.client;

import net.minecraft.util.Mth;
import net.toxicteddie.witchcraft.power.ElementPower;
import net.toxicteddie.witchcraft.power.ElementPowerConstants;

import java.util.EnumMap;

public final class ClientElementPowerData {
    private static final EnumMap<ElementPower, Integer> POWER = new EnumMap<>(ElementPower.class);

    static {
        for (ElementPower element : ElementPower.values()) POWER.put(element, 0);
    }

    private ClientElementPowerData() {}

    public static int get(ElementPower element) {
        return POWER.getOrDefault(element, 0);
    }

    public static float percent(ElementPower element) {
        return get(element) / (float) ElementPowerConstants.MAX_POWER;
    }

    public static void update(int air, int fire, int water, int earth) {
        POWER.put(ElementPower.AIR, clamp(air));
        POWER.put(ElementPower.FIRE, clamp(fire));
        POWER.put(ElementPower.WATER, clamp(water));
        POWER.put(ElementPower.EARTH, clamp(earth));
    }

    private static int clamp(int value) {
        return Mth.clamp(value, 0, ElementPowerConstants.MAX_POWER);
    }
}
