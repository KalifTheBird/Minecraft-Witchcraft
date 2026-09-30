package net.toxicteddie.witchcraft.client.wand;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class WandTipTracker {
    private static Vec3 firstPersonTip;
    private static Vec3 thirdPersonTip;

    private WandTipTracker() {}

    public static void update(ItemDisplayContext context, Vec3 worldPosition) {
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            firstPersonTip = worldPosition;
        } else if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            thirdPersonTip = worldPosition;
        }
    }

    public static Vec3 get(boolean firstPerson) {
        return firstPerson ? firstPersonTip : thirdPersonTip;
    }

    public static void clear() {
        firstPersonTip = null;
        thirdPersonTip = null;
    }
}
