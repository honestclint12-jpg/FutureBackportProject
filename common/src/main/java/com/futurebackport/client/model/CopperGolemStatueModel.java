package com.futurebackport.client.model;

import com.futurebackport.client.util.ArgbColor;
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

public class CopperGolemStatueModel extends Model {
   private final ModelPart root;

   public CopperGolemStatueModel(ModelPart roots) {
      super(RenderType::entityCutout);
      this.root = roots.getChild("root");
   }

   public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
      int color = ArgbColor.pack(red, green, blue, alpha);
      this.root.resetPose();
      this.root.y = 0.0F;
      this.root.zRot = (float) Math.PI;
      this.root.render(poseStack, buffer, light, overlay, ArgbColor.red(color), ArgbColor.green(color), ArgbColor.blue(color), ArgbColor.alpha(color));
   }

   public static LayerDefinition createRunningPoseLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-1.064F, -5.0F, 0.0F));
      body.addOrReplaceChild(
         "body_r1",
         CubeListBuilder.create().texOffs(0, 15).addBox(-4.02F, -6.116F, -3.5F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.1F, 0.1F, 0.7F, 0.1204F, -0.0064F, -0.0779F)
      );
      body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -5.1F, -5.0F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(56, 0)
            .addBox(-1.02F, -2.1F, -6.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(37, 8)
            .addBox(-1.02F, -9.1F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))
            .texOffs(37, 0)
            .addBox(-2.0F, -13.1F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F)),
         PartPose.offset(0.7F, -5.6F, -1.8F)
      );
      PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, -6.0F, 0.0F));
      rightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(36, 16).addBox(-3.052F, -1.11F, -2.036F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.7F, -0.248F, -1.62F, 1.0036F, 0.0F, 0.0F)
      );
      PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(4.0F, -6.0F, 0.0F));
      leftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(50, 16).addBox(0.032F, -1.1F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.732F, 0.0F, 0.0F, -0.8715F, -0.0535F, -0.0449F)
      );
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-3.064F, -5.0F, 0.0F));
      rightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 27).addBox(-1.856F, -0.1F, -1.09F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.048F, 0.0F, -0.9F, -0.8727F, 0.0F, 0.0F)
      );
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(0.936F, -5.0F, 0.0F));
      leftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(16, 27).addBox(-2.088F, -0.1F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition createSittingPoseLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(3, 19)
            .addBox(-3.0F, -4.0F, -4.525F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(0, 15)
            .addBox(-4.0F, -3.0F, -3.525F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -3.0F, 2.325F)
      );
      body.addOrReplaceChild(
         "body_r1",
         CubeListBuilder.create().texOffs(3, 18).addBox(-4.0F, -3.0F, -2.2F, 8.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -1.0F, -4.325F, 0.0F, 0.0F, -3.1416F)
      );
      body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(37, 8)
            .addBox(-1.0F, -7.0F, -3.3F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))
            .texOffs(37, 0)
            .addBox(-2.0F, -11.0F, -4.3F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F))
            .texOffs(0, 0)
            .addBox(-4.0F, -3.0F, -7.325F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(56, 0)
            .addBox(-1.0F, 0.0F, -8.325F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -6.0F, -0.2F)
      );
      PartDefinition rightArm = body.addOrReplaceChild(
         "right_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -5.6F, -1.8F, 0.4363F, 0.0F, 0.0F)
      );
      rightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(36, 16).addBox(-3.075F, -0.9733F, -1.9966F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.0893F, 0.1198F, -1.0472F, 0.0F, 0.0F)
      );
      PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, -5.6F, -1.7F, 0.4363F, 0.0F, 0.0F));
      leftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(50, 16).addBox(0.075F, -1.0443F, -1.8997F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -0.0015F, -0.0808F, -1.0472F, 0.0F, 0.0F)
      );
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.1F, -2.1F, -2.075F));
      rightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 27).addBox(-2.0F, 0.975F, 0.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.05F, -1.9F, 1.075F, -1.5708F, 0.0F, 0.0F)
      );
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.0F, -2.0F, -2.075F));
      leftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(16, 27).addBox(-2.0F, 0.975F, 0.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.05F, -2.0F, 1.075F, -1.5708F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition createStarPoseLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -5.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -5.0F, -5.0F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
            .texOffs(56, 0)
            .addBox(-1.0F, -2.0F, -6.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(37, 8)
            .addBox(-1.0F, -9.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))
            .texOffs(37, 0)
            .addBox(-2.0F, -13.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F)),
         PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, -6.0F, 0.0F));
      rightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(36, 16).addBox(-1.5F, -5.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.9199F)
      );
      rightArm.addOrReplaceChild("rightItem", CubeListBuilder.create(), PartPose.offset(-1.0F, 7.4F, -1.0F));
      PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(4.0F, -6.0F, 0.0F));
      leftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(50, 16).addBox(-1.5F, -5.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1.9199F)
      );
      PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-3.0F, -5.0F, 0.0F));
      rightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 27).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.35F, 2.0F, 0.01F, 0.0F, 0.0F, 0.2618F)
      );
      PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.0F, -5.0F, 0.0F));
      leftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(16, 27).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.65F, 2.0F, 0.0F, 0.0F, 0.0F, -0.2618F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }
}
