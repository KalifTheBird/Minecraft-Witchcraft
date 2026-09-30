package net.toxicteddie.witchcraft.client.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsAnimations;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import net.toxicteddie.witchcraft.block.custom.BookOfShadowsBlock;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;


public final class BookPageGeometry {
    public record PagePoint(boolean left, float x, float y, double distance) {}
    public record HomePoint(float x, float y, double distance) {}

    private BookPageGeometry() {}

    public static PagePoint intersect(BookOfShadowsBlockEntity book, Vec3 origin, Vec3 direction, double reach) {
        if (book.getLevel() == null || !book.isReaderActive() || !book.getPhase().open()) return null;
        float partial = Minecraft.getInstance().getFrameTime();
        BookOfShadowsModel model = freshModel();
        float age = book.getLevel().getGameTime() + partial;
        model.applyAnimation(book.animationState, animationFor(book.getPhase()), age, speedFor(book.getPhase()));
        applyBlockPageTurn(model, book, age);

        PoseStack root = new PoseStack();
        var pos = book.getBlockPos();
        root.translate(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
        Direction facing = book.getBlockState().getValue(BookOfShadowsBlock.FACING);
        root.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        root.scale(1.0F, -1.0F, -1.0F);
        return nearest(model, root, origin, direction, reach);
    }

    public static PagePoint intersect(BookOfShadowsFollowingEntity book, Vec3 origin, Vec3 direction, double reach) {
        if (!book.isReaderActive()) return null;
        book.ensureFloatingAnimationStarted();
        float partial = Minecraft.getInstance().getFrameTime();
        BookOfShadowsModel model = freshModel();
        float age = book.tickCount + partial;
        model.applyAnimation(book.floatingIdleAnimation, BookOfShadowsAnimations.BOOK_FLOAT_IDLE, age, 0.88F);
        if (book.isPageTurning()) {
            AnimationDefinition turn = book.getPageTurnDirection() > 0
                    ? BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_FORWARD
                    : BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_BACKWARD;
            model.applyOverlayAnimation(book.pageTurnAnimationState, turn, age, 1.0F);
        }

        double x = Mth.lerp(partial, book.xOld, book.getX());
        double y = Mth.lerp(partial, book.yOld, book.getY());
        double z = Mth.lerp(partial, book.zOld, book.getZ());
        float yaw = Mth.rotLerp(partial, book.yRotO, book.getYRot());
        PoseStack root = new PoseStack();
        root.translate(x, y + 1.5D, z);
        root.mulPose(Axis.YP.rotationDegrees(-yaw));
        root.scale(1.0F, -1.0F, -1.0F);
        return nearest(model, root, origin, direction, reach);
    }


    public static HomePoint intersectHome(BookOfShadowsBlockEntity book, Vec3 origin, Vec3 direction, double reach) {
        if (book.getLevel() == null || !book.isReaderActive() || !book.getPhase().open()) return null;
        float partial = Minecraft.getInstance().getFrameTime();
        BookOfShadowsModel model = freshModel();
        float age = book.getLevel().getGameTime() + partial;
        model.applyAnimation(book.animationState, animationFor(book.getPhase()), age, speedFor(book.getPhase()));
        applyBlockPageTurn(model, book, age);
        PoseStack root = new PoseStack();
        var pos = book.getBlockPos();
        root.translate(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
        Direction facing = book.getBlockState().getValue(BookOfShadowsBlock.FACING);
        root.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        root.scale(1.0F, -1.0F, -1.0F);
        return intersectHomePlane(model, root, origin, direction, reach);
    }

    public static HomePoint intersectHome(BookOfShadowsFollowingEntity book, Vec3 origin, Vec3 direction, double reach) {
        if (!book.isReaderActive()) return null;
        book.ensureFloatingAnimationStarted();
        float partial = Minecraft.getInstance().getFrameTime();
        BookOfShadowsModel model = freshModel();
        float age = book.tickCount + partial;
        model.applyAnimation(book.floatingIdleAnimation, BookOfShadowsAnimations.BOOK_FLOAT_IDLE, age, 0.88F);
        if (book.isPageTurning()) {
            AnimationDefinition turn = book.getPageTurnDirection() > 0
                    ? BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_FORWARD
                    : BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_BACKWARD;
            model.applyOverlayAnimation(book.pageTurnAnimationState, turn, age, 1.0F);
        }
        double x = Mth.lerp(partial, book.xOld, book.getX());
        double y = Mth.lerp(partial, book.yOld, book.getY());
        double z = Mth.lerp(partial, book.zOld, book.getZ());
        float yaw = Mth.rotLerp(partial, book.yRotO, book.getYRot());
        PoseStack root = new PoseStack();
        root.translate(x, y + 1.5D, z);
        root.mulPose(Axis.YP.rotationDegrees(-yaw));
        root.scale(1.0F, -1.0F, -1.0F);
        return intersectHomePlane(model, root, origin, direction, reach);
    }

    private static HomePoint intersectHomePlane(BookOfShadowsModel model, PoseStack root, Vec3 origin, Vec3 direction, double reach) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(root.last().pose());
        BookHomePlane.apply(model, pose);
        Matrix4f inverse = new Matrix4f(pose.last().pose());
        if (Math.abs(inverse.determinant()) < 1.0E-8F) return null;
        inverse.invert();
        Vec3 end = origin.add(direction.scale(reach));
        Vector4f a = transform(inverse, origin);
        Vector4f b = transform(inverse, end);
        float dz = b.z() - a.z();
        if (Math.abs(dz) < 1.0E-7F) return null;

        float t = (BookPageLayout.INK_Z - a.z()) / dz;
        if (t < 0.0F || t > 1.0F) return null;
        float x = Mth.lerp(t, a.x(), b.x());
        float y = Mth.lerp(t, a.y(), b.y());
        return new HomePoint(x, y, reach * t);
    }

    private static BookOfShadowsModel freshModel() {
        return new BookOfShadowsModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_LAYER));
    }

    private static PagePoint nearest(BookOfShadowsModel model, PoseStack root, Vec3 origin, Vec3 direction, double reach) {
        PagePoint left = intersectPage(model, root, true, origin, direction, reach);
        PagePoint right = intersectPage(model, root, false, origin, direction, reach);
        if (left == null) return right;
        if (right == null) return left;
        return left.distance <= right.distance ? left : right;
    }

    private static PagePoint intersectPage(BookOfShadowsModel model, PoseStack root, boolean left,
                                            Vec3 origin, Vec3 direction, double reach) {
        PoseStack pose = new PoseStack();
        pose.last().pose().set(root.last().pose());
        model.translateToPage(pose, left);
        BookPageLayout.applyPlane(pose, left);
        Matrix4f inverse = new Matrix4f(pose.last().pose());
        if (Math.abs(inverse.determinant()) < 1.0E-8F) return null;
        inverse.invert();

        Vec3 end = origin.add(direction.scale(reach));
        Vector4f a = transform(inverse, origin);
        Vector4f b = transform(inverse, end);
        float dz = b.z() - a.z();
        if (Math.abs(dz) < 1.0E-7F) return null;
        float t = (BookPageLayout.INK_Z - a.z()) / dz;
        if (t < 0.0F || t > 1.0F) return null;
        float x = Mth.lerp(t, a.x(), b.x());
        float y = Mth.lerp(t, a.y(), b.y());
        float overflow = 8.0F;
        if (x < -overflow || x > BookPageLayout.WIDTH + overflow
                || y < -overflow || y > BookPageLayout.HEIGHT + overflow) return null;
        return new PagePoint(left, x, y, reach * t);
    }

    private static Vector4f transform(Matrix4f matrix, Vec3 p) {
        Vector4f v = new Vector4f((float)p.x, (float)p.y, (float)p.z, 1.0F);
        matrix.transform(v);
        if (v.w() != 0.0F && v.w() != 1.0F) v.div(v.w());
        return v;
    }

    private static void applyBlockPageTurn(BookOfShadowsModel model, BookOfShadowsBlockEntity book, float age) {
        if (!book.isPageTurning()) return;
        boolean floating = book.getPhase() == BookOfShadowsBlockEntity.Phase.FLOAT_IDLE;
        AnimationDefinition flip = floating
                ? (book.getPageTurnDirection() > 0 ? BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_FORWARD
                                                   : BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_BACKWARD)
                : (book.getPageTurnDirection() > 0 ? BookOfShadowsAnimations.BOOK_PAGE_FLIP_BACKWARD
                                                   : BookOfShadowsAnimations.BOOK_PAGE_FLIP_FORWARD);
        model.applyOverlayAnimation(book.pageTurnAnimationState, flip, age, 1.0F);
    }

    private static float speedFor(BookOfShadowsBlockEntity.Phase phase) {
        return switch (phase) {
            case CLOSED_OPENING, OPEN_CLOSING, FLOAT_OPENING, FLOAT_CLOSING -> 20.0F / 22.0F;
            case FLOAT_IDLE -> 0.88F;
            default -> 1.0F;
        };
    }

    private static AnimationDefinition animationFor(BookOfShadowsBlockEntity.Phase phase) {
        return switch (phase) {
            case CLOSED_IDLE -> BookOfShadowsAnimations.BOOK_CLOSED_IDLE;
            case CLOSED_OPENING -> BookOfShadowsAnimations.BOOK_CLOSED_OPEN;
            case OPEN_IDLE -> BookOfShadowsAnimations.BOOK_OPEN_IDLE;
            case OPEN_CLOSING -> BookOfShadowsAnimations.BOOK_OPEN_CLOSE;
            case FLOAT_OPENING -> BookOfShadowsAnimations.BOOK_FLOAT_OPEN;
            case FLOAT_IDLE -> BookOfShadowsAnimations.BOOK_FLOAT_IDLE;
            case FLOAT_CLOSING -> BookOfShadowsAnimations.BOOK_FLOAT_CLOSE;
        };
    }
}
