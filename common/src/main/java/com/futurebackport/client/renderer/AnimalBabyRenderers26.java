package com.futurebackport.client.renderer;

import com.futurebackport.client.util.ArgbColor;
import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.FoxBabyAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Fox.Type;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class AnimalBabyRenderers26 {
   public static final ModelLayerLocation PANDA_BABY = layer("panda_baby");
   public static final ModelLayerLocation LLAMA_BABY = layer("llama_baby");
   public static final ModelLayerLocation LLAMA_BABY_DECOR = new ModelLayerLocation(FutureBackport.id("llama_baby"), "decor");
   public static final ModelLayerLocation BEE_BABY = layer("bee_baby");
   public static final ModelLayerLocation FOX_BABY = layer("fox_baby");

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id(name), "main");
   }

   private static ResourceLocation tex(String path) {
      return FutureBackport.id("textures/entity/" + path + ".png");
   }

   private AnimalBabyRenderers26() {
   }

   public static class BabyBeeModel extends HierarchicalModel<Bee> {
      private final ModelPart root;
      private final ModelPart bone;
      private final ModelPart stinger;
      private final ModelPart rightWing;
      private final ModelPart leftWing;
      private final ModelPart frontLeg;
      private final ModelPart midLeg;
      private final ModelPart backLeg;
      private float partialTick;

      public BabyBeeModel(ModelPart root) {
         this.root = root;
         this.bone = root.getChild("bone");
         this.stinger = this.bone.getChild("body").getChild("stinger");
         this.rightWing = this.bone.getChild("right_wing");
         this.leftWing = this.bone.getChild("left_wing");
         this.frontLeg = this.bone.getChild("front_legs");
         this.midLeg = this.bone.getChild("middle_legs");
         this.backLeg = this.bone.getChild("back_legs");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition bone = root.addOrReplaceChild(
            "bone",
            CubeListBuilder.create()
               .texOffs(6, 12)
               .addBox(1.0F, -1.6667F, -2.1633F, 1.0F, 2.0F, 2.0F)
               .texOffs(0, 12)
               .addBox(-2.0F, -1.6667F, -2.1933F, 1.0F, 2.0F, 2.0F),
            PartPose.offset(0.0F, 19.6667F, -1.8567F)
         );
         PartDefinition body = bone.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 1.3333F, 2.3567F)
         );
         body.addOrReplaceChild(
            "stinger", CubeListBuilder.create().texOffs(13, 2).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 0.5F, 2.5F)
         );
         bone.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(3, 9).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F),
            PartPose.offsetAndRotation(-1.0F, -0.6667F, 0.8567F, 0.2182F, 0.3491F, 0.0F)
         );
         bone.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(-3, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 3.0F).mirror(false),
            PartPose.offsetAndRotation(1.0F, -0.6667F, 0.8567F, 0.2182F, -0.3491F, 0.0F)
         );
         bone.addOrReplaceChild(
            "front_legs", CubeListBuilder.create().texOffs(13, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 1.8567F)
         );
         bone.addOrReplaceChild(
            "middle_legs", CubeListBuilder.create().texOffs(13, 1).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 2.8567F)
         );
         bone.addOrReplaceChild(
            "back_legs", CubeListBuilder.create().texOffs(13, 2).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F), PartPose.offset(0.0F, 3.3333F, 3.8567F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(Bee bee, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Bee bee, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.stinger.visible = !bee.hasStung();
         boolean onGround = bee.onGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7;
         if (!onGround) {
            float speed = ageInTicks * 120.32113F * (float) (Math.PI / 180.0);
            this.rightWing.yRot = 0.0F;
            this.rightWing.zRot = Mth.cos(speed) * (float) Math.PI * 0.15F;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = this.rightWing.yRot;
            this.leftWing.zRot = -this.rightWing.zRot;
            this.frontLeg.xRot = (float) (Math.PI / 4);
            this.midLeg.xRot = (float) (Math.PI / 4);
            this.backLeg.xRot = (float) (Math.PI / 4);
         }

         if (!bee.isAngry() && !onGround) {
            float bob = Mth.cos(ageInTicks * 0.18F);
            this.bone.xRot = 0.1F + bob * (float) Math.PI * 0.025F;
            this.bone.y = this.bone.y - Mth.cos(ageInTicks * 0.18F) * 0.9F;
            this.frontLeg.xRot = -bob * (float) Math.PI * 0.1F + (float) (Math.PI / 8);
            this.backLeg.xRot = -bob * (float) Math.PI * 0.05F + (float) (Math.PI / 4);
         }

         float roll = bee.getRollAmount(this.partialTick);
         if (roll > 0.0F) {
            this.bone.xRot = Mth.rotLerp(roll, this.bone.xRot, 3.0915928F);
         }
      }
   }

   public static class BabyFoxModel extends HierarchicalModel<Fox> {
      private static final float AGE_SCALE = 0.5F;
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart tail;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;
      final ModelPart head;
      private float partialTick;
      private float legMotionPos;

      public BabyFoxModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.body = root.getChild("body");
         this.tail = this.body.getChild("tail");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-3.0F, -2.125F, -5.125F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
               .texOffs(18, 20)
               .addBox(-1.0F, 0.875F, -7.125F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
               .texOffs(22, 8)
               .addBox(-3.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
               .texOffs(22, 11)
               .addBox(1.0F, -4.125F, -4.125F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 18.125F, 0.125F)
         );
         root.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-1.5F, 22.0F, 4.0F)
         );
         root.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.5F, 22.0F, 4.0F)
         );
         root.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(22, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-1.5F, 22.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.5F, 22.0F, 0.0F)
         );
         PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 20.0F, 2.0F)
         );
         body.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(0, 20).addBox(-1.5F, -1.48F, -1.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -0.5F, 3.0F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(Fox fox, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Fox fox, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.head.zRot = fox.getHeadRollAngle(this.partialTick);
         this.animateWalk(FoxBabyAnimation.FOX_BABY_WALK, limbSwing, limbSwingAmount, 1.0F, 2.5F);
         if (fox.isCrouching()) {
            float crouch = fox.getCrouchAmount(this.partialTick);
            this.body.xRot += 0.10471976F;
            this.head.y += crouch * 0.5F;
            float wiggle = Mth.cos(ageInTicks) * 0.05F;
            this.body.yRot = wiggle;
            this.rightHindLeg.zRot = wiggle;
            this.leftHindLeg.zRot = wiggle;
            this.rightFrontLeg.zRot = wiggle / 2.0F;
            this.leftFrontLeg.zRot = wiggle / 2.0F;
            this.body.y += crouch / 6.0F;
         } else if (fox.isSleeping()) {
            this.rightHindLeg.visible = this.leftHindLeg.visible = this.rightFrontLeg.visible = this.leftFrontLeg.visible = false;
            this.body.zRot = (float) (-Math.PI / 2);
            this.body.xRot = -0.17453294F;
            this.body.y++;
            this.body.z--;
            this.body.x--;
            this.tail.xRot = -2.1816616F;
            this.tail.x -= 0.7F;
            this.tail.z += 0.6F;
            this.tail.y += 0.9F;
            this.head.x -= 2.0F;
            this.head.y += 2.8F;
            this.head.z -= 4.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = 0.0F;
         } else if (fox.isSitting()) {
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
            this.body.xRot = -0.959931F;
            this.body.z -= 2.25F;
            this.body.y++;
            this.tail.y -= 0.6F;
            this.tail.z--;
            this.tail.xRot = 0.95993114F;
            this.head.y -= 0.75F;
            this.rightFrontLeg.xRot = (float) (-Math.PI / 12);
            this.leftFrontLeg.xRot = (float) (-Math.PI / 12);
            this.rightFrontLeg.z--;
            this.leftFrontLeg.z--;
            this.rightFrontLeg.x += 0.01F;
            this.leftFrontLeg.x -= 0.01F;
            this.rightHindLeg.z -= 3.75F;
            this.leftHindLeg.z -= 3.75F;
            this.rightHindLeg.x += 0.01F;
            this.leftHindLeg.x -= 0.01F;
         }

         if (!fox.isSleeping() && !fox.isFaceplanted() && !fox.isCrouching()) {
            this.head.xRot = headPitch * (float) (Math.PI / 180.0);
            this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         }

         if (fox.isSleeping()) {
            this.head.xRot = 0.0F;
            this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
            this.head.zRot = Mth.cos(ageInTicks * 0.027F) / 22.0F;
         }

         if (fox.isFaceplanted()) {
            this.legMotionPos += 0.67F;
            this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
            this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
            this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
         }
      }
   }

   public static class BabyLlamaModel extends HierarchicalModel<Llama> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;

      public BabyLlamaModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
         root.getChild("right_chest").visible = false;
         root.getChild("left_chest").visible = false;
      }

      public static LayerDefinition createBodyLayer(CubeDeformation g) {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-3.0F, -9.0F, -4.0F, 6.0F, 11.0F, 4.0F, g)
               .texOffs(0, 15)
               .addBox(-1.5F, -7.0F, -7.0F, 3.0F, 3.0F, 3.0F, g)
               .texOffs(20, 4)
               .addBox(0.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g)
               .texOffs(20, 0)
               .addBox(-2.5F, -11.0F, -3.0F, 2.0F, 2.0F, 2.0F, g),
            PartPose.offset(0.0F, 12.0F, -4.0F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, 4.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 45).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, 4.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.4F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(-2.5F, 16.5F, -3.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.6F, -0.5F, -1.5F, 3.0F, 8.0F, 3.0F, g), PartPose.offset(2.5F, 16.5F, -3.5F)
         );
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -3.0F, -8.5F, 8.0F, 6.0F, 13.0F, g), PartPose.offset(0.0F, 14.0F, 2.5F)
         );
         root.addOrReplaceChild(
            "right_chest",
            CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
            PartPose.offsetAndRotation(-8.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
         );
         root.addOrReplaceChild(
            "left_chest",
            CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, g),
            PartPose.offsetAndRotation(5.5F, 4.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Llama llama, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      }
   }

   public static class BabyPandaModel extends PandaModel<Panda> {
      private final ModelPart root;
      private float partialTick;

      public BabyPandaModel(ModelPart root) {
         super(root);
         this.root = root;
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.5F, -3.5F, -5.5F, 9.0F, 7.0F, 11.0F), PartPose.offset(0.0F, 18.5F, 2.5F)
         );
         root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-3.5F, -3.0F, -5.0F, 7.0F, 6.0F, 5.0F)
               .texOffs(24, 6)
               .addBox(-2.0F, 1.0F, -6.0F, 4.0F, 2.0F, 1.0F)
               .texOffs(24, 0)
               .addBox(-4.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F)
               .texOffs(33, 0)
               .addBox(1.5F, -4.0F, -3.5F, 3.0F, 3.0F, 1.0F),
            PartPose.offset(0.0F, 19.0F, -3.0F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, 6.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, 6.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, -1.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(12, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.0F, 22.0F, -1.5F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public void prepareMobModel(Panda panda, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Panda panda, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
         this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         if (panda.getUnhappyCounter() > 0) {
            this.head.yRot = 0.35F * Mth.sin(0.6F * ageInTicks);
            this.head.zRot = 0.35F * Mth.sin(0.6F * ageInTicks);
            this.rightFrontLeg.xRot = -0.75F * Mth.sin(0.3F * ageInTicks);
            this.leftFrontLeg.xRot = 0.75F * Mth.sin(0.3F * ageInTicks);
         }

         if (panda.isSneezing()) {
            int sneeze = panda.getSneezeCounter();
            if (sneeze < 15) {
               this.head.xRot = (float) (-Math.PI / 4) * sneeze / 14.0F;
            } else if (sneeze < 20) {
               this.head.xRot = (float) (-Math.PI / 4) + (float) (Math.PI / 4) * ((sneeze - 15) / 5);
            }
         }

         float sit = panda.getSitAmount(this.partialTick);
         if (sit > 0.0F) {
            this.body.xRot = Mth.rotLerp(sit, this.body.xRot, 0.17453294F);
            this.body.z = Mth.lerp(sit, this.body.z, -1.5F);
            this.head.z = Mth.lerp(sit, this.head.z, -11.5F);
            this.head.y = Mth.lerp(sit, this.head.y, 17.5F);
            this.rightFrontLeg.z = Mth.lerp(sit, this.rightFrontLeg.z, -5.0F);
            this.leftFrontLeg.z = Mth.lerp(sit, this.leftFrontLeg.z, -5.0F);
            this.rightHindLeg.z = Mth.lerp(sit, this.rightHindLeg.z, 3.0F);
            this.leftHindLeg.z = Mth.lerp(sit, this.leftHindLeg.z, 3.0F);
            this.rightFrontLeg.zRot = -0.27079642F;
            this.leftFrontLeg.zRot = 0.27079642F;
            this.rightHindLeg.zRot = 0.5707964F;
            this.leftHindLeg.zRot = -0.5707964F;
            if (panda.isEating()) {
               this.head.xRot = (float) (Math.PI / 2) + 0.2F * Mth.sin(ageInTicks * 0.6F);
               this.rightFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
               this.leftFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(ageInTicks * 0.6F);
            }

            if (panda.isScared()) {
               this.head.xRot = 2.1707964F;
               this.rightFrontLeg.xRot = -0.9F;
               this.leftFrontLeg.xRot = -0.9F;
            }
         }

         float lie = panda.getLieOnBackAmount(this.partialTick);
         if (lie > 0.0F) {
            this.rightHindLeg.xRot = -0.6F * Mth.sin(ageInTicks * 0.15F);
            this.leftHindLeg.xRot = 0.6F * Mth.sin(ageInTicks * 0.15F);
            this.rightFrontLeg.xRot = 0.3F * Mth.sin(ageInTicks * 0.25F);
            this.leftFrontLeg.xRot = -0.3F * Mth.sin(ageInTicks * 0.25F);
            this.head.xRot = Mth.rotLerp(lie, this.head.xRot, (float) (Math.PI / 2));
         }
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
         int color = ArgbColor.pack(red, green, blue, alpha);
         this.young = false;
         super.renderToBuffer(poseStack, buffer, light, overlay, ArgbColor.red(color), ArgbColor.green(color), ArgbColor.blue(color), ArgbColor.alpha(color));
      }
   }

   public static class Bee26 extends BeeRenderer {
      private final MobRenderer<Bee, AnimalBabyRenderers26.BabyBeeModel> baby;

      public Bee26(Context context) {
         super(context);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context,
            new AnimalBabyRenderers26.BabyBeeModel(context.bakeLayer(AnimalBabyRenderers26.BEE_BABY)),
            0.2F,
            b -> b.isAngry()
               ? (b.hasNectar() ? AnimalBabyRenderers26.tex("bee/bee_angry_nectar_baby") : AnimalBabyRenderers26.tex("bee/bee_angry_baby"))
               : (b.hasNectar() ? AnimalBabyRenderers26.tex("bee/bee_nectar_baby") : AnimalBabyRenderers26.tex("bee/bee_baby"))
         );
      }

      public void render(Bee bee, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (bee.isBaby()) {
            this.baby.render(bee, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(bee, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Fox26 extends FoxRenderer {
      private final MobRenderer<Fox, AnimalBabyRenderers26.BabyFoxModel> baby;

      public Fox26(Context context) {
         super(context);
         final Map<Type, ResourceLocation> awake = Map.of(
            Type.RED, AnimalBabyRenderers26.tex("fox/fox_baby"), Type.SNOW, AnimalBabyRenderers26.tex("fox/fox_snow_baby")
         );
         final Map<Type, ResourceLocation> asleep = Map.of(
            Type.RED, AnimalBabyRenderers26.tex("fox/fox_sleep_baby"), Type.SNOW, AnimalBabyRenderers26.tex("fox/fox_snow_sleep_baby")
         );
         this.baby = new MobRenderer<Fox, AnimalBabyRenderers26.BabyFoxModel>(
            context, new AnimalBabyRenderers26.BabyFoxModel(context.bakeLayer(AnimalBabyRenderers26.FOX_BABY)), 0.2F
         ) {
            public ResourceLocation getTextureLocation(Fox fox) {
               return (fox.isSleeping() ? asleep : awake).get(fox.getVariant());
            }

            protected void setupRotations(Fox fox, PoseStack poseStack, float bob, float yBodyRot, float partialTick) {
               super.setupRotations(fox, poseStack, bob, yBodyRot, partialTick);
               if (fox.isPouncing() || fox.isFaceplanted()) {
                  poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, fox.xRotO, fox.getXRot())));
               }
            }
         };
         this.baby.addLayer(new AnimalBabyRenderers26.KitHeldItemLayer(this.baby, context.getItemInHandRenderer()));
      }

      public void render(Fox fox, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (fox.isBaby()) {
            this.baby.render(fox, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(fox, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   private static class KitHeldItemLayer extends RenderLayer<Fox, AnimalBabyRenderers26.BabyFoxModel> {
      private final ItemInHandRenderer itemRenderer;

      KitHeldItemLayer(RenderLayerParent<Fox, AnimalBabyRenderers26.BabyFoxModel> parent, ItemInHandRenderer itemRenderer) {
         super(parent);
         this.itemRenderer = itemRenderer;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Fox fox,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         ItemStack item = fox.getItemBySlot(EquipmentSlot.MAINHAND);
         if (!item.isEmpty()) {
            boolean sleeping = fox.isSleeping();
            ModelPart head = ((AnimalBabyRenderers26.BabyFoxModel)this.getParentModel()).head;
            poseStack.pushPose();
            poseStack.translate(head.x / 16.0F, head.y / 16.0F, head.z / 16.0F);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.mulPose(Axis.ZP.rotation(fox.getHeadRollAngle(partialTick)));
            poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
            if (sleeping) {
               poseStack.translate(0.4F, 0.26F, 0.15F);
            } else {
               poseStack.translate(0.06F, 0.26F, -0.5F);
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (sleeping) {
               poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            this.itemRenderer.renderItem(fox, item, ItemDisplayContext.GROUND, false, poseStack, buffers, light);
            poseStack.popPose();
         }
      }
   }

   public static class Llama26 extends LlamaRenderer {
      private final MobRenderer<Llama, AnimalBabyRenderers26.BabyLlamaModel> baby;

      public Llama26(Context context, ModelLayerLocation adultLayer) {
         super(context, adultLayer);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new AnimalBabyRenderers26.BabyLlamaModel(context.bakeLayer(AnimalBabyRenderers26.LLAMA_BABY)), 0.35F, l -> {
               return switch (l.getVariant()) {
                  case WHITE -> AnimalBabyRenderers26.tex("llama/llama_white_baby");
                  case BROWN -> AnimalBabyRenderers26.tex("llama/llama_brown_baby");
                  case GRAY -> AnimalBabyRenderers26.tex("llama/llama_gray_baby");
                  default -> AnimalBabyRenderers26.tex("llama/llama_creamy_baby");
               };
            }
         );
         this.baby
            .addLayer(
               new AnimalBabyRenderers26.TraderDecorLayer(
                  this.baby, new AnimalBabyRenderers26.BabyLlamaModel(context.bakeLayer(AnimalBabyRenderers26.LLAMA_BABY_DECOR))
               )
            );
      }

      public void render(Llama llama, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (llama.isBaby()) {
            this.baby.render(llama, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(llama, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Panda26 extends PandaRenderer {
      private final PandaModel<Panda> adult = (PandaModel<Panda>)this.model;
      private final AnimalBabyRenderers26.BabyPandaModel baby;

      public Panda26(Context context) {
         super(context);
         this.baby = new AnimalBabyRenderers26.BabyPandaModel(context.bakeLayer(AnimalBabyRenderers26.PANDA_BABY));
      }

      public void render(Panda panda, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = panda.isBaby() ? this.baby : this.adult;
         super.render(panda, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(Panda panda) {
         if (!panda.isBaby()) {
            return super.getTextureLocation(panda);
         } else {
            return switch (panda.getVariant()) {
               case LAZY -> AnimalBabyRenderers26.tex("panda/lazy_panda_baby");
               case WORRIED -> AnimalBabyRenderers26.tex("panda/worried_panda_baby");
               case PLAYFUL -> AnimalBabyRenderers26.tex("panda/playful_panda_baby");
               case BROWN -> AnimalBabyRenderers26.tex("panda/brown_panda_baby");
               case WEAK -> AnimalBabyRenderers26.tex("panda/weak_panda_baby");
               case AGGRESSIVE -> AnimalBabyRenderers26.tex("panda/aggressive_panda_baby");
               default -> AnimalBabyRenderers26.tex("panda/panda_baby");
            };
         }
      }
   }

   private static class TraderDecorLayer extends RenderLayer<Llama, AnimalBabyRenderers26.BabyLlamaModel> {
      private static final ResourceLocation TEXTURE = AnimalBabyRenderers26.tex("equipment/llama_body/trader_llama_baby");
      private final AnimalBabyRenderers26.BabyLlamaModel model;

      TraderDecorLayer(RenderLayerParent<Llama, AnimalBabyRenderers26.BabyLlamaModel> parent, AnimalBabyRenderers26.BabyLlamaModel model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Llama llama,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (llama.isTraderLlama() && !llama.isInvisible()) {
            this.model.setupAnim(llama, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}
