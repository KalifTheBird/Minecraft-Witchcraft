package net.toxicteddie.witchcraft.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;


public class BookOfShadowsModel extends HierarchicalModel<Entity> {
    private static final float PAPER_SHELL = 0.006F;
    private static final float METAL_SHELL = 0.012F;


    private static final float PAGE_SEPARATION = 0.03F;

    private final ModelPart root;

    public BookOfShadowsModel(ModelPart bakedRoot) {
        this.root = bakedRoot.getChild("root");
    }


    public static LayerDefinition createBodyLayer() {
        return createLayer(0.0F);
    }


    public static LayerDefinition createPaperLayer() {
        return createLayer(PAPER_SHELL);
    }


    public static LayerDefinition createMetalLayer() {
        return createLayer(METAL_SHELL);
    }

    private static LayerDefinition createLayer(float shell) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation deformation = new CubeDeformation(shell);

        PartDefinition root = partdefinition.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 22.5F, -0.2F)
        );

        root.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(0, 60).addBox(9.6F, -0.5F, -5.8F, 0.0F, 1.0F, 12.0F, deformation)
                .texOffs(48, 68).addBox(-0.5F, -0.5F, -5.9F, 10.0F, 1.0F, 0.0F, deformation)
                .texOffs(0, 13).addBox(-0.5F, -0.5F, -5.8F, 10.0F, 1.0F, 12.0F, deformation)
                .texOffs(68, 68).addBox(-0.5F, -0.5F, 6.3F, 10.0F, 1.0F, 0.0F, deformation)
                .texOffs(44, 0).addBox(-0.4F, 0.0F, -4.8F, 9.0F, 1.0F, 10.0F, deformation),
                PartPose.offset(-4.5F, -1.0F, 0.0F));

        root.addOrReplaceChild("bottom", CubeListBuilder.create()
                .texOffs(0, 59).addBox(9.6F, -0.5F, -5.9F, 0.0F, 1.0F, 12.0F, deformation)
                .texOffs(0, 0).addBox(-0.5F, -0.5F, -5.8F, 10.0F, 1.0F, 12.0F, deformation)
                .texOffs(44, 31).addBox(-0.5F, -0.5F, -5.9F, 10.0F, 1.0F, 0.0F, deformation)
                .texOffs(44, 32).addBox(-0.5F, -0.5F, 6.3F, 10.0F, 1.0F, 0.0F, deformation)
                .texOffs(40, 38).addBox(-0.4F, -1.0F, -4.8F, 9.0F, 1.0F, 10.0F, deformation),
                PartPose.offset(-4.5F, 1.0F, 0.0F));

        root.addOrReplaceChild("bind", CubeListBuilder.create()
                .texOffs(38, 49).addBox(-0.5F, -0.5F, -5.8F, 1.0F, 1.0F, 12.0F, deformation)
                .texOffs(24, 62).addBox(0.0F, -0.5F, -5.3F, 1.0F, 1.0F, 11.0F, deformation),
                PartPose.offset(-4.5F, 0.0F, 0.0F));

        PartDefinition middle = root.addOrReplaceChild(
                "middle",
                CubeListBuilder.create(),
                PartPose.offset(-3.5F, 0.0F, 0.0F)
        );


        middle.addOrReplaceChild("right", CubeListBuilder.create()
                        .texOffs(44, 11).addBox(-0.6F, PAGE_SEPARATION, -4.8F,
                                9.0F, 0.0F, 10.0F, deformation),
                PartPose.ZERO);

        middle.addOrReplaceChild("flip", CubeListBuilder.create()
                        .texOffs(0, 49).addBox(-0.6F, 0.0F, -4.8F,
                                9.0F, 0.0F, 10.0F, deformation),
                PartPose.ZERO);

        middle.addOrReplaceChild("left", CubeListBuilder.create()
                        .texOffs(44, 21).addBox(-0.6F, -PAGE_SEPARATION, -4.8F,
                                9.0F, 0.0F, 10.0F, deformation),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    public void applyAnimation(AnimationState state, AnimationDefinition definition,
                               float ageInTicks, float speed) {
        root.getAllParts().forEach(ModelPart::resetPose);
        animate(state, definition, ageInTicks, speed);
    }


    public void applyOverlayAnimation(AnimationState state, AnimationDefinition definition,
                                      float ageInTicks, float speed) {
        animate(state, definition, ageInTicks, speed);
    }


    public void translateToPage(PoseStack poseStack, boolean leftPage) {
        root.translateAndRotate(poseStack);
        ModelPart middle = root.getChild("middle");
        middle.translateAndRotate(poseStack);
        middle.getChild(leftPage ? "left" : "right").translateAndRotate(poseStack);
    }


    public void translateToBinding(PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        root.getChild("bind").translateAndRotate(poseStack);
    }


    public float getFlipPageZRot() {
        return root.getChild("middle").getChild("flip").zRot;
    }


    public void translateToFlipPage(PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        ModelPart middle = root.getChild("middle");
        middle.translateAndRotate(poseStack);
        middle.getChild("flip").translateAndRotate(poseStack);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
