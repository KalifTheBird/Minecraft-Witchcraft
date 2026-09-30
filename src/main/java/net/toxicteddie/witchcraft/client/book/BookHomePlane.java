package net.toxicteddie.witchcraft.client.book;

import com.mojang.blaze3d.vertex.PoseStack;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;


public final class BookHomePlane {
    private BookHomePlane() {}

    public static void apply(BookOfShadowsModel model, PoseStack pose) {
        Matrix4f parent = new Matrix4f(pose.last().pose());
        Matrix4f left = pageMatrix(model, parent, true);
        Matrix4f right = pageMatrix(model, parent, false);

        float y = BookPageLayout.HOME_VERTICAL;
        Vector3f l = point(left, BookPageLayout.WIDTH, y, BookPageLayout.INK_Z);
        Vector3f r = point(right, 0.0F, y, BookPageLayout.INK_Z);
        Vector3f center = new Vector3f(l).add(r).mul(0.5F);


        Vector3f x = axis(left, 1, 0, 0).add(axis(right, 1, 0, 0));
        Vector3f yAxis = axis(left, 0, 1, 0).add(axis(right, 0, 1, 0));
        if (x.lengthSquared() < 1.0E-10F) x.set(axis(left, 1, 0, 0));
        if (yAxis.lengthSquared() < 1.0E-10F) yAxis.set(axis(left, 0, 1, 0));
        x.normalize().mul(BookPageLayout.SCALE);
        yAxis.normalize().mul(BookPageLayout.SCALE);

        Vector3f normal = new Vector3f(x).cross(yAxis);
        if (normal.lengthSquared() < 1.0E-10F) normal.set(axis(left, 0, 0, 1));
        normal.normalize().mul(BookPageLayout.SCALE);

        yAxis.set(normal).cross(x).normalize().mul(BookPageLayout.SCALE);


        Vector3f normalUnit = new Vector3f(normal).normalize();
        Vector3f xUnit = new Vector3f(x).normalize();
        center.fma(BookPageLayout.HOME_DEPTH_PIXELS * BookPageLayout.SCALE, normalUnit);
        center.fma(BookPageLayout.HOME_SIDE_PIXELS * BookPageLayout.SCALE, xUnit);

        Matrix4f m = new Matrix4f().identity();
        m.m00(x.x);       m.m01(x.y);       m.m02(x.z);
        m.m10(yAxis.x);   m.m11(yAxis.y);   m.m12(yAxis.z);
        m.m20(normal.x);  m.m21(normal.y);  m.m22(normal.z);
        m.m30(center.x);  m.m31(center.y);  m.m32(center.z);
        pose.last().pose().set(m);
    }

    private static Matrix4f pageMatrix(BookOfShadowsModel model, Matrix4f parent, boolean leftPage) {
        PoseStack p = new PoseStack();
        p.last().pose().set(parent);
        model.translateToPage(p, leftPage);
        BookPageLayout.applyPlane(p, leftPage);
        return new Matrix4f(p.last().pose());
    }

    private static Vector3f point(Matrix4f m, float x, float y, float z) {
        Vector4f v = new Vector4f(x, y, z, 1.0F);
        m.transform(v);
        if (v.w != 0.0F && v.w != 1.0F) v.div(v.w);
        return new Vector3f(v.x, v.y, v.z);
    }

    private static Vector3f axis(Matrix4f m, float x, float y, float z) {
        Vector4f v = new Vector4f(x, y, z, 0.0F);
        m.transform(v);
        return new Vector3f(v.x, v.y, v.z);
    }
}
