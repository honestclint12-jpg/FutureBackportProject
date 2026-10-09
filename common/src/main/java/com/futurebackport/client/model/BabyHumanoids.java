package com.futurebackport.client.model;

import com.futurebackport.client.util.ArgbColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;

public final class BabyHumanoids {
   private BabyHumanoids() {
   }

   static ModelPart[] parts(HumanoidModel<?> model) {
      return new ModelPart[]{model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
   }

   public static void remap(HumanoidModel<?> model, BabyHumanoids.Skeleton baby) {
      ModelPart[] parts = parts(model);
      float[] a = BabyHumanoids.Skeleton.ADULT.positions();
      float[] b = baby.positions();

      for (int i = 0; i < parts.length; i++) {
         ModelPart p = parts[i];
         p.x = b[i * 3] + (p.x - a[i * 3]) * 0.5F;
         p.y = b[i * 3 + 1] + (p.y - a[i * 3 + 1]) * 0.5F;
         p.z = b[i * 3 + 2] + (p.z - a[i * 3 + 2]) * 0.5F;
      }

      model.hat.copyFrom(model.head);
   }

   static void translateToBabyHand(HumanoidModel<?> model, HumanoidArm arm, PoseStack poseStack, Runnable toHand) {
      if (model.young) {
         poseStack.scale(2.0F, 2.0F, 2.0F);
         poseStack.translate(0.0F, -0.75F, 0.0F);
      }

      toHand.run();
      if (model.young) {
         poseStack.scale(0.5F, 0.5F, 0.5F);
      }
   }

   static void renderUnscaled(Iterable<ModelPart> head, Iterable<ModelPart> body, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
      head.forEach(p -> p.render(poseStack, buffer, light, overlay, ArgbColor.red(color), ArgbColor.green(color), ArgbColor.blue(color), ArgbColor.alpha(color)));
      body.forEach(p -> p.render(poseStack, buffer, light, overlay, ArgbColor.red(color), ArgbColor.green(color), ArgbColor.blue(color), ArgbColor.alpha(color)));
   }

   public static LayerDefinition zombieLayer(CubeDeformation g) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, g), PartPose.offset(0.0F, 17.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(3, 3)
            .addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(35, 3)
            .addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)),
         PartPose.offset(0.0F, 15.25F, 0.0F)
      );
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 15.25F, 0.0F));
      root.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(36, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(-3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(28, 16).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, g), PartPose.offset(3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(-1.0F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, g), PartPose.offset(1.0F, 20.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition zombieVillagerLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 15)
            .addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F)
            .texOffs(16, 22)
            .addBox(-2.0F, -2.75F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, 18.75F, 0.0F)
      );
      PartDefinition head = root.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F), PartPose.offset(0.0F, 16.0F, 0.0F)
      );
      head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.0F, -4.0F));
      PartDefinition hat = root.addOrReplaceChild(
         "hat",
         CubeListBuilder.create().texOffs(0, 31).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)),
         PartPose.offset(0.0F, 16.0F, 0.0F)
      );
      hat.addOrReplaceChild(
         "hat_rim", CubeListBuilder.create().texOffs(0, 46).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F), PartPose.offset(0.0F, -4.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(24, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(3.0F, 15.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.0F, 21.5F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.0F, 21.5F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition piglinLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 18.0F, -0.5F));
      PartDefinition head = root.addOrReplaceChild(
         "head",
         CubeListBuilder.create().texOffs(21, 30).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 3.0F, 1.0F).texOffs(0, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 6.0F, 7.0F),
         PartPose.offset(0.0F, 15.0F, 0.0F)
      );
      PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.2F, -4.0F, 0.0F));
      leftEar.addOrReplaceChild(
         "left_ear_r1",
         CubeListBuilder.create().texOffs(0, 21).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(1.0F, 1.75F, 0.0F, 0.0F, 0.0F, -0.6109F)
      );
      PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.2F, -4.0F, 0.0F));
      rightEar.addOrReplaceChild(
         "right_ear_r1",
         CubeListBuilder.create().texOffs(18, 13).addBox(-0.5F, -3.0F, -2.0F, 1.0F, 6.0F, 4.0F),
         PartPose.offsetAndRotation(-1.0F, 1.75F, 0.0F, 0.0F, 0.0F, 0.6109F)
      );
      root.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(28, 13).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(4.0F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(10, 30).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F), PartPose.offset(-4.0F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(22, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(-1.5F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(10, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(1.5F, 20.0F, 0.0F)
      );

      for (String empty : new String[]{"hat", "ear", "cloak", "left_sleeve", "right_sleeve", "left_pants", "right_pants", "jacket"}) {
         root.addOrReplaceChild(empty, CubeListBuilder.create(), PartPose.ZERO);
      }

      return LayerDefinition.create(mesh, 64, 64);
   }

   public static class Drowned26<T extends Drowned> extends DrownedModel<T> {
      public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
         BabyHumanoids.translateToBabyHand(this, arm, poseStack, () -> super.translateToHand(arm, poseStack));
      }

      public Drowned26(ModelPart root) {
         super(root);
      }

      public void setupAnim(T drowned, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(drowned, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         BabyHumanoids.remap(this, BabyHumanoids.Skeleton.ZOMBIE);
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
         int color = ArgbColor.pack(red, green, blue, alpha);
         BabyHumanoids.renderUnscaled(this.headParts(), this.bodyParts(), poseStack, buffer, light, overlay, color);
      }
   }

   public static class Piglin26<T extends Mob> extends PiglinModel<T> {
      private static final float EAR_ANGLE_DIFFERENCE = 0.43633235F;

      public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
         BabyHumanoids.translateToBabyHand(this, arm, poseStack, () -> super.translateToHand(arm, poseStack));
      }

      public Piglin26(ModelPart root) {
         super(root);
      }

      public void setupAnim(T piglin, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(piglin, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         BabyHumanoids.remap(this, BabyHumanoids.Skeleton.PIGLIN);
         this.head.getChild("left_ear").zRot += 0.43633235F;
         this.head.getChild("right_ear").zRot -= 0.43633235F;
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
         int color = ArgbColor.pack(red, green, blue, alpha);
         BabyHumanoids.renderUnscaled(this.headParts(), this.bodyParts(), poseStack, buffer, light, overlay, color);
      }
   }

   public record Skeleton(float[] positions) {
      static final BabyHumanoids.Skeleton ADULT = new BabyHumanoids.Skeleton(
         new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -5.0F, 2.0F, 0.0F, 5.0F, 2.0F, 0.0F, -1.9F, 12.0F, 0.0F, 1.9F, 12.0F, 0.0F}
      );
      public static final BabyHumanoids.Skeleton ZOMBIE = new BabyHumanoids.Skeleton(
         new float[]{0.0F, 15.25F, 0.0F, 0.0F, 17.5F, 0.0F, -3.0F, 15.5F, 0.0F, 3.0F, 15.5F, 0.0F, -1.0F, 20.0F, 0.0F, 1.0F, 20.0F, 0.0F}
      );
      public static final BabyHumanoids.Skeleton ZOMBIE_VILLAGER = new BabyHumanoids.Skeleton(
         new float[]{0.0F, 16.0F, 0.0F, 0.0F, 18.75F, 0.0F, -3.0F, 15.5F, 0.0F, 3.0F, 15.5F, 0.0F, -1.0F, 21.5F, 0.0F, 1.0F, 21.5F, 0.0F}
      );
      public static final BabyHumanoids.Skeleton PIGLIN = new BabyHumanoids.Skeleton(
         new float[]{0.0F, 15.0F, 0.0F, 0.0F, 18.0F, -0.5F, -4.0F, 15.0F, 0.0F, 4.0F, 15.0F, 0.0F, -1.5F, 20.0F, 0.0F, 1.5F, 20.0F, 0.0F}
      );
   }

   public static class Zombie26<T extends Zombie> extends ZombieModel<T> {
      public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
         BabyHumanoids.translateToBabyHand(this, arm, poseStack, () -> super.translateToHand(arm, poseStack));
      }

      public Zombie26(ModelPart root) {
         super(root);
      }

      public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         BabyHumanoids.remap(this, BabyHumanoids.Skeleton.ZOMBIE);
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
         int color = ArgbColor.pack(red, green, blue, alpha);
         BabyHumanoids.renderUnscaled(this.headParts(), this.bodyParts(), poseStack, buffer, light, overlay, color);
      }
   }

   public static class ZombieVillager26<T extends ZombieVillager> extends ZombieVillagerModel<T> {
      public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
         BabyHumanoids.translateToBabyHand(this, arm, poseStack, () -> super.translateToHand(arm, poseStack));
      }

      public ZombieVillager26(ModelPart root) {
         super(root);
      }

      public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         BabyHumanoids.remap(this, BabyHumanoids.Skeleton.ZOMBIE_VILLAGER);
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
         int color = ArgbColor.pack(red, green, blue, alpha);
         BabyHumanoids.renderUnscaled(this.headParts(), this.bodyParts(), poseStack, buffer, light, overlay, color);
      }
   }
}
