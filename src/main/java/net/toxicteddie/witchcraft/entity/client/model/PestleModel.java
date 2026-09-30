package net.toxicteddie.witchcraft.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class PestleModel extends Model {

    private final ModelPart pestle;

    public PestleModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.pestle = root.getChild("pestle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild(
                "pestle",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -1.0F,
                                -4.0F,
                                -1.0F,
                                2.0F,
                                8.0F,
                                2.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-1.3827F, 18.0761F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    public void setRotationDegrees(float xDegrees, float yDegrees, float zDegrees) {
        float radians = (float) Math.PI / 180.0F;
        this.pestle.xRot = xDegrees * radians;
        this.pestle.yRot = yDegrees * radians;
        this.pestle.zRot = zDegrees * radians;
    }

    public void resetRotation() {
        this.pestle.xRot = 0.0F;
        this.pestle.yRot = 0.0F;
        this.pestle.zRot = 0.0F;
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        this.pestle.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}
