package com.futurebackport.client.model;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.HappyGhast;
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
import net.minecraft.world.entity.EquipmentSlot;

public final class HappyGhastModels {
   public static final ModelLayerLocation GHAST = new ModelLayerLocation(FutureBackport.id("happy_ghast"), "main");
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("happy_ghast_baby"), "main");
   public static final ModelLayerLocation HARNESS = new ModelLayerLocation(FutureBackport.id("happy_ghast"), "harness");
   private static final int[] TENTACLE_LENGTHS = new int[]{5, 7, 4, 5, 5, 7, 8, 8, 5};
   private static final float[][] TENTACLE_POSITIONS = new float[][]{
      {-3.75F, -5.0F}, {1.25F, -5.0F}, {6.25F, -5.0F}, {-6.25F, 0.0F}, {-1.25F, 0.0F}, {3.75F, 0.0F}, {-3.75F, 5.0F}, {1.25F, 5.0F}, {6.25F, 5.0F}
   };

   private HappyGhastModels() {
   }

   public static LayerDefinition body(boolean baby) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition body = mesh.getRoot()
         .addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
      if (baby) {
         body.addOrReplaceChild(
            "inner_body",
            CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-0.5F)),
            PartPose.offset(0.0F, 8.0F, 0.0F)
         );
      }

      for (int i = 0; i < 9; i++) {
         body.addOrReplaceChild(
            "tentacle" + i,
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, TENTACLE_LENGTHS[i], 2.0F),
            PartPose.offset(TENTACLE_POSITIONS[i][0], 7.0F, TENTACLE_POSITIONS[i][1])
         );
      }

      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition harness() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "harness", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "goggles",
         CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -2.5F, -2.5F, 16.0F, 5.0F, 5.0F, new CubeDeformation(0.15F)),
         PartPose.offset(0.0F, 14.0F, -5.5F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static class GhastModel extends HierarchicalModel<HappyGhast> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart[] tentacles = new ModelPart[9];

      public GhastModel(ModelPart root) {
         this.root = root;
         this.body = root.getChild("body");

         for (int i = 0; i < 9; i++) {
            this.tentacles[i] = this.body.getChild("tentacle" + i);
         }
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(HappyGhast ghast, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         float squeeze = ghast.getBodyItem().isEmpty() ? 1.0F : 0.9375F;
         this.body.xScale = squeeze;
         this.body.yScale = squeeze;
         this.body.zScale = squeeze;

         for (int i = 0; i < this.tentacles.length; i++) {
            this.tentacles[i].xRot = 0.2F * Mth.sin(ageInTicks * 0.3F + i) + 0.4F;
         }
      }
   }

   public static class HarnessModel extends HierarchicalModel<HappyGhast> {
      private final ModelPart root;
      private final ModelPart goggles;

      public HarnessModel(ModelPart root) {
         this.root = root;
         this.goggles = root.getChild("goggles");
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(HappyGhast ghast, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         if (ghast.isVehicle()) {
            this.goggles.xRot = 0.0F;
            this.goggles.y = 14.0F;
         } else {
            this.goggles.xRot = -0.7854F;
            this.goggles.y = 9.0F;
         }
      }
   }
}
