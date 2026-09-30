package net.toxicteddie.witchcraft.client.book;

import net.toxicteddie.witchcraft.book.BookPageTurnTiming;


public final class BookPageTurnLayout {
    private BookPageTurnLayout() {}

    public enum Presentation {
        GROUNDED,
        FLOATING
    }

    public record Spread(int left, int right) {}

    public static Spread visibleSpread(int newSpread, boolean turning, int direction,
                                       float progress, Presentation presentation) {
        return visibleSpread(newSpread, turning, direction, progress, presentation, Float.NaN);
    }


    public static Spread visibleSpread(int newSpread, boolean turning, int direction,
                                       float progress, Presentation presentation, float flipZRot) {
        int left = newSpread;
        int right = newSpread + 1;
        if (!turning) return new Spread(left, right);

        int oldSpread = Math.max(0, newSpread - direction * 2);

        if (direction > 0) {


            left = progress < BookPageTurnTiming.LANDING_PAGE_SETTLE ? oldSpread : newSpread;


            if (presentation == Presentation.FLOATING && !Float.isNaN(flipZRot)) {


                right = flipZRot > BookPageTurnTiming.FLOATING_FORWARD_RIGHT_CROSSOVER_Z
                        ? oldSpread + 1 : newSpread + 1;
            } else {

                right = progress < BookPageTurnTiming.GROUNDED_FORWARD_RIGHT_REVEAL
                        ? oldSpread + 1 : newSpread + 1;
            }
        } else {


            left = newSpread;


            if (presentation == Presentation.FLOATING && !Float.isNaN(flipZRot)) {


                right = flipZRot < BookPageTurnTiming.FLOATING_BACKWARD_RIGHT_CROSSOVER_Z
                        ? oldSpread + 1 : newSpread + 1;
            } else {

                right = progress < BookPageTurnTiming.GROUNDED_BACKWARD_RIGHT_REVEAL
                        ? oldSpread + 1 : newSpread + 1;
            }
        }

        return new Spread(left, right);
    }
}
