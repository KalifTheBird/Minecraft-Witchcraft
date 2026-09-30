package net.toxicteddie.witchcraft.client.book;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.book.BookContentLayout;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;
import net.toxicteddie.witchcraft.network.ModNetworking;
import net.toxicteddie.witchcraft.network.packet.BookButtonClickPacket;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BookButtonInputHandler {
    private static final double REACH = 6.0D;
    private static final float BUTTON_TOLERANCE = 1.25F;

    private BookButtonInputHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouse(InputEvent.MouseButton event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT || event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        Vec3 origin = mc.player.getEyePosition(1.0F);
        Vec3 direction = mc.player.getViewVector(1.0F).normalize();
        Pick best = null;


        BlockPos eye = BlockPos.containing(origin);
        int radius = (int)Math.ceil(REACH) + 1;
        for (BlockPos pos : BlockPos.betweenClosed(eye.offset(-radius, -radius, -radius),
                                                   eye.offset(radius, radius, radius))) {
            if (!(mc.level.getBlockEntity(pos) instanceof BookOfShadowsBlockEntity book)) continue;
            if (book.isPageTurning()) continue;


            BookPageGeometry.HomePoint homePoint = BookPageGeometry.intersectHome(book, origin, direction, REACH);
            if (homePoint != null) {
                PhysicalBookContent.Page leftPage = PhysicalBookContent.page(book.getBlockPos(), book.getReaderPage());
                PhysicalBookContent.Page rightPage = PhysicalBookContent.page(book.getBlockPos(), book.getReaderPage() + 1);
                int homeTarget = homeTargetAt(leftPage, rightPage, homePoint.x(), homePoint.y());
                if (homeTarget >= 0 && (best == null || homePoint.distance() < best.distance)) {
                    best = Pick.block(book.getBlockPos(), homeTarget, homePoint.distance());
                }
            }

            BookPageGeometry.PagePoint point = BookPageGeometry.intersect(book, origin, direction, REACH);
            if (point == null) continue;
            int pageIndex = book.getReaderPage() + (point.left() ? 0 : 1);
            PhysicalBookContent.Page page = PhysicalBookContent.page(book.getBlockPos(), pageIndex);
            int target = targetAt(page, point.left(), point.x(), point.y());
            if (target >= 0 && (best == null || point.distance() < best.distance)) {
                best = Pick.block(book.getBlockPos(), target, point.distance());
            }
        }


        AABB area = mc.player.getBoundingBox().inflate(REACH + 1.0D);
        for (BookOfShadowsFollowingEntity book : mc.level.getEntitiesOfClass(BookOfShadowsFollowingEntity.class, area)) {
            if (book.isPageTurning()) continue;

            BookPageGeometry.HomePoint homePoint = BookPageGeometry.intersectHome(book, origin, direction, REACH);
            if (homePoint != null) {
                PhysicalBookContent.Page leftPage = PhysicalBookContent.page(book.getContentSourcePos(), book.getReaderPage());
                PhysicalBookContent.Page rightPage = PhysicalBookContent.page(book.getContentSourcePos(), book.getReaderPage() + 1);
                int homeTarget = homeTargetAt(leftPage, rightPage, homePoint.x(), homePoint.y());
                if (homeTarget >= 0 && (best == null || homePoint.distance() < best.distance)) {
                    best = Pick.entity(book.getId(), homeTarget, homePoint.distance());
                }
            }

            BookPageGeometry.PagePoint point = BookPageGeometry.intersect(book, origin, direction, REACH);
            if (point == null) continue;
            int pageIndex = book.getReaderPage() + (point.left() ? 0 : 1);
            PhysicalBookContent.Page page = PhysicalBookContent.page(book.getContentSourcePos(), pageIndex);
            int target = targetAt(page, point.left(), point.x(), point.y());
            if (target >= 0 && (best == null || point.distance() < best.distance)) {
                best = Pick.entity(book.getId(), target, point.distance());
            }
        }

        if (best == null) return;
        if (best.blockPos != null) {
            ModNetworking.CHANNEL.sendToServer(BookButtonClickPacket.forBlock(best.blockPos, best.targetPage));
        } else {
            ModNetworking.CHANNEL.sendToServer(BookButtonClickPacket.forEntity(best.entityId, best.targetPage));
        }
        event.setCanceled(true);
        mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    private static int homeTargetAt(PhysicalBookContent.Page left, PhysicalBookContent.Page right, float x, float y) {
        BookContentLayout.Button home = BookPageLayout.homeButton(left, right);
        BookPageLayout.ButtonRect rect = BookPageLayout.homeRect(home);
        return rect != null && rect.contains(x, y, BUTTON_TOLERANCE) ? home.targetPage() : -1;
    }

    private static int targetAt(PhysicalBookContent.Page page, boolean leftPage, float x, float y) {
        if (page == null) return -1;
        for (BookPageLayout.ButtonRect rect : BookPageLayout.buttons(page, leftPage)) {
            if (rect.contains(x, y, BUTTON_TOLERANCE)) return rect.button().targetPage();
        }
        return -1;
    }

    private static final class Pick {
        final BlockPos blockPos;
        final int entityId;
        final int targetPage;
        final double distance;
        private Pick(BlockPos pos, int entityId, int targetPage, double distance) {
            this.blockPos = pos; this.entityId = entityId; this.targetPage = targetPage; this.distance = distance;
        }
        static Pick block(BlockPos pos, int target, double distance) { return new Pick(pos, -1, target, distance); }
        static Pick entity(int id, int target, double distance) { return new Pick(null, id, target, distance); }
    }
}
