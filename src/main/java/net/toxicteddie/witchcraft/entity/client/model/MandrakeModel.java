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
import net.toxicteddie.witchcraft.entity.custom.MandrakeEntity;

public class MandrakeModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;
	private final ModelPart hip;
	private final ModelPart spine;
	private final ModelPart chest;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart top;
	private final ModelPart bottom;
	private final ModelPart mouth;
	private final ModelPart arms;
	private final ModelPart l_arm;
	private final ModelPart r_arm;
	private final ModelPart legs;
	private final ModelPart r_leg;
	private final ModelPart l_leg;

	public MandrakeModel(ModelPart root) {
		this.root = root;
		this.hip = root.getChild("hip");
		this.spine = this.hip.getChild("spine");
		this.chest = this.spine.getChild("chest");
		this.torso = this.chest.getChild("torso");
		this.head = this.chest.getChild("head");
		this.top = this.head.getChild("top");
		this.bottom = this.head.getChild("bottom");
		this.mouth = this.bottom.getChild("mouth");
		this.arms = this.chest.getChild("arms");
		this.l_arm = this.arms.getChild("l_arm");
		this.r_arm = this.arms.getChild("r_arm");
		this.legs = this.hip.getChild("legs");
		this.r_leg = this.legs.getChild("r_leg");
		this.l_leg = this.legs.getChild("l_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition hip = partdefinition.addOrReplaceChild("hip", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition spine = hip.addOrReplaceChild("spine", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition torso = chest.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 15).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition top = head.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(20, 5).addBox(-1.5F, -6.0F, -2.5F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 2.5F));

		PartDefinition bottom = head.addOrReplaceChild("bottom", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		PartDefinition mouth = bottom.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 9).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition arms = chest.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, -0.5F, 0.0F));

		PartDefinition l_arm = arms.addOrReplaceChild("l_arm", CubeListBuilder.create().texOffs(10, 15).addBox(0.0F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 21).addBox(1.0F, -0.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(12, 20).addBox(0.5F, 1.0F, 0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(14, 20).addBox(0.5F, -0.5F, -1.5F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, 0.0F));

		PartDefinition r_arm = arms.addOrReplaceChild("r_arm", CubeListBuilder.create().texOffs(14, 15).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 21).addBox(-2.0F, -0.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 7).addBox(-0.5F, 0.0F, -1.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 20).addBox(-0.5F, -0.5F, 0.5F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 0.0F, 0.0F));

		PartDefinition legs = hip.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0167F));

		PartDefinition r_leg = legs.addOrReplaceChild("r_leg", CubeListBuilder.create().texOffs(18, 15).addBox(-0.5F, 0.0F, -0.5333F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 21).addBox(-1.5F, 0.0F, -0.0333F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 20).addBox(0.0F, 0.0F, 0.4667F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, 0.0F, 0.0167F));

		PartDefinition l_leg = legs.addOrReplaceChild("l_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 21).addBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(20, 20).addBox(0.0F, 0.0F, -1.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 21).addBox(0.0F, 0.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.75F, 0.0F, -0.0167F));

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
		MandrakeEntity mandrake = (MandrakeEntity) entity;

		this.root().getAllParts().forEach(ModelPart::resetPose);


		this.animate(
				mandrake.idleAnimationState,
				ModAnimationDefinitions.MANDRAKE_IDLE,
				ageInTicks,
				1.0F
		);


		if (limbSwingAmount > 0.01F) {
			this.animateWalk(
					ModAnimationDefinitions.MANDRAKE_RUN,
					limbSwing,
					limbSwingAmount,
					2.0F,
					2.5F
			);
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
		this.head.xRot += headPitch * Mth.DEG_TO_RAD;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		hip.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

    @Override
    public ModelPart root() {
        return this.root;
    }
}
