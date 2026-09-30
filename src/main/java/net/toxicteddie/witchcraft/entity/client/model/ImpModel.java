package net.toxicteddie.witchcraft.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.toxicteddie.witchcraft.entity.animation.ModAnimationDefinitions;
import net.toxicteddie.witchcraft.entity.custom.ImpEntity;

public class ImpModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart Imp;
    private final ModelPart hip;
    private final ModelPart legs;
    private final ModelPart l_leg;
    private final ModelPart r_leg;
    private final ModelPart spine;
    private final ModelPart chest;
    private final ModelPart arms;
    private final ModelPart l_arm;
    private final ModelPart r_arm;
    private final ModelPart torso;
    private final ModelPart stomach;
    private final ModelPart wings;
    private final ModelPart r_wing;
    private final ModelPart l_wing;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart skull;
    private final ModelPart horns;
    private final ModelPart ears;
    private final ModelPart l_ear;
    private final ModelPart r_ear;

    public ImpModel(ModelPart root) {
        this.root = root;
        this.Imp = root.getChild("Imp");
        this.hip = this.Imp.getChild("hip");
        this.legs = this.hip.getChild("legs");
        this.l_leg = this.legs.getChild("l_leg");
        this.r_leg = this.legs.getChild("r_leg");
        this.spine = this.hip.getChild("spine");
        this.chest = this.spine.getChild("chest");
        this.arms = this.chest.getChild("arms");
        this.l_arm = this.arms.getChild("l_arm");
        this.r_arm = this.arms.getChild("r_arm");
        this.torso = this.chest.getChild("torso");
        this.stomach = this.torso.getChild("stomach");
        this.wings = this.torso.getChild("wings");
        this.r_wing = this.wings.getChild("r_wing");
        this.l_wing = this.wings.getChild("l_wing");
        this.tail = this.torso.getChild("tail");
        this.head = this.chest.getChild("head");
        this.skull = this.head.getChild("skull");
        this.horns = this.head.getChild("horns");
        this.ears = this.head.getChild("ears");
        this.l_ear = this.ears.getChild("l_ear");
        this.r_ear = this.ears.getChild("r_ear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Imp = partdefinition.addOrReplaceChild("Imp", CubeListBuilder.create(), PartPose.offset(-0.0417F, 19.875F, 0.0F));

        PartDefinition hip = Imp.addOrReplaceChild("hip", CubeListBuilder.create(), PartPose.offset(0.0417F, 0.125F, 0.0F));

        PartDefinition legs = hip.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition l_leg = legs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(18, 19).addBox(-0.4999F, 1.5F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.4F));

        PartDefinition cube_r1 = l_leg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-0.9999F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 1.9651F, 0.2893F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r2 = l_leg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(18, 10).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2325F, -0.7554F, 0.3927F, 0.0F, 0.0F));

        PartDefinition r_leg = legs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(18, 13).addBox(-0.5001F, 1.5F, -0.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.4F));

        PartDefinition cube_r3 = r_leg.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(10, 18).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2325F, -0.7554F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r4 = r_leg.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(10, 15).addBox(-1.0001F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 1.9651F, 0.2893F, 0.3927F, 0.0F, 0.0F));

        PartDefinition spine = hip.addOrReplaceChild("spine", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition arms = chest.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

        PartDefinition l_arm = arms.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(14, 10).addBox(0.0F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, 0.0F));

        PartDefinition r_arm = arms.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(10, 10).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 0.0F, 0.0F));

        PartDefinition torso = chest.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition stomach = torso.addOrReplaceChild("stomach", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition wings = torso.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 1.0F));

        PartDefinition r_wing = wings.addOrReplaceChild("r_wing", CubeListBuilder.create().texOffs(2, 17).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

        PartDefinition l_wing = wings.addOrReplaceChild("l_wing", CubeListBuilder.create().texOffs(2, 17).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 1.3F, 0.7F));

        PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, 0.4F, 0.6109F, 0.0F, 0.0F));

        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition horns = head.addOrReplaceChild("horns", CubeListBuilder.create().texOffs(0, 19).addBox(1.5F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 19).addBox(-2.5F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -2.5F));

        PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -1.45F));

        PartDefinition l_ear = ears.addOrReplaceChild("l_ear", CubeListBuilder.create(), PartPose.offset(2.5F, 0.0F, 0.0F));

        PartDefinition cube_r6 = l_ear.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(4, 19).addBox(0.0F, -2.0F, 1.95F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(6, 16).addBox(0.0F, -1.0F, -0.05F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition r_ear = ears.addOrReplaceChild("r_ear", CubeListBuilder.create(), PartPose.offset(-2.5F, 0.0F, 0.0F));

        PartDefinition cube_r7 = r_ear.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(16, 15).addBox(0.0F, -1.0F, -0.05F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 2).addBox(0.0F, -2.0F, 1.95F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        ImpEntity imp = (ImpEntity) entity;

        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean movingHorizontally =
                imp.getDeltaMovement().horizontalDistanceSqr() > 0.0005D;

        if (imp.isFlying()) {
            if (movingHorizontally) {
                this.animate(
                        imp.flyAnimationState,
                        ModAnimationDefinitions.IMP_FLY,
                        ageInTicks,
                        1.0F
                );
            } else {
                this.animate(
                        imp.flyingIdleAnimationState,
                        ModAnimationDefinitions.IMP_IDLE_FLY,
                        ageInTicks,
                        1.0F
                );
            }
        } else {


            this.animate(
                    imp.idleAnimationState,
                    ModAnimationDefinitions.IMP_IDLE,
                    ageInTicks,
                    1.0F
            );

            if (limbSwingAmount > 0.01F) {
                this.animateWalk(
                        ModAnimationDefinitions.IMP_WALK,
                        limbSwing,
                        limbSwingAmount,
                        1.5F,
                        2.5F
                );
            }
        }

        this.applyHeadRotation(netHeadYaw, headPitch);
    }

    private void applyHeadRotation(
            float netHeadYaw,
            float headPitch
    ) {
        netHeadYaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        headPitch = Mth.clamp(headPitch, -25.0F, 45.0F);


        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.65F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Imp.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
