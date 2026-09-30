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
import net.toxicteddie.witchcraft.entity.custom.DemonEntity;

public class DemonModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart Demon;
    private final ModelPart hip;
    private final ModelPart spine;
    private final ModelPart chest;
    private final ModelPart torso;
    private final ModelPart arms;
    private final ModelPart l_arm;
    private final ModelPart r_arm;
    private final ModelPart head;
    private final ModelPart skull;
    private final ModelPart hornes;
    private final ModelPart l_horn;
    private final ModelPart r_horn;
    private final ModelPart ears;
    private final ModelPart l_ear;
    private final ModelPart r_ear;
    private final ModelPart tail;
    private final ModelPart legs;
    private final ModelPart r_leg;
    private final ModelPart l_leg;

    public DemonModel(ModelPart root) {
        this.root = root;
        this.Demon = root.getChild("Demon");
        this.hip = this.Demon.getChild("hip");
        this.spine = this.hip.getChild("spine");
        this.chest = this.spine.getChild("chest");
        this.torso = this.chest.getChild("torso");
        this.arms = this.chest.getChild("arms");
        this.l_arm = this.arms.getChild("l_arm");
        this.r_arm = this.arms.getChild("r_arm");
        this.head = this.chest.getChild("head");
        this.skull = this.head.getChild("skull");
        this.hornes = this.head.getChild("hornes");
        this.l_horn = this.hornes.getChild("l_horn");
        this.r_horn = this.hornes.getChild("r_horn");
        this.ears = this.head.getChild("ears");
        this.l_ear = this.ears.getChild("l_ear");
        this.r_ear = this.ears.getChild("r_ear");
        this.tail = this.chest.getChild("tail");
        this.legs = this.hip.getChild("legs");
        this.r_leg = this.legs.getChild("r_leg");
        this.l_leg = this.legs.getChild("l_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Demon = partdefinition.addOrReplaceChild("Demon", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition hip = Demon.addOrReplaceChild("hip", CubeListBuilder.create(), PartPose.offset(0.0F, 4.75F, 0.0F));

        PartDefinition spine = hip.addOrReplaceChild("spine", CubeListBuilder.create(), PartPose.offset(0.0F, -2.75F, 0.0F));

        PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition torso = chest.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -6.5F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, 0.0F));

        PartDefinition arms = chest.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition l_arm = arms.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(24, 16).addBox(0.0F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, 0.0F));

        PartDefinition r_arm = arms.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, 0.0F));

        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 0.0F));

        PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hornes = head.addOrReplaceChild("hornes", CubeListBuilder.create(), PartPose.offset(0.0F, -5.6F, -3.0F));

        PartDefinition l_horn = hornes.addOrReplaceChild("l_horn", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0203F, -0.4874F, 0.415F, -0.3914F, 0.0334F, 0.0807F));

        PartDefinition cube_r1 = l_horn.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 31).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4295F, -5.0704F, 0.7327F, -0.6545F, 0.0F, 0.1309F));

        PartDefinition cube_r2 = l_horn.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 41).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.049F, -1.4355F, -0.1221F, 0.0F, 0.0F, 0.1309F));

        PartDefinition cube_r3 = l_horn.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.239F, -3.6237F, 0.1027F, -0.2618F, 0.0F, 0.1309F));

        PartDefinition r_horn = hornes.addOrReplaceChild("r_horn", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.6797F, -0.4874F, 0.415F, -0.3914F, 0.0334F, 0.0807F));

        PartDefinition cube_r4 = r_horn.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 50).addBox(-0.5103F, -0.9772F, -0.1683F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5449F, -5.0497F, 0.401F, -0.6545F, 0.0F, -0.2618F));

        PartDefinition cube_r5 = r_horn.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(44, 31).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5471F, -1.3919F, -0.1221F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r6 = r_horn.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(44, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1184F, -3.5238F, 0.1027F, -0.2618F, 0.0F, -0.2618F));

        PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -2.0F));

        PartDefinition l_ear = ears.addOrReplaceChild("l_ear", CubeListBuilder.create().texOffs(48, 40).addBox(0.0233F, -1.0F, -0.0691F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 50).addBox(0.0233F, -2.0F, 1.9309F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 48).addBox(0.0233F, -2.0F, 0.9309F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 19).addBox(0.0233F, -3.0F, 1.9309F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0478F, 0.0F, 0.3927F, 0.0F));

        PartDefinition r_ear = ears.addOrReplaceChild("r_ear", CubeListBuilder.create().texOffs(48, 48).addBox(0.0236F, -1.0F, 0.0615F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(42, 50).addBox(0.0236F, -2.0F, 2.0615F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(44, 50).addBox(0.0236F, -2.0F, 1.0615F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 50).addBox(0.0236F, -3.0F, 2.0615F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, -0.0519F, 0.0F, -0.3927F, 0.0F));

        PartDefinition tail = chest.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.8446F, 1.4186F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r7 = tail.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(50, 22).addBox(-0.5F, -0.5F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 44).addBox(-1.0F, -0.5F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 10).addBox(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 46).addBox(-0.5F, -0.5F, -3.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.3148F, 12.3714F, 0.6545F, 0.0F, 0.0F));

        PartDefinition cube_r8 = tail.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, 0.0381F, -3.4394F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.7555F, 10.1992F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r9 = tail.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(24, 46).addBox(-0.5F, 0.0381F, -3.4395F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.2876F, 7.5377F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r10 = tail.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(16, 46).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.3307F, 3.2021F, -0.7418F, 0.0F, 0.0F));

        PartDefinition cube_r11 = tail.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(40, 19).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.1522F, 1.3742F, -1.0036F, 0.0F, 0.0F));

        PartDefinition legs = hip.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 2.75F, 0.0F));

        PartDefinition r_leg = legs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(40, 24).addBox(-0.95F, 9.5F, -0.3563F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.3F, 0.0F, 0.0F));

        PartDefinition cube_r12 = r_leg.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(48, 24).addBox(-0.75F, -1.3944F, 1.0014F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 12.7141F, -1.0497F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r13 = r_leg.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(32, 48).addBox(-1.25F, 0.5176F, -0.0285F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 8.7421F, -0.9911F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r14 = r_leg.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(24, 50).addBox(-0.9214F, 4.7579F, -1.5333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 8.7421F, 0.0089F, 0.0F, 0.1745F, 0.0F));

        PartDefinition cube_r15 = r_leg.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(32, 50).addBox(-0.0786F, 4.7579F, -1.5333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, 8.7421F, 0.0089F, 0.0F, -0.1745F, 0.0F));

        PartDefinition cube_r16 = r_leg.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(32, 34).addBox(-1.75F, 0.0155F, -2.491F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 6.2769F, -0.8048F, 0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r17 = r_leg.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(16, 34).addBox(-2.25F, -5.741F, -2.1495F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3F, 5.7044F, -1.0492F, -0.2182F, 0.0F, 0.0F));

        PartDefinition l_leg = legs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(32, 41).addBox(-1.05F, 9.5F, -0.3563F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.3F, 0.0F, 0.0F));

        PartDefinition cube_r18 = l_leg.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(48, 27).addBox(-0.75F, -1.3944F, 1.0014F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3F, 12.7141F, -1.0497F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r19 = l_leg.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(48, 46).addBox(-1.25F, 0.5176F, -0.0285F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 8.7421F, -0.9911F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r20 = l_leg.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(28, 50).addBox(-0.9214F, 4.7579F, -1.5333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 8.7421F, 0.0089F, 0.0F, 0.1745F, 0.0F));

        PartDefinition cube_r21 = l_leg.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(36, 50).addBox(-0.0786F, 4.7579F, -1.5333F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.05F, 8.7421F, 0.0089F, 0.0F, -0.1745F, 0.0F));

        PartDefinition cube_r22 = l_leg.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(40, 12).addBox(-1.75F, 0.0155F, -2.491F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 6.2769F, -0.8048F, 0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r23 = l_leg.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(32, 0).addBox(-2.25F, -5.741F, -2.1495F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 5.7044F, -1.0492F, -0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
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
        DemonEntity demon = (DemonEntity) entity;

        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.animate(
                demon.idleAnimationState,
                ModAnimationDefinitions.DEMON_IDLE,
                ageInTicks,
                1.0F
        );

        if (limbSwingAmount > 0.01F) {
            this.animateWalk(
                    ModAnimationDefinitions.DEMON_WALK,
                    limbSwing,
                    limbSwingAmount,
                    1.25F,
                    2.5F
            );
        }

        this.applyHeadRotation(netHeadYaw, headPitch);
    }

    private void applyHeadRotation(
            float netHeadYaw,
            float headPitch
    ) {
        netHeadYaw = Mth.clamp(netHeadYaw, -35.0F, 35.0F);
        headPitch = Mth.clamp(headPitch, -25.0F, 35.0F);

        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.75F;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.65F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Demon.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
