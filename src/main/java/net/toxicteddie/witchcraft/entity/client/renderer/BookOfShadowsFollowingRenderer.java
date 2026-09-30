package net.toxicteddie.witchcraft.entity.client.renderer;

import net.toxicteddie.witchcraft.client.book.BookPageTurnLayout;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsAnimations;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;
import net.toxicteddie.witchcraft.client.book.PhysicalBookContent;
import net.toxicteddie.witchcraft.client.book.PhysicalBookPageRenderer;

import java.util.List;
import net.toxicteddie.witchcraft.entity.custom.BookOfShadowsFollowingEntity;


public class BookOfShadowsFollowingRenderer extends EntityRenderer<BookOfShadowsFollowingEntity> {
    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/base.png");
    private static final ResourceLocation PAPER_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/paper.png");
    private static final ResourceLocation METAL_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/metal.png");

    private final BookOfShadowsModel baseModel;
    private final BookOfShadowsModel paperModel;
    private final BookOfShadowsModel metalModel;

    public BookOfShadowsFollowingRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
        this.baseModel = new BookOfShadowsModel(context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_LAYER));
        this.paperModel = new BookOfShadowsModel(context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_PAPER_LAYER));
        this.metalModel = new BookOfShadowsModel(context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_METAL_LAYER));
    }

    @Override
    public void render(BookOfShadowsFollowingEntity book, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {


        book.ensureFloatingAnimationStarted();

        poseStack.pushPose();


        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        poseStack.scale(1.0F, -1.0F, -1.0F);

        float ageInTicks = book.tickCount + partialTick;
        baseModel.applyAnimation(book.floatingIdleAnimation, BookOfShadowsAnimations.BOOK_FLOAT_IDLE,
                ageInTicks, 0.88F);
        paperModel.applyAnimation(book.floatingIdleAnimation, BookOfShadowsAnimations.BOOK_FLOAT_IDLE,
                ageInTicks, 0.88F);
        metalModel.applyAnimation(book.floatingIdleAnimation, BookOfShadowsAnimations.BOOK_FLOAT_IDLE,
                ageInTicks, 0.88F);

        if (book.isPageTurning()) {


            var turn = book.getPageTurnDirection() > 0
                    ? BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_FORWARD
                    : BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_BACKWARD;
            baseModel.applyOverlayAnimation(book.pageTurnAnimationState, turn, ageInTicks, 1.0F);
            paperModel.applyOverlayAnimation(book.pageTurnAnimationState, turn, ageInTicks, 1.0F);
            metalModel.applyOverlayAnimation(book.pageTurnAnimationState, turn, ageInTicks, 1.0F);
        }

        renderLayer(baseModel, poseStack, buffer, packedLight, BASE_TEXTURE);
        renderLayer(paperModel, poseStack, buffer, packedLight, PAPER_TEXTURE);
        renderLayer(metalModel, poseStack, buffer, packedLight, METAL_TEXTURE);

        if (book.isReaderActive()) {
            renderPhysicalPages(book, partialTick, poseStack, buffer, packedLight);
        }

        poseStack.popPose();
        super.render(book, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderPhysicalPages(BookOfShadowsFollowingEntity book, float partialTick, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        int newSpread = book.getReaderPage();
        BookPageTurnLayout.Spread spread = BookPageTurnLayout.visibleSpread(
                newSpread,
                book.isPageTurning(),
                book.getPageTurnDirection(),
                book.getClientPageTurnProgress(),
                BookPageTurnLayout.Presentation.FLOATING,
                baseModel.getFlipPageZRot());
        int leftIndex = spread.left();
        int rightIndex = spread.right();

        PhysicalBookContent.Page left = PhysicalBookContent.page(book.getContentSourcePos(), leftIndex);
        PhysicalBookContent.Page right = PhysicalBookContent.page(book.getContentSourcePos(), rightIndex);
        if (left != null) PhysicalBookPageRenderer.renderPage(baseModel, left, true, poseStack, buffer, packedLight);
        if (right != null) PhysicalBookPageRenderer.renderPage(baseModel, right, false, poseStack, buffer, packedLight);
        PhysicalBookPageRenderer.renderHome(baseModel, left, right, poseStack, buffer, packedLight);

        if (book.isPageTurning()) {

            int flipIndex = book.getPageTurnDirection() > 0
                    ? Math.max(0, book.getReaderPage() - 1)
                    : book.getReaderPage() + 2;
            boolean fromRightPage = book.getPageTurnDirection() > 0;
            PhysicalBookContent.Page flip = PhysicalBookContent.page(book.getContentSourcePos(), flipIndex);
            int reverseIndex = fromRightPage ? flipIndex + 1 : Math.max(0, flipIndex - 1);
            PhysicalBookContent.Page reverse = PhysicalBookContent.page(book.getContentSourcePos(), reverseIndex);
            if (flip != null) PhysicalBookPageRenderer.renderFlipPage(baseModel, flip, fromRightPage, poseStack, buffer, packedLight);
            if (reverse != null) PhysicalBookPageRenderer.renderFlipPage(baseModel, reverse, !fromRightPage, poseStack, buffer, packedLight);
        }
    }

    private static void renderLayer(BookOfShadowsModel model, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight,
                                    ResourceLocation texture) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(BookOfShadowsFollowingEntity entity) {
        return BASE_TEXTURE;
    }
}
