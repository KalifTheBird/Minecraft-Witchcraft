package net.toxicteddie.witchcraft.book;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;


public final class BookPageTurnTiming {
    // Grounded and floating books are tuned separately; change these values carefully.
    private BookPageTurnTiming() {}


    public static final float TURN_SECONDS = 0.72F;
    public static final int TURN_TICKS = 15;


    public static final float LANDING_PAGE_SETTLE = 0.92F;


    public static final float GROUNDED_FORWARD_RIGHT_REVEAL = 0.00F;
    public static final float GROUNDED_BACKWARD_RIGHT_REVEAL = 0.60F;


    public static final float FLOATING_FORWARD_RIGHT_CROSSOVER_Z = 0.95F;
    public static final float FLOATING_BACKWARD_RIGHT_CROSSOVER_Z = 0.70F;


    public static float progress(AnimationState animationState) {
        return Mth.clamp(animationState.getAccumulatedTime() / (TURN_SECONDS * 1000.0F), 0.0F, 1.0F);
    }
}
