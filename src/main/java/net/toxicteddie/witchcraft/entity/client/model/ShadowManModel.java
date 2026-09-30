package net.toxicteddie.witchcraft.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.toxicteddie.witchcraft.Witchcraft;
import net.toxicteddie.witchcraft.entity.animation.TheShadowManAnimation;
import net.toxicteddie.witchcraft.entity.custom.ShadowManEntity;

public class ShadowManModel<T extends Entity> extends HierarchicalModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Witchcraft.MOD_ID, "shadow_man"), "main");
	private final ModelPart root;
	private final ModelPart hip;
	private final ModelPart spine;
	private final ModelPart torso;
	private final ModelPart chest;
	private final ModelPart upper;
	private final ModelPart tail;
	private final ModelPart arms;
	private final ModelPart r_arm;
	private final ModelPart l_arm;
	private final ModelPart head;
	private final ModelPart skull;
	private final ModelPart horns;
	private final ModelPart l_horn;
	private final ModelPart r_horn;
	private final ModelPart ears;
	private final ModelPart l_ear;
	private final ModelPart r_ear;
	private final ModelPart legs;
	private final ModelPart r_leg;
	private final ModelPart l_leg;

	public ShadowManModel(ModelPart root) {
		this.root = root.getChild("root");
		this.hip = this.root.getChild("hip");
		this.spine = this.hip.getChild("spine");
		this.torso = this.spine.getChild("torso");
		this.chest = this.torso.getChild("chest");
		this.upper = this.chest.getChild("upper");
		this.tail = this.chest.getChild("tail");
		this.arms = this.torso.getChild("arms");
		this.r_arm = this.arms.getChild("r_arm");
		this.l_arm = this.arms.getChild("l_arm");
		this.head = this.spine.getChild("head");
		this.skull = this.head.getChild("skull");
		this.horns = this.head.getChild("horns");
		this.l_horn = this.horns.getChild("l_horn");
		this.r_horn = this.horns.getChild("r_horn");
		this.ears = this.head.getChild("ears");
		this.l_ear = this.ears.getChild("l_ear");
		this.r_ear = this.ears.getChild("r_ear");
		this.legs = this.hip.getChild("legs");
		this.r_leg = this.legs.getChild("r_leg");
		this.l_leg = this.legs.getChild("l_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition hip = root.addOrReplaceChild("hip", CubeListBuilder.create(), PartPose.offset(0.0F, -27.0F, 0.5F));

		PartDefinition spine = hip.addOrReplaceChild("spine", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition torso = spine.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest = torso.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition upper = chest.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5F, -8.0F, -2.0F, 9.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition tail = chest.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -4.5F, 1.0F));

		PartDefinition cube_r1 = tail.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(62, 18).addBox(-2.5F, -1.4537F, 0.0507F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(42, 68).addBox(-0.5F, -3.5463F, -0.5507F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.9739F, 23.1972F, 2.138F, 0.0F, 0.0F));

		PartDefinition cube_r2 = tail.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(62, 24).addBox(-2.4493F, -1.4537F, 0.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.9739F, 23.1972F, 1.5708F, 0.5672F, -1.5708F));

		PartDefinition cube_r3 = tail.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(46, 68).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.2491F, 18.9239F, 1.8326F, 0.0F, 0.0F));

		PartDefinition cube_r4 = tail.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(52, 62).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0117F, 15.1006F, 1.6581F, 0.0F, 0.0F));

		PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 64).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.4028F, 10.4755F, 1.2217F, 0.0F, 0.0F));

		PartDefinition cube_r6 = tail.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 61).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.2094F, 6.2298F, 1.0036F, 0.0F, 0.0F));

		PartDefinition cube_r7 = tail.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(20, 61).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 2.0F, 0.6981F, 0.0F, 0.0F));

		PartDefinition arms = torso.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, 0.0F));

		PartDefinition r_arm = arms.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, -3.0F, -2.5F, 5.0F, 20.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 0.0F, 0.0F));

		PartDefinition l_arm = arms.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(0, 39).addBox(-5.0F, -3.0F, -2.5F, 5.0F, 20.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 0.0F, 0.0F));

		PartDefinition head = spine.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -17.0F, 0.0F));

		PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -9.0F, -1.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 0.0F, -3.5F));

		PartDefinition horns = head.addOrReplaceChild("horns", CubeListBuilder.create(), PartPose.offset(-1.5F, 41.0F, -0.5F));

		PartDefinition l_horn = horns.addOrReplaceChild("l_horn", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

		PartDefinition cube_r8 = l_horn.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(68, 56).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5464F, -53.1935F, 3.8154F, -0.4902F, 0.6411F, 1.0655F));

		PartDefinition cube_r9 = l_horn.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(20, 69).addBox(-0.3277F, -1.0188F, -0.3121F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2355F, -53.754F, 5.9243F, -0.9771F, 0.2943F, 0.7661F));

		PartDefinition cube_r10 = l_horn.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(24, 69).addBox(-0.0034F, 0.6493F, -0.3121F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2355F, -53.754F, 5.9243F, -0.86F, 0.6084F, 1.03F));

		PartDefinition cube_r11 = l_horn.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(68, 68).addBox(-0.3277F, -2.6326F, 0.054F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.2355F, -53.754F, 5.9243F, -0.5408F, 0.2943F, 0.7661F));

		PartDefinition cube_r12 = l_horn.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 64).addBox(-2.6903F, -0.5825F, -1.9167F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4411F, -51.3722F, 2.794F, -0.7073F, 0.7324F, 0.8726F));

		PartDefinition cube_r13 = l_horn.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(44, 62).addBox(-4.3189F, 0.5142F, -1.9167F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4411F, -51.3722F, 2.794F, -0.9641F, 0.1316F, 0.2996F));

		PartDefinition cube_r14 = l_horn.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(56, 60).addBox(-5.4311F, 1.8778F, -3.9099F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.4411F, -51.3722F, 2.794F, -0.5672F, 0.0F, 0.0F));

		PartDefinition r_horn = horns.addOrReplaceChild("r_horn", CubeListBuilder.create(), PartPose.offset(1.5F, -22.5261F, 24.6972F));

		PartDefinition cube_r15 = r_horn.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(68, 65).addBox(-0.6723F, -2.6326F, 0.054F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7355F, -31.2279F, -18.7728F, -0.5408F, -0.2943F, -0.7661F));

		PartDefinition cube_r16 = r_horn.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(68, 62).addBox(-0.6723F, -1.0188F, -0.3121F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7355F, -31.2279F, -18.7728F, -0.9771F, -0.2943F, -0.7661F));

		PartDefinition cube_r17 = r_horn.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(68, 53).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0464F, -30.6675F, -20.8818F, -0.4902F, -0.6411F, -1.0655F));

		PartDefinition cube_r18 = r_horn.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(68, 59).addBox(-0.9966F, 0.6493F, -0.3121F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.7355F, -31.2279F, -18.7728F, -0.86F, -0.6084F, -1.03F));

		PartDefinition cube_r19 = r_horn.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(62, 36).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0687F, -30.3279F, -21.9759F, -0.7073F, -0.7324F, -0.8726F));

		PartDefinition cube_r20 = r_horn.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(36, 62).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2722F, -29.2675F, -24.0336F, -0.9641F, -0.1316F, -0.2996F));

		PartDefinition cube_r21 = r_horn.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(56, 53).addBox(2.4311F, 1.8778F, -3.9099F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.9411F, -28.8461F, -21.9032F, -0.5672F, 0.0F, 0.0F));

		PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(-1.5F, 41.0F, -0.5F));

		PartDefinition l_ear = ears.addOrReplaceChild("l_ear", CubeListBuilder.create().texOffs(68, 0).addBox(0.0F, -0.1F, -3.3F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(20, 39).addBox(0.0F, -1.1F, -2.3F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(30, 69).addBox(0.0F, -1.1F, 0.7F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 12).addBox(0.0F, -2.1F, -0.3F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(50, 68).addBox(0.0F, -0.1F, -0.3F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.2629F, -46.9F, 1.0488F, 0.0F, 0.3927F, 0.0F));

		PartDefinition r_ear = ears.addOrReplaceChild("r_ear", CubeListBuilder.create().texOffs(14, 68).addBox(0.0F, -0.1F, -0.3F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 6).addBox(0.0F, -0.1F, -3.3F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(8, 68).addBox(0.0F, -2.1F, -0.3F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(28, 69).addBox(0.0F, -1.1F, 0.7F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 68).addBox(0.0F, -1.1F, -2.3F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2629F, -46.9F, 1.0488F, 0.0F, -0.3927F, 0.0F));

		PartDefinition legs = hip.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 2.9396F, 0.8089F));

		PartDefinition r_leg = legs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(56, 9).addBox(-1.5F, 17.1604F, -2.5089F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(56, 48).addBox(-2.0F, 23.1604F, -2.4089F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.7F, -1.1F, 0.2F));

		PartDefinition cube_r22 = r_leg.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(60, 67).addBox(-0.0418F, 1.0F, -2.4485F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.1604F, -2.2199F, 0.0F, -0.1309F, 0.0F));

		PartDefinition cube_r23 = r_leg.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(52, 67).addBox(-1.9582F, 1.0F, -2.4485F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.1604F, -2.2199F, 0.0F, 0.1309F, 0.0F));

		PartDefinition cube_r24 = r_leg.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(46, 18).addBox(-2.5F, -5.0F, -2.5F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 15.0754F, -1.6362F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r25 = r_leg.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(20, 43).addBox(-2.29F, -5.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 4.7733F, -1.5267F, -0.3054F, 0.0F, 0.0F));

		PartDefinition cube_r26 = r_leg.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(46, 36).addBox(-2.5F, -4.5F, -0.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.6F, 0.6F, 0.6109F, 0.0F, 0.0F));

		PartDefinition l_leg = legs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.5F, 17.1604F, -2.5089F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 23.1604F, -2.4089F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.7F, -1.1F, 0.2F));

		PartDefinition cube_r27 = l_leg.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(8, 64).addBox(-0.0418F, 1.0F, -2.4485F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.1604F, -2.2199F, 0.0F, -0.1309F, 0.0F));

		PartDefinition cube_r28 = l_leg.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(64, 30).addBox(-1.9582F, 1.0F, -2.4485F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 22.1604F, -2.2199F, 0.0F, 0.1309F, 0.0F));

		PartDefinition cube_r29 = l_leg.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(40, 43).addBox(-2.5F, -5.0F, -2.5F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 15.0754F, -1.6362F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r30 = l_leg.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(36, 0).addBox(-2.31F, -5.0F, -2.5F, 5.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 4.7733F, -1.5267F, -0.3054F, 0.0F, 0.0F));

		PartDefinition cube_r31 = l_leg.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(40, 55).addBox(-2.5F, -4.5F, -0.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.6F, 0.6F, 0.6109F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if (entity instanceof ShadowManEntity shadowMan) {
			this.animate(shadowMan.idleAnimationState, TheShadowManAnimation.BEAST_IDLE, ageInTicks, 1.0F);
		}
		if (limbSwingAmount > 0.01F) {
			this.animateWalk(TheShadowManAnimation.BEAST_WALK, limbSwing, limbSwingAmount, 1.0F, 2.5F);
		}
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = headPitch * ((float)Math.PI / 180F);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
