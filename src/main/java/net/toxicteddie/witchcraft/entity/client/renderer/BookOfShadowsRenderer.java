package net.toxicteddie.witchcraft.block.client.renderer;

import net.toxicteddie.witchcraft.client.book.BookPageTurnLayout;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.toxicteddie.witchcraft.client.book.PhysicalBookContent;
import net.toxicteddie.witchcraft.client.book.PhysicalBookPageRenderer;

import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsAnimations;
import net.toxicteddie.witchcraft.block.client.model.BookOfShadowsModel;
import net.toxicteddie.witchcraft.block.custom.BookOfShadowsBlock;
import net.toxicteddie.witchcraft.block.entity.BookOfShadowsBlockEntity;
import net.toxicteddie.witchcraft.entity.client.ModModelLayers;


public class BookOfShadowsRenderer implements BlockEntityRenderer<BookOfShadowsBlockEntity> {
    private static final ResourceLocation BASE_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/base.png");
    private static final ResourceLocation PAPER_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/paper.png");
    private static final ResourceLocation METAL_TEXTURE =
            new ResourceLocation(Witchcraft.MOD_ID, "textures/block/book_of_shadows/metal.png");

    private final BookOfShadowsModel baseModel;
    private final BookOfShadowsModel paperModel;
    private final BookOfShadowsModel metalModel;

    public BookOfShadowsRenderer(BlockEntityRendererProvider.Context context) {
        this.baseModel = new BookOfShadowsModel(
                context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_LAYER));
        this.paperModel = new BookOfShadowsModel(
                context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_PAPER_LAYER));
        this.metalModel = new BookOfShadowsModel(
                context.bakeLayer(ModModelLayers.BOOK_OF_SHADOWS_METAL_LAYER));
    }

    @Override
    public void render(BookOfShadowsBlockEntity book, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (book.getLevel() == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.5D, 0.5D);

        Direction facing = book.getBlockState().getValue(BookOfShadowsBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));


        poseStack.scale(1.0F, -1.0F, -1.0F);

        AnimationDefinition animation = animationFor(book.getPhase());
        float ageInTicks = book.getLevel().getGameTime() + partialTick;
        float speed = animationSpeedFor(book.getPhase());


        applyAnimationToAll(book, animation, ageInTicks, speed);


        if (book.isPageTurning()) {
            boolean floatingTurn = book.getPhase() == BookOfShadowsBlockEntity.Phase.FLOAT_IDLE;
            AnimationDefinition pageFlip = floatingTurn
                    ? (book.getPageTurnDirection() > 0
                        ? BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_FORWARD
                        : BookOfShadowsAnimations.BOOK_FLOAT_PAGE_FLIP_BACKWARD)
                    : (book.getPageTurnDirection() > 0
                        ? BookOfShadowsAnimations.BOOK_PAGE_FLIP_BACKWARD
                        : BookOfShadowsAnimations.BOOK_PAGE_FLIP_FORWARD);
            applyPageTurnToAll(book, pageFlip, ageInTicks);
        }

        renderLayer(baseModel, poseStack, buffer, packedLight, BASE_TEXTURE);
        renderLayer(paperModel, poseStack, buffer, packedLight, PAPER_TEXTURE);
        renderLayer(metalModel, poseStack, buffer, packedLight, METAL_TEXTURE);


        if (book.isReaderActive() && book.getPhase().open()) {


            renderPhysicalPages(book, poseStack, buffer, packedLight);
        }

        poseStack.popPose();
    }

    private void applyAnimationToAll(BookOfShadowsBlockEntity book,
                                     AnimationDefinition animation,
                                     float ageInTicks,
                                     float speed) {
        baseModel.applyAnimation(book.animationState, animation, ageInTicks, speed);
        paperModel.applyAnimation(book.animationState, animation, ageInTicks, speed);
        metalModel.applyAnimation(book.animationState, animation, ageInTicks, speed);
    }


    private void applyPageTurnToAll(BookOfShadowsBlockEntity book,
                                    AnimationDefinition animation,
                                    float ageInTicks) {
        baseModel.applyOverlayAnimation(book.pageTurnAnimationState, animation, ageInTicks, 1.0F);
        paperModel.applyOverlayAnimation(book.pageTurnAnimationState, animation, ageInTicks, 1.0F);
        metalModel.applyOverlayAnimation(book.pageTurnAnimationState, animation, ageInTicks, 1.0F);
    }

    private void renderPhysicalPages(BookOfShadowsBlockEntity book, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        int newSpread = book.getReaderPage();
        boolean floatingIdle = book.getPhase() == BookOfShadowsBlockEntity.Phase.FLOAT_IDLE;
        BookPageTurnLayout.Spread spread = BookPageTurnLayout.visibleSpread(
                newSpread,
                book.isPageTurning(),
                book.getPageTurnDirection(),
                book.getClientPageTurnProgress(),
                floatingIdle ? BookPageTurnLayout.Presentation.FLOATING
                        : BookPageTurnLayout.Presentation.GROUNDED,
                floatingIdle ? baseModel.getFlipPageZRot() : Float.NaN);
        int leftIndex = spread.left();
        int rightIndex = spread.right();

        PhysicalBookContent.Page left = PhysicalBookContent.page(book.getBlockPos(), leftIndex);
        PhysicalBookContent.Page right = PhysicalBookContent.page(book.getBlockPos(), rightIndex);
        if (left != null) PhysicalBookPageRenderer.renderPage(baseModel, left, true, poseStack, buffer, packedLight);
        if (right != null) PhysicalBookPageRenderer.renderPage(baseModel, right, false, poseStack, buffer, packedLight);
        PhysicalBookPageRenderer.renderHome(baseModel, left, right, poseStack, buffer, packedLight);

        if (book.isPageTurning()) {

            int flipIndex = book.getPageTurnDirection() > 0
                    ? Math.max(0, book.getReaderPage() - 1)
                    : book.getReaderPage() + 2;
            boolean fromRightPage = book.getPageTurnDirection() > 0;
            PhysicalBookContent.Page flip = PhysicalBookContent.page(book.getBlockPos(), flipIndex);
            int reverseIndex = fromRightPage ? flipIndex + 1 : Math.max(0, flipIndex - 1);
            PhysicalBookContent.Page reverse = PhysicalBookContent.page(book.getBlockPos(), reverseIndex);
            if (flip != null) PhysicalBookPageRenderer.renderFlipPage(baseModel, flip, fromRightPage, poseStack, buffer, packedLight);
            if (reverse != null) PhysicalBookPageRenderer.renderFlipPage(baseModel, reverse, !fromRightPage, poseStack, buffer, packedLight);
        }
    }

    private static void renderLayer(BookOfShadowsModel model,
                                    PoseStack poseStack,
                                    MultiBufferSource buffer,
                                    int packedLight,
                                    ResourceLocation texture) {


        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }


    private static float animationSpeedFor(BookOfShadowsBlockEntity.Phase phase) {
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

    @Override
    public boolean shouldRenderOffScreen(BookOfShadowsBlockEntity book) {

        return book.isFloatingOrTransitioning();
    }
}
