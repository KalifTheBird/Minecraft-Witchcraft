package net.toxicteddie.witchcraft.item.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;


public class WitchesHatModel<T extends LivingEntity> extends HumanoidModel<T> {

    public WitchesHatModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild(
                "head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild(
                "left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

PartDefinition Head = head.addOrReplaceChild("witch_hat", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition Top = Head.addOrReplaceChild("Top", CubeListBuilder.create(), PartPose.offset(0.4F, 25.118F, -0.3F));

		PartDefinition cube_r1 = Top.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(18, 48).addBox(4.0F, -5.468F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(56, 56).addBox(5.0F, -5.468F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(46, 56).addBox(-6.0F, -5.468F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 48).addBox(-5.0F, -5.468F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(50, 48).addBox(3.0F, -7.468F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(36, 48).addBox(-4.0F, -7.468F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(36, 15).addBox(-3.0F, -7.468F, -4.0F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(10, 57).addBox(-2.0F, -5.468F, -6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 57).addBox(-2.0F, -5.468F, 5.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-4.0F, -5.468F, -5.0F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r2 = Top.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(56, 36).addBox(-3.0F, -9.5439F, -0.5146F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(36, 56).addBox(2.0F, -9.5439F, -0.5146F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 39).addBox(-2.0F, -9.5439F, -1.5146F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.6554F, -0.0358F, 3.0125F));

		PartDefinition cube_r3 = Top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(6, 59).addBox(-2.0F, -11.0085F, 2.8542F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 59).addBox(1.0F, -11.0085F, 2.8542F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(52, 29).addBox(-1.0F, -11.0085F, 1.8542F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.3936F, -0.0358F, 3.0125F));

		PartDefinition cube_r4 = Top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 63).addBox(-1.2F, -12.2075F, 1.6996F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 36).addBox(-0.2F, -12.2075F, 2.6996F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 57).addBox(-0.2F, -13.2075F, 2.6996F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(12, 59).addBox(-0.2F, -12.2075F, 4.6996F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(20, 57).addBox(-1.0F, -11.2075F, 5.1996F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.1754F, -0.0358F, 3.0125F));

		PartDefinition Rim = Head.addOrReplaceChild("Rim", CubeListBuilder.create(), PartPose.offset(0.6F, 25.118F, -0.5F));

		PartDefinition cube_r5 = Rim.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(52, 0).addBox(-4.0F, -6.468F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.2F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r6 = Rim.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(56, 61).addBox(-4.0F, -6.4179F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 61).addBox(-3.0F, -6.4179F, -4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.4F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r7 = Rim.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(18, 61).addBox(2.0F, -6.3629F, -4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -24.0F, 0.4F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r8 = Rim.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(56, 43).addBox(-3.0F, -6.388F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -24.0F, 0.4F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r9 = Rim.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 61).addBox(3.0F, -6.4629F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -23.9F, 0.4F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r10 = Rim.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(52, 7).addBox(3.0F, -6.4079F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -24.0F, 0.2F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r11 = Rim.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(46, 61).addBox(2.0F, -6.468F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 62).addBox(2.0F, -6.4639F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -24.0F, 0.0F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r12 = Rim.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(56, 45).addBox(-3.0F, -6.4879F, 3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -24.0F, 0.0F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition cube_r13 = Rim.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(60, 61).addBox(-3.0F, -6.5179F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 61).addBox(-4.0F, -6.5179F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.8735F, -0.0358F, 3.0125F));

		PartDefinition Base = Head.addOrReplaceChild("Base", CubeListBuilder.create(), PartPose.offset(0.4F, 25.118F, -0.3F));

		PartDefinition cube_r14 = Base.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(52, 27).addBox(-4.0F, -4.468F, 7.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 39).addBox(7.0F, -4.468F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(6.0F, -4.468F, -6.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(52, 25).addBox(-4.0F, -4.468F, -8.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.0F, -4.468F, -7.0F, 12.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(26, 26).addBox(-7.0F, -4.468F, -6.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(38, 39).addBox(-8.0F, -4.468F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -2.8735F, -0.0358F, 3.0125F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
