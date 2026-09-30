package net.toxicteddie.witchcraft.client.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.toxicteddie.witchcraft.book.BookContentLayout;

import java.util.ArrayList;
import java.util.List;


public final class BookPageLayout {
    public static final float WIDTH = 86.0F;
    public static final float HEIGHT = 92.0F;
    public static final float SCALE = 0.00515F;
    public static final float INK_Z = -0.003F;
    private static final float LEFT_INK = 0.020F;
    private static final float RIGHT_INK = -0.020F;


    public static final float NAV_SIZE = 12.0F;
    public static final float BACK_X = -2.0F;
    public static final float BACK_Y = 82.0F;
    public static final float FORWARD_X = 76.0F;
    public static final float FORWARD_Y = 82.0F;


    // Home uses its own plane so it stays centered over the book binding.
    public static final float HOME_VERTICAL = 82.0F;
    // Keep this small: it only lifts the icon enough to avoid clipping into the pages.
    public static final float HOME_DEPTH_PIXELS = 0.5F;
    public static final float HOME_SIDE_PIXELS = 0.0F;

    public static final float HOME_X = -NAV_SIZE * 0.5F;
    public static final float HOME_Y = -NAV_SIZE * 0.5F;

    public record ButtonRect(BookContentLayout.Button button, float x, float y, float w, float h,
                             float iconX, float iconY, float iconSize, boolean labelled) {
        public boolean contains(float px, float py, float tolerance) {
            return px >= x - tolerance && px <= x + w + tolerance
                    && py >= y - tolerance && py <= y + h + tolerance;
        }
    }

    private BookPageLayout() {}

    public static void applyPlane(PoseStack pose, boolean left) {
        if (left) {
            pose.translate(0.50F / 16.0F, LEFT_INK / 16.0F, -3.60F / 16.0F);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.translate(WIDTH * SCALE, HEIGHT * SCALE, 0.0F);
            pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        } else {
            pose.translate(0.75F / 16.0F, RIGHT_INK / 16.0F, 4.00F / 16.0F);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
        pose.scale(SCALE, SCALE, SCALE);
    }


    public static List<ButtonRect> buttons(PhysicalBookContent.Page page, boolean leftPage) {
        List<ButtonRect> out = new ArrayList<>();
        int row = 0;
        for (BookContentLayout.Button button : page.buttons()) {
            switch (button.kind()) {
                case CHAPTER, ENTRY, VEIL -> {


                    float y = 19.0F + row++ * 14.0F;
                    out.add(new ButtonRect(button, 5.0F, y, 76.0F, 13.0F, 6.0F, y + 1.0F, 7.0F, true));
                }
                case BACK -> { if (leftPage) out.add(nav(button, BACK_X, BACK_Y)); }
                case FORWARD -> { if (!leftPage) out.add(nav(button, FORWARD_X, FORWARD_Y)); }
                case RETURN -> {
                    if (!isHome(button)) {
                        float y = 19.0F + row++ * 14.0F;
                        out.add(new ButtonRect(button, 5.0F, y, 76.0F, 13.0F, 6.0F, y + 1.0F, 7.0F, true));
                    }
                }
            }
        }
        return List.copyOf(out);
    }

    public static BookContentLayout.Button homeButton(PhysicalBookContent.Page left, PhysicalBookContent.Page right) {


        BookContentLayout.Button found = findHome(left);
        if (found == null) found = findHome(right);
        return found != null ? found
                : new BookContentLayout.Button("Seek the Index", 2, BookContentLayout.ButtonKind.RETURN);
    }

    public static ButtonRect homeRect(BookContentLayout.Button button) {
        return button == null ? null : nav(button, HOME_X, HOME_Y);
    }

    private static BookContentLayout.Button findHome(PhysicalBookContent.Page page) {
        if (page == null) return null;
        for (BookContentLayout.Button b : page.buttons()) if (isHome(b)) return b;
        return null;
    }

    private static boolean isHome(BookContentLayout.Button b) {
        return b.kind() == BookContentLayout.ButtonKind.RETURN
                && b.label().toLowerCase().contains("index");
    }

    private static ButtonRect nav(BookContentLayout.Button button, float x, float y) {
        return new ButtonRect(button, x, y, NAV_SIZE, NAV_SIZE, x, y, NAV_SIZE, false);
    }
}
