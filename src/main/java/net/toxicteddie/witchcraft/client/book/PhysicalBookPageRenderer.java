package net.toxicteddie.witchcraft.client.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import net.toxicteddie.witchcraft.book.BookContentLayout;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;


public final class PhysicalBookPageRenderer {
    private static final int MUTED_INK = 0xFF6B5139;
    private static final int ACCENT_INK = 0xFF59232B;
    private static final float TEXT_SCALE = 0.78F;
    private static final ResourceLocation BTN_ARROW = tex("arrow.png"),
            BTN_HOME = tex("home.png"), BTN_SEAL = tex("seal.png"), DIVIDER = tex("divider.png"), GLOW = tex("hover_glow.png");

    private PhysicalBookPageRenderer() {}

    public static void renderPage(BookOfShadowsModel model, PhysicalBookContent.Page page, boolean leftPage,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        model.translateToPage(poseStack, leftPage);
        BookPageLayout.applyPlane(poseStack, leftPage);
        drawPage(page, leftPage, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    public static void renderFlipPage(BookOfShadowsModel model, PhysicalBookContent.Page page, boolean fromRightPage,
                                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        model.translateToFlipPage(poseStack);
        BookPageLayout.applyPlane(poseStack, !fromRightPage);
        drawPage(page, !fromRightPage, poseStack, buffer, packedLight);
        poseStack.popPose();
    }


    public static void renderHome(BookOfShadowsModel model, PhysicalBookContent.Page left, PhysicalBookContent.Page right,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BookContentLayout.Button home = BookPageLayout.homeButton(left, right);
        if (home == null) return;
        BookPageLayout.ButtonRect rect = BookPageLayout.homeRect(home);
        poseStack.pushPose();
        BookHomePlane.apply(model, poseStack);


        if (!isHomeFrontFacing(poseStack)) {
            poseStack.popPose();
            return;
        }

        boolean hover = isHovered(rect, poseStack);
        if (hover) drawBookTexture(GLOW, rect.iconX()-3F, rect.iconY()-3F, rect.iconSize()+6F, rect.iconSize()+6F,
                255,235,190,220,poseStack,buffer,packedLight);


        drawHomeTexture(BTN_HOME, rect.iconX(), rect.iconY(), rect.iconSize(), rect.iconSize(),
                hover?255:235, hover?245:235, hover?210:235, 255, poseStack,buffer,packedLight);
        poseStack.popPose();
    }

    private static void drawPage(PhysicalBookContent.Page page, boolean leftPage, PoseStack pose,
                                 MultiBufferSource buffer, int light) {
        Font font = Minecraft.getInstance().font;
        int ink = page.inkColor();
        List<BookPageLayout.ButtonRect> controls = BookPageLayout.buttons(page, leftPage);
        boolean rows = controls.stream().anyMatch(BookPageLayout.ButtonRect::labelled);
        boolean rich = BookRichContentRenderer.hasHero(page);
        float y = 3F;

        if (!page.title().isBlank()) {
            List<FormattedCharSequence> title = font.split(Component.literal(page.title()), 88);
            for (int i = 0; i < Math.min(2, title.size()); i++) {
                drawCenteredSmall(font, title.get(i), y, ink, pose, buffer, light);
                y += 6.0F;
            }
            drawBookTexture(DIVIDER, 20F, y, 46F, 3F, 255, 255, 255, 230, pose, buffer, light);
            y += 6.0F;
        } else {
            y += 3F;
        }

        if (rich) {
            BookRichContentRenderer.renderHero(page, pose, buffer, light);
            y = Math.max(y, 45F);
        }

        boolean recipe = BookRichContentRenderer.renderRecipe(page, pose, buffer, light);
        if (recipe) y = Math.max(y, 61F);


        float bodyBottom = rows ? 39F : 75F;
        if (rich && !recipe && !rows) bodyBottom = 75F;

        if (!page.body().isBlank()) {
            for (String paragraph : page.body().split("\\n", -1)) {
                if (paragraph.isBlank()) {
                    y += 4F;
                    continue;
                }
                for (FormattedCharSequence line : font.split(Component.literal(paragraph), 78)) {
                    if (y + 6.0F > bodyBottom) break;
                    drawSmall(font, line, 4F, y, ink, pose, buffer, light);
                    y += 6.0F;
                }
                y += 2F;
                if (y + 6.0F > bodyBottom) break;
            }
        }

        for (BookPageLayout.ButtonRect rect : controls) {
            drawControl(rect, font, pose, buffer, light);
        }

        if (!page.footer().isBlank()) {
            drawCenteredSmall(font, Component.literal(page.footer()).getVisualOrderText(),
                    78F, MUTED_INK, pose, buffer, light);
        }
    }

    private static void drawControl(BookPageLayout.ButtonRect rect, Font font, PoseStack pose,
                                    MultiBufferSource buffer, int light) {
        BookContentLayout.Button b = rect.button();
        boolean isHome = b.kind() == BookContentLayout.ButtonKind.RETURN
                && b.label().toLowerCase().contains("index");

        ResourceLocation icon = (b.kind() == BookContentLayout.ButtonKind.CHAPTER
                || b.kind() == BookContentLayout.ButtonKind.ENTRY
                || b.kind() == BookContentLayout.ButtonKind.VEIL
                || (b.kind() == BookContentLayout.ButtonKind.RETURN && !isHome))
                ? BTN_SEAL
                : (isHome ? BTN_HOME : BTN_ARROW);

        boolean hover = isHovered(rect, pose);

        if (hover) {
            drawBookTexture(GLOW,
                    rect.iconX() - 3F, rect.iconY() - 3F,
                    rect.iconSize() + 6F, rect.iconSize() + 6F,
                    255, 235, 190, 220, pose, buffer, light);
        }

        boolean flipArrow = b.kind() == BookContentLayout.ButtonKind.BACK;
        drawBookTexture(icon,
                rect.iconX(), rect.iconY(), rect.iconSize(), rect.iconSize(),
                hover ? 255 : 235, hover ? 245 : 235, hover ? 210 : 235, 255,
                flipArrow, pose, buffer, light);

        if (rect.labelled()) {


            List<FormattedCharSequence> lines = font.split(Component.literal(b.label()), 82);
            float labelY = rect.y() + 0.8F;
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                drawSmall(font, lines.get(i), 15F, labelY + i * 5.5F,
                        hover ? 0xFF7B2F3A : ACCENT_INK, pose, buffer, light);
            }
        }
    }

    private static boolean isHovered(BookPageLayout.ButtonRect rect, PoseStack pose) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return false;

        Matrix4f inv = new Matrix4f(pose.last().pose());
        if (Math.abs(inv.determinant()) < 1e-8F) return false;
        inv.invert();

        Vec3 a = mc.player.getEyePosition(mc.getFrameTime());
        Vec3 b = a.add(mc.player.getViewVector(mc.getFrameTime()).scale(6));
        Vector4f p = tx(inv, a);
        Vector4f q = tx(inv, b);

        float dz = q.z() - p.z();
        if (Math.abs(dz) < 1e-7F) return false;

        float t = (BookPageLayout.INK_Z - p.z()) / dz;
        if (t < 0 || t > 1) return false;

        float x = p.x() + (q.x() - p.x()) * t;
        float y = p.y() + (q.y() - p.y()) * t;
        return rect.contains(x, y, 1.25F);
    }

    private static Vector4f tx(Matrix4f m, Vec3 p) {
        Vector4f v = new Vector4f((float) p.x, (float) p.y, (float) p.z, 1);
        m.transform(v);
        if (v.w() != 0 && v.w() != 1) v.div(v.w());
        return v;
    }

    private static boolean isHomeFrontFacing(PoseStack pose) {
        Matrix4f inverse = new Matrix4f(pose.last().pose());
        if (Math.abs(inverse.determinant()) < 1.0E-8F) return false;
        inverse.invert();


        Vector4f cameraLocal = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
        inverse.transform(cameraLocal);
        if (cameraLocal.w() != 0.0F && cameraLocal.w() != 1.0F) cameraLocal.div(cameraLocal.w());
        return cameraLocal.z() < BookPageLayout.INK_Z;
    }

    private static void drawHomeTexture(ResourceLocation texture, float x, float y, float w, float h,
                                        int r, int g, int b, int a, PoseStack pose,
                                        MultiBufferSource buffer, int light) {
        VertexConsumer vc = buffer.getBuffer(RenderType.text(texture));
        var m = pose.last().pose();
        float z = BookPageLayout.INK_Z - 0.02F;
        vc.vertex(m,x,y+h,z).color(r,g,b,a).uv(0,1).uv2(light).endVertex();
        vc.vertex(m,x+w,y+h,z).color(r,g,b,a).uv(1,1).uv2(light).endVertex();
        vc.vertex(m,x+w,y,z).color(r,g,b,a).uv(1,0).uv2(light).endVertex();
        vc.vertex(m,x,y,z).color(r,g,b,a).uv(0,0).uv2(light).endVertex();
    }

    private static void drawSmall(Font f, FormattedCharSequence text, float x, float y, int color,
                                  PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(TEXT_SCALE, TEXT_SCALE, 1);
        f.drawInBatch(text, 0, 0, color, false, pose.last().pose(), buffer,
                Font.DisplayMode.POLYGON_OFFSET, 0, light);
        pose.popPose();
    }

    private static void drawCenteredSmall(Font f, FormattedCharSequence text, float y, int color,
                                          PoseStack pose, MultiBufferSource buffer, int light) {
        float width = f.width(text) * TEXT_SCALE;
        drawSmall(f, text, (BookPageLayout.WIDTH - width) / 2F, y, color, pose, buffer, light);
    }

    private static ResourceLocation tex(String n) {
        return new ResourceLocation("witchcraft", "textures/gui/book/" + n);
    }

    public static void drawBookTexture(ResourceLocation texture, float x, float y, float w, float h,
                                       int r, int g, int b, int a, PoseStack pose,
                                       MultiBufferSource buffer, int light) {
        drawBookTexture(texture, x, y, w, h, r, g, b, a, false, pose, buffer, light);
    }

    public static void drawBookTexture(ResourceLocation texture, float x, float y, float w, float h,
                                       int r, int g, int b, int a, boolean flipX, PoseStack pose,
                                       MultiBufferSource buffer, int light) {
        VertexConsumer vc = buffer.getBuffer(RenderType.text(texture));
        var m = pose.last().pose();
        float z = BookPageLayout.INK_Z - 0.001F;
        float u0 = flipX ? 1F : 0F;
        float u1 = flipX ? 0F : 1F;

        vc.vertex(m, x, y + h, z).color(r, g, b, a).uv(u0, 1).uv2(light).endVertex();
        vc.vertex(m, x + w, y + h, z).color(r, g, b, a).uv(u1, 1).uv2(light).endVertex();
        vc.vertex(m, x + w, y, z).color(r, g, b, a).uv(u1, 0).uv2(light).endVertex();
        vc.vertex(m, x, y, z).color(r, g, b, a).uv(u0, 0).uv2(light).endVertex();
    }
}
