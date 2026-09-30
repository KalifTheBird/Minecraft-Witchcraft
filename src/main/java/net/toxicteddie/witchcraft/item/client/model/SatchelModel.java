package net.toxicteddie.witchcraft.item.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class SatchelModel<T extends LivingEntity> extends HumanoidModel<T> {
    public SatchelModel(ModelPart root) { super(root); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

PartDefinition Torso = body.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(18, 0).addBox(-4.5F, -6.7F, -2.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(-4.5F, -6.5F, 2.2F, 9.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.5F, -6.5F, -2.2F, 9.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 4).addBox(-6.0F, 4.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(12, 28).addBox(-6.0F, 4.0F, 3.1F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(12, 24).addBox(-6.0F, 4.0F, -3.1F, 2.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 14).addBox(-6.1F, 3.6F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(16, 30).addBox(-6.2F, 4.6F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 30).addBox(-6.2F, 4.6F, 1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 22).addBox(-5.0F, 3.6F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-3.9F, 3.6F, -3.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.5F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
