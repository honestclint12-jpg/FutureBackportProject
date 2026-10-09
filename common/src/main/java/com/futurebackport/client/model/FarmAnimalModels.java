package com.futurebackport.client.model;

import com.futurebackport.FutureBackport;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;

public final class FarmAnimalModels {
   public static final ModelLayerLocation COW = layer("cow");
   public static final ModelLayerLocation COW_COLD = layer("cow_cold");
   public static final ModelLayerLocation COW_WARM = layer("cow_warm");
   public static final ModelLayerLocation COW_BABY = layer("cow_baby");
   public static final ModelLayerLocation PIG = layer("pig");
   public static final ModelLayerLocation PIG_COLD = layer("pig_cold");
   public static final ModelLayerLocation PIG_BABY = layer("pig_baby");
   public static final ModelLayerLocation PIG_SADDLE = layer("pig_saddle");
   public static final ModelLayerLocation CHICKEN = layer("chicken");
   public static final ModelLayerLocation CHICKEN_COLD = layer("chicken_cold");
   public static final ModelLayerLocation CHICKEN_BABY = layer("chicken_baby");

   private FarmAnimalModels() {
   }

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id(name), "main");
   }

   private static MeshDefinition cowMesh() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
            .texOffs(1, 33)
            .addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F)
            .texOffs(22, 0)
            .addBox("right_horn", -5.0F, -5.0F, -5.0F, 1, 3, 1, CubeDeformation.NONE, 22, 0)
            .texOffs(22, 0)
            .addBox("left_horn", 4.0F, -5.0F, -5.0F, 1, 3, 1, CubeDeformation.NONE, 22, 0),
         PartPose.offset(0.0F, 4.0F, -8.0F)
      );
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F).texOffs(52, 0).addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
      );
      CubeListBuilder leftLeg = CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
      CubeListBuilder rightLeg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
      root.addOrReplaceChild("right_hind_leg", rightLeg, PartPose.offset(-4.0F, 12.0F, 7.0F));
      root.addOrReplaceChild("left_hind_leg", leftLeg, PartPose.offset(4.0F, 12.0F, 7.0F));
      root.addOrReplaceChild("right_front_leg", rightLeg, PartPose.offset(-4.0F, 12.0F, -5.0F));
      root.addOrReplaceChild("left_front_leg", leftLeg, PartPose.offset(4.0F, 12.0F, -5.0F));
      return mesh;
   }

   public static LayerDefinition cow() {
      return LayerDefinition.create(cowMesh(), 64, 64);
   }

   public static LayerDefinition coldCow() {
      MeshDefinition mesh = cowMesh();
      mesh.getRoot()
         .addOrReplaceChild(
            "body",
            CubeListBuilder.create()
               .texOffs(20, 32)
               .addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.5F))
               .texOffs(18, 4)
               .addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F)
               .texOffs(52, 0)
               .addBox(-2.0F, 2.0F, -8.0F, 4.0F, 6.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
         );
      PartDefinition head = mesh.getRoot()
         .addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F).texOffs(9, 33).addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F),
            PartPose.offset(0.0F, 4.0F, -8.0F)
         );
      head.addOrReplaceChild(
         "right_horn",
         CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, -4.5F, -0.5F, 2.0F, 6.0F, 2.0F),
         PartPose.offsetAndRotation(-4.5F, -2.5F, -3.5F, 1.5708F, 0.0F, 0.0F)
      );
      head.addOrReplaceChild(
         "left_horn",
         CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -3.0F, -0.5F, 2.0F, 6.0F, 2.0F),
         PartPose.offsetAndRotation(5.5F, -2.5F, -5.0F, 1.5708F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition warmCow() {
      MeshDefinition mesh = cowMesh();
      mesh.getRoot()
         .addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
               .texOffs(1, 33)
               .addBox(-3.0F, 1.0F, -7.0F, 6.0F, 3.0F, 1.0F)
               .texOffs(27, 0)
               .addBox(-8.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F)
               .texOffs(39, 0)
               .addBox(-8.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F)
               .texOffs(27, 0)
               .mirror()
               .addBox(4.0F, -3.0F, -5.0F, 4.0F, 2.0F, 2.0F)
               .mirror(false)
               .texOffs(39, 0)
               .mirror()
               .addBox(6.0F, -5.0F, -5.0F, 2.0F, 2.0F, 2.0F)
               .mirror(false),
            PartPose.offset(0.0F, 4.0F, -8.0F)
         );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition babyCow() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 18)
            .addBox(-3.0F, -4.569F, -4.8333F, 6.0F, 6.0F, 5.0F)
            .texOffs(8, 29)
            .addBox(3.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
            .texOffs(4, 29)
            .mirror()
            .addBox(-4.0F, -5.569F, -3.8333F, 1.0F, 2.0F, 1.0F)
            .mirror(false)
            .texOffs(12, 29)
            .addBox(-2.0F, -1.569F, -5.8333F, 4.0F, 3.0F, 1.0F),
         PartPose.offset(0.0F, 13.569F, -5.1667F)
      );
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -7.0F, -1.0F, 8.0F, 6.0F, 12.0F), PartPose.offset(3.0F, 19.0F, -5.0F));
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(22, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(34, 18).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -3.5F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(22, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 3.5F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(34, 27).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 3.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   private static MeshDefinition pigMesh(CubeDeformation g) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, g)
            .texOffs(16, 16)
            .addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, g),
         PartPose.offset(0.0F, 12.0F, -6.0F)
      );
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, g),
         PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
      );
      CubeListBuilder rightLeg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, g);
      CubeListBuilder leftLeg = CubeListBuilder.create().mirror(true).texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, g);
      root.addOrReplaceChild("right_hind_leg", rightLeg, PartPose.offset(-3.0F, 18.0F, 7.0F));
      root.addOrReplaceChild("left_hind_leg", leftLeg, PartPose.offset(3.0F, 18.0F, 7.0F));
      root.addOrReplaceChild("right_front_leg", rightLeg, PartPose.offset(-3.0F, 18.0F, -5.0F));
      root.addOrReplaceChild("left_front_leg", leftLeg, PartPose.offset(3.0F, 18.0F, -5.0F));
      return mesh;
   }

   public static LayerDefinition pig() {
      return LayerDefinition.create(pigMesh(CubeDeformation.NONE), 64, 64);
   }

   public static LayerDefinition pigSaddle() {
      return LayerDefinition.create(pigMesh(new CubeDeformation(0.5F)), 64, 32);
   }

   public static LayerDefinition coldPig() {
      MeshDefinition mesh = pigMesh(CubeDeformation.NONE);
      mesh.getRoot()
         .addOrReplaceChild(
            "body",
            CubeListBuilder.create()
               .texOffs(28, 8)
               .addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F)
               .texOffs(28, 32)
               .addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, new CubeDeformation(0.5F)),
            PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
         );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition babyPig() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -4.5F, 7.0F, 6.0F, 9.0F), PartPose.offset(0.0F, 19.0F, 0.5F));
      root.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(0, 15)
            .addBox(-3.5F, -5.0F, -5.0F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.025F))
            .texOffs(6, 27)
            .addBox(-1.5F, -1.975F, -6.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.015F)),
         PartPose.offset(0.0F, 19.0F, -2.0F)
      );
      root.addOrReplaceChild(
         "left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(2.5F, 22.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "right_front_leg", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-2.5F, 22.0F, -3.0F)
      );
      root.addOrReplaceChild(
         "left_hind_leg", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(2.5F, 22.0F, 4.0F)
      );
      root.addOrReplaceChild(
         "right_hind_leg", CubeListBuilder.create().texOffs(23, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-2.5F, 22.0F, 4.0F)
      );
      return LayerDefinition.create(mesh, 32, 32);
   }

   private static MeshDefinition chickenMesh() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition head = root.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 15.0F, -4.0F)
      );
      head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
      head.addOrReplaceChild("red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F),
         PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
      );
      CubeListBuilder leg = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      root.addOrReplaceChild("right_leg", leg, PartPose.offset(-2.0F, 19.0F, 1.0F));
      root.addOrReplaceChild("left_leg", leg, PartPose.offset(1.0F, 19.0F, 1.0F));
      root.addOrReplaceChild(
         "right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(-4.0F, 13.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(4.0F, 13.0F, 0.0F)
      );
      return mesh;
   }

   public static LayerDefinition chicken() {
      return LayerDefinition.create(chickenMesh(), 64, 32);
   }

   public static LayerDefinition coldChicken() {
      MeshDefinition mesh = chickenMesh();
      mesh.getRoot()
         .addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F).texOffs(38, 9).addBox(0.0F, 3.0F, -1.0F, 0.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
         );
      PartDefinition head = mesh.getRoot()
         .addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F).texOffs(44, 0).addBox(-3.0F, -7.0F, -2.015F, 6.0F, 3.0F, 4.0F),
            PartPose.offset(0.0F, 15.0F, -4.0F)
         );
      head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
      head.addOrReplaceChild("red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 32);
   }

   public static LayerDefinition babyChicken() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F).texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F),
         PartPose.offset(0.0F, 20.25F, -1.25F)
      );
      root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
         PartPose.offset(1.0F, 22.0F, 0.5F)
      );
      root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F).texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
         PartPose.offset(-1.0F, 22.0F, 0.5F)
      );
      root.addOrReplaceChild(
         "right_wing", CubeListBuilder.create().texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(2.0F, 20.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "left_wing", CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F), PartPose.offset(-2.0F, 20.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 16, 16);
   }

   public static class ChickenModel extends HierarchicalModel<Chicken> {
      private final ModelPart root;
      private final ModelPart rightLeg;
      private final ModelPart leftLeg;
      private final ModelPart rightWing;
      private final ModelPart leftWing;
      private final ModelPart head;

      public ChickenModel(ModelPart root) {
         this.root = root;
         this.rightLeg = root.getChild("right_leg");
         this.leftLeg = root.getChild("left_leg");
         this.rightWing = root.getChild("right_wing");
         this.leftWing = root.getChild("left_wing");
         this.head = root.hasChild("head") ? root.getChild("head") : null;
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Chicken chicken, float limbSwing, float limbSwingAmount, float flapAngle, float netHeadYaw, float headPitch) {
         if (this.head != null) {
            this.head.xRot = headPitch * (float) (Math.PI / 180.0);
            this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         }

         this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.rightWing.zRot = flapAngle;
         this.leftWing.zRot = -flapAngle;
      }
   }

   public static class Quadruped<T extends Mob> extends HierarchicalModel<T> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;

      public Quadruped(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      }
   }
}
