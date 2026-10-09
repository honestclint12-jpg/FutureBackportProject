package com.futurebackport.client.model;

import com.futurebackport.entity.nautilus.AbstractNautilus;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class NautilusModel<T extends AbstractNautilus> extends HierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart corals;

   public NautilusModel(ModelPart roots) {
      this.root = roots;
      ModelPart nautilus = roots.getChild("root");
      this.body = nautilus.getChild("body");
      ModelPart shell = nautilus.getChild("shell");
      this.corals = shell.hasChild("corals") ? shell.getChild("corals") : null;
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T nautilus, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.root.getAllParts().forEach(ModelPart::resetPose);
      this.body.yRot = Mth.clamp(netHeadYaw, -10.0F, 10.0F) * (float) (Math.PI / 180.0);
      this.body.xRot = Mth.clamp(headPitch, -10.0F, 10.0F) * (float) (Math.PI / 180.0);
      this.animateWalk(NautilusAnimation.SWIMMING, limbSwing + ageInTicks / 5.0F, limbSwingAmount + 0.2F, 2.0F, 3.0F);
      if (this.corals != null) {
         this.corals.visible = nautilus.getBodyArmorItem().isEmpty();
      }
   }

   public static MeshDefinition createBodyMesh() {
      return createBodyMesh(0.0F);
   }

   private static MeshDefinition createBodyMesh(float shellInflate) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition nautilus = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 29.0F, -6.0F));
      nautilus.addOrReplaceChild(
         "shell",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F, new CubeDeformation(shellInflate))
            .texOffs(0, 26)
            .addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 20.0F, new CubeDeformation(shellInflate))
            .texOffs(48, 26)
            .addBox(-7.0F, 0.0F, 6.0F, 14.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -13.0F, 5.0F)
      );
      PartDefinition body = nautilus.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 54)
            .addBox(-5.0F, -4.51F, -3.0F, 10.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(0, 76)
            .addBox(-5.0F, -4.51F, 7.0F, 10.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -8.5F, 12.3F)
      );
      body.addOrReplaceChild(
         "upper_mouth",
         CubeListBuilder.create().texOffs(54, 54).addBox(-5.0F, -2.0F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)),
         PartPose.offset(0.0F, -2.51F, 7.0F)
      );
      body.addOrReplaceChild(
         "inner_mouth",
         CubeListBuilder.create().texOffs(54, 70).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -0.51F, 7.5F)
      );
      body.addOrReplaceChild(
         "lower_mouth",
         CubeListBuilder.create().texOffs(54, 62).addBox(-5.0F, -1.98F, 0.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(-0.001F)),
         PartPose.offset(0.0F, 1.49F, 7.0F)
      );
      return mesh;
   }

   public static LayerDefinition createBodyLayer() {
      return LayerDefinition.create(createBodyMesh(), 128, 128);
   }

   public static LayerDefinition createArmorLayer() {
      return LayerDefinition.create(createBodyMesh(0.01F), 128, 128);
   }

   public static LayerDefinition createSaddleLayer() {
      MeshDefinition mesh = createBodyMesh();
      PartDefinition nautilus = mesh.getRoot().getChild("root");
      nautilus.addOrReplaceChild(
         "shell",
         CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 16.0F, new CubeDeformation(0.2F)),
         PartPose.offset(0.0F, -13.0F, 5.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }

   public static LayerDefinition createBabyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition nautilus = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(-0.5F, 28.0F, -0.5F));
      nautilus.addOrReplaceChild(
         "shell",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-6.0F, -4.0F, -1.0F, 7.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(0, 11)
            .addBox(-6.0F, 0.0F, -1.0F, 7.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
            .texOffs(23, 11)
            .addBox(-6.0F, 0.0F, 5.0F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(3.0F, -8.0F, -2.0F)
      );
      PartDefinition body = nautilus.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(0, 24)
            .addBox(-2.5F, -3.01F, -1.0F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(0, 35)
            .addBox(-2.5F, -3.01F, 4.1F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.5F, -5.0F, 3.0F)
      );
      body.addOrReplaceChild(
         "upper_mouth",
         CubeListBuilder.create().texOffs(24, 24).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.001F)),
         PartPose.offset(0.0F, -2.01F, 3.9F)
      );
      body.addOrReplaceChild(
         "inner_mouth",
         CubeListBuilder.create().texOffs(24, 32).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -1.01F, 4.9F)
      );
      body.addOrReplaceChild(
         "lower_mouth",
         CubeListBuilder.create().texOffs(24, 28).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(-0.001F)),
         PartPose.offset(0.0F, -0.01F, 3.9F)
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   public static LayerDefinition createCoralLayer() {
      MeshDefinition mesh = createBodyMesh();
      PartDefinition corals = mesh.getRoot()
         .getChild("root")
         .getChild("shell")
         .addOrReplaceChild("corals", CubeListBuilder.create(), PartPose.offset(8.0F, 4.5F, -8.0F));
      PartDefinition yellow = corals.addOrReplaceChild("yellow_coral", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 11.0F));
      yellow.addOrReplaceChild(
         "yellow_coral_second",
         CubeListBuilder.create().texOffs(0, 85).addBox(-4.5F, -3.5F, 0.0F, 6.0F, 8.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, -0.7854F, 0.0F)
      );
      yellow.addOrReplaceChild(
         "yellow_coral_first",
         CubeListBuilder.create().texOffs(0, 85).addBox(-4.5F, -3.5F, 0.0F, 6.0F, 8.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition pink = corals.addOrReplaceChild(
         "pink_coral", CubeListBuilder.create().texOffs(-8, 94).addBox(-4.5F, 4.5F, 0.0F, 6.0F, 0.0F, 8.0F), PartPose.offset(-12.5F, -18.0F, 11.0F)
      );
      pink.addOrReplaceChild(
         "pink_coral_second",
         CubeListBuilder.create().texOffs(-8, 94).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 0.0F, 8.0F),
         PartPose.offsetAndRotation(-1.5F, 4.5F, 4.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition blue = corals.addOrReplaceChild("blue_coral", CubeListBuilder.create(), PartPose.offset(-14.0F, 0.0F, 5.5F));
      blue.addOrReplaceChild(
         "blue_second",
         CubeListBuilder.create().texOffs(0, 102).addBox(-3.5F, -5.5F, 0.0F, 5.0F, 10.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.0F, 0.7854F, 0.0F)
      );
      blue.addOrReplaceChild(
         "blue_first",
         CubeListBuilder.create().texOffs(0, 102).addBox(-3.5F, -5.5F, 0.0F, 5.0F, 10.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition red = corals.addOrReplaceChild("red_coral", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      red.addOrReplaceChild(
         "red_coral_second",
         CubeListBuilder.create().texOffs(0, 112).addBox(-2.5F, -5.5F, 0.0F, 4.0F, 10.0F, 0.0F),
         PartPose.offsetAndRotation(-0.5F, -1.0F, 1.5F, 0.0F, -0.829F, 0.0F)
      );
      red.addOrReplaceChild(
         "red_coral_first",
         CubeListBuilder.create().texOffs(0, 112).addBox(-4.5F, -5.5F, 0.0F, 6.0F, 10.0F, 0.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      return LayerDefinition.create(mesh, 128, 128);
   }
}
