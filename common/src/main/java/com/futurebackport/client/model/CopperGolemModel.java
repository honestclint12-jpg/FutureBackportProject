package com.futurebackport.client.model;

import com.futurebackport.entity.CopperGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;

public class CopperGolemModel extends HierarchicalModel<CopperGolem> implements ArmedModel, HeadedModel {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private CopperGolem.State state = CopperGolem.State.IDLE;

   public CopperGolemModel(ModelPart roots) {
      this.root = roots.getChild("root");
      this.body = this.root.getChild("body");
      this.head = this.body.getChild("head");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition body = root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -6.0F, -3.0F, 8.0F, 6.0F, 6.0F, CubeDeformation.NONE),
         PartPose.offset(0.0F, -5.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -5.0F, -5.0F, 8.0F, 5.0F, 10.0F, new CubeDeformation(0.015F))
            .texOffs(56, 0)
            .addBox(-1.0F, -2.0F, -6.0F, 2.0F, 3.0F, 2.0F, CubeDeformation.NONE)
            .texOffs(37, 8)
            .addBox(-1.0F, -9.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.015F))
            .texOffs(37, 0)
            .addBox(-2.0F, -13.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.015F)),
         PartPose.offset(0.0F, -6.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(36, 16).addBox(-3.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offset(-4.0F, -6.0F, 0.0F)
      );
      body.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(50, 16).addBox(0.0F, -1.0F, -2.0F, 3.0F, 10.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offset(4.0F, -6.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 27).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offset(0.0F, -5.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(16, 27).addBox(0.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE),
         PartPose.offset(0.0F, -5.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(CopperGolem golem, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      this.state = golem.getState();
      this.head.xRot = headPitch * (float) (Math.PI / 180.0);
      this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
      if (golem.getMainHandItem().isEmpty() && golem.getOffhandItem().isEmpty()) {
         this.animateWalk(CopperGolemAnimation.COPPER_GOLEM_WALK, limbSwing, limbSwingAmount, 2.0F, 2.5F);
      } else {
         this.animateWalk(CopperGolemAnimation.COPPER_GOLEM_WALK_ITEM, limbSwing, limbSwingAmount, 2.0F, 2.5F);
         this.poseHeldItemArmsIfStill();
      }

      this.animate(golem.idleAnimationState, CopperGolemAnimation.COPPER_GOLEM_IDLE, ageInTicks);
      this.animate(golem.interactionGetItemAnimationState, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_GET, ageInTicks);
      this.animate(golem.interactionGetNoItemAnimationState, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_NOITEM_NOGET, ageInTicks);
      this.animate(golem.interactionDropItemAnimationState, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_DROP, ageInTicks);
      this.animate(golem.interactionDropNoItemAnimationState, CopperGolemAnimation.COPPER_GOLEM_CHEST_INTERACTION_ITEM_NODROP, ageInTicks);
   }

   private void poseHeldItemArmsIfStill() {
      this.rightArm.xRot = Math.min(this.rightArm.xRot, -0.87266463F);
      this.leftArm.xRot = Math.min(this.leftArm.xRot, -0.87266463F);
      this.rightArm.yRot = Math.min(this.rightArm.yRot, -0.1134464F);
      this.leftArm.yRot = Math.max(this.leftArm.yRot, 0.1134464F);
      this.rightArm.zRot = Math.min(this.rightArm.zRot, -0.064577185F);
      this.leftArm.zRot = Math.max(this.leftArm.zRot, 0.064577185F);
   }

   public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
      this.root.translateAndRotate(poseStack);
      this.body.translateAndRotate(poseStack);
      (arm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm).translateAndRotate(poseStack);
      if (this.state == CopperGolem.State.IDLE) {
         poseStack.mulPose(Axis.YP.rotationDegrees(arm == HumanoidArm.RIGHT ? -90.0F : 90.0F));
         poseStack.translate(0.0F, 0.0F, 0.125F);
      } else {
         poseStack.scale(0.55F, 0.55F, 0.55F);
         poseStack.translate(-0.125F, 0.3125F, -0.1875F);
      }
   }

   public ModelPart getHead() {
      return this.head;
   }
}
