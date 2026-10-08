package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.BabyArmadilloAnimation;
import com.futurebackport.client.model.BabyAxolotlAnimation;
import com.futurebackport.client.model.BabyRabbitAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.joml.Vector3f;

public final class KeyframeBabyRenderers26 {
   public static final ModelLayerLocation RABBIT_BABY = layer("rabbit_baby");
   public static final ModelLayerLocation ARMADILLO_BABY = layer("armadillo_baby");
   public static final ModelLayerLocation AXOLOTL_BABY = layer("axolotl_baby");
   private static final Vector3f CACHE = new Vector3f();

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id(name), "main");
   }

   private static ResourceLocation tex(String path) {
      return FutureBackport.id("textures/entity/" + path + ".png");
   }

   static void play(HierarchicalModel<?> model, AnimationDefinition clip, float seconds) {
      KeyframeAnimations.animate(model, clip, (long)(seconds * 1000.0F), 1.0F, CACHE);
   }

   private KeyframeBabyRenderers26() {
   }

   public static class Armadillo26 extends ArmadilloRenderer {
      private final MobRenderer<Armadillo, KeyframeBabyRenderers26.BabyArmadilloModel> baby;

      public Armadillo26(Context context) {
         super(context);
         ResourceLocation texture = KeyframeBabyRenderers26.tex("armadillo/armadillo_baby");
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new KeyframeBabyRenderers26.BabyArmadilloModel(context.bakeLayer(KeyframeBabyRenderers26.ARMADILLO_BABY)), 0.2F, a -> texture
         );
      }

      public void render(Armadillo armadillo, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (armadillo.isBaby()) {
            this.baby.render(armadillo, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(armadillo, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Axolotl26 extends AxolotlRenderer {
      private final MobRenderer<Axolotl, KeyframeBabyRenderers26.BabyAxolotlModel> baby;

      public Axolotl26(Context context) {
         super(context);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context,
            new KeyframeBabyRenderers26.BabyAxolotlModel(context.bakeLayer(KeyframeBabyRenderers26.AXOLOTL_BABY)),
            0.2F,
            a -> KeyframeBabyRenderers26.tex("axolotl/axolotl_" + a.getVariant().getName() + "_baby")
         );
      }

      public void render(Axolotl axolotl, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (axolotl.isBaby()) {
            this.baby.render(axolotl, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(axolotl, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class BabyArmadilloModel extends HierarchicalModel<Armadillo> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart head;
      private final ModelPart tail;
      private final ModelPart cube;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;

      public BabyArmadilloModel(ModelPart root) {
         this.root = root;
         this.body = root.getChild("body");
         this.head = this.body.getChild("head");
         this.tail = this.body.getChild("tail");
         this.cube = root.getChild("cube");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-2.5F, -2.0F, -3.5F, 5.0F, 4.0F, 7.0F, new CubeDeformation(0.3F))
               .texOffs(0, 11)
               .addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F),
            PartPose.offset(0.0F, 20.0F, 0.5F)
         );
         PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.4F));
         tail.addOrReplaceChild(
            "right_ear_cube",
            CubeListBuilder.create().texOffs(22, 11).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 1.5F, 1.0F, -1.0472F, 0.0F, 0.0F)
         );
         PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -3.2F));
         PartDefinition headCube = head.addOrReplaceChild(
            "head_cube",
            CubeListBuilder.create().texOffs(20, 17).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7417649F, 0.0F, 0.0F)
         );
         headCube.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(28, 8).mirror().addBox(-1.8F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F).mirror(false),
            PartPose.offsetAndRotation(-1.0F, -2.0F, -0.3F, -0.4363F, -0.1134F, 0.0524F)
         );
         headCube.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(28, 8).addBox(-0.2F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F),
            PartPose.offsetAndRotation(1.0F, -2.0F, -0.3F, -0.4363F, 0.1134F, -0.0524F)
         );
         root.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(20, 27).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
            PartPose.offset(-1.5F, 22.0F, 2.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(20, 27).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, 2.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.5F, 22.0F, -1.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F).mirror(false),
            PartPose.offset(-1.5F, 22.0F, -1.5F)
         );
         root.addOrReplaceChild(
            "cube",
            CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, 20.7F, 0.5F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Armadillo armadillo, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         boolean hiding = armadillo.shouldHideInShell();
         this.body.skipDraw = hiding;
         this.leftHindLeg.visible = !hiding;
         this.rightHindLeg.visible = !hiding;
         this.tail.visible = !hiding;
         this.cube.visible = hiding;
         if (!hiding) {
            this.head.xRot = Mth.clamp(headPitch, -22.5F, 25.0F) * (float) (Math.PI / 180.0);
            this.head.yRot = Mth.clamp(netHeadYaw, -32.5F, 32.5F) * (float) (Math.PI / 180.0);
            this.animateWalk(BabyArmadilloAnimation.ARMADILLO_BABY_WALK, limbSwing, limbSwingAmount, 16.5F, 2.5F);
         }

         this.animate(armadillo.rollOutAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_OUT, ageInTicks);
         this.animate(armadillo.rollUpAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_ROLL_UP, ageInTicks);
         this.animate(armadillo.peekAnimationState, BabyArmadilloAnimation.ARMADILLO_BABY_PEEK, ageInTicks);
      }
   }

   public static class BabyAxolotlModel extends HierarchicalModel<Axolotl> {
      private final ModelPart root;

      public BabyAxolotlModel(ModelPart roots) {
         this.root = roots;
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
         PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-2.0F, -0.75F, -2.75F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
               .texOffs(0, 12)
               .addBox(0.0F, -1.75F, -2.75F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -1.25F, 1.75F)
         );
         body.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(20, 16).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 0.25F, -1.25F)
         );
         PartDefinition rightLeg = body.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.25F, 1.75F, 0.0F, 1.5708F, 1.5708F)
         );
         rightLeg.addOrReplaceChild(
            "right_leg_r1",
            CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F)
         );
         body.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(20, 13).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 0.25F, -1.25F)
         );
         body.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(20, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 0.25F, 1.75F)
         );
         body.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(10, 9).addBox(0.0F, -1.5F, -1.0F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -0.25F, 3.25F)
         );
         PartDefinition head = body.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.25F, -2.75F)
         );
         head.addOrReplaceChild(
            "left_gills",
            CubeListBuilder.create().texOffs(20, 8).addBox(0.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(3.0F, -0.5F, -2.0F)
         );
         head.addOrReplaceChild(
            "right_gills",
            CubeListBuilder.create().texOffs(20, 3).addBox(-3.0F, -3.5F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, -0.5F, -2.0F)
         );
         head.addOrReplaceChild(
            "top_gills",
            CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -2.0F, -2.0F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Axolotl axolotl, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float seconds = ageInTicks / 20.0F;
         boolean moving = limbSwingAmount > 0.01F || axolotl.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
         if (axolotl.isPlayingDead()) {
            KeyframeBabyRenderers26.play(this, BabyAxolotlAnimation.BABY_AXOLOTL_PLAY_DEAD, seconds);
         } else if (axolotl.isInWaterOrBubble()) {
            if (moving) {
               if (axolotl.onGround()) {
                  KeyframeBabyRenderers26.play(this, BabyAxolotlAnimation.WALK_FLOOR_UNDERWATER, seconds);
               } else {
                  KeyframeBabyRenderers26.play(this, BabyAxolotlAnimation.BABY_AXOLOTL_SWIM, seconds);
               }
            } else {
               KeyframeBabyRenderers26.play(
                  this, axolotl.onGround() ? BabyAxolotlAnimation.IDLE_FLOOR_UNDERWATER : BabyAxolotlAnimation.IDLE_UNDERWATER, seconds
               );
            }
         } else if (moving) {
            this.animateWalk(BabyAxolotlAnimation.AXOLOTL_WALK_FLOOR, limbSwing, limbSwingAmount, 15.0F, 30.0F);
         } else {
            KeyframeBabyRenderers26.play(this, BabyAxolotlAnimation.BABY_AXOLOTL_IDLE_FLOOR, seconds);
         }
      }
   }

   public static class BabyRabbitModel extends HierarchicalModel<Rabbit> {
      private static final float TILT_PERIOD = 15.0F;
      private final ModelPart root;
      private final ModelPart head;
      private float partialTick;

      public BabyRabbitModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("body").getChild("head");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.6F));
         body.addOrReplaceChild(
            "body_r1",
            CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, -2.0F, -1.6F, -0.5236F, 0.0F, 0.0F)
         );
         PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -2.2F, 2.0F));
         tail.addOrReplaceChild(
            "tail_r1",
            CubeListBuilder.create().texOffs(0, 21).addBox(-1.4F, -2.0268F, -1.0177F, 3.0F, 3.0F, 3.0F),
            PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F)
         );
         PartDefinition head = body.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 4.0F), PartPose.offset(0.0F, -5.0F, -2.6F)
         );
         head.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(-1.5F, -3.5F, -0.5F)
         );
         head.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offset(1.5F, -3.5F, -0.5F)
         );
         PartDefinition frontLegs = body.addOrReplaceChild("frontlegs", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, -2.6F));
         PartDefinition leftFront = frontLegs.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
         );
         leftFront.addOrReplaceChild(
            "left_front_leg_r1",
            CubeListBuilder.create().texOffs(18, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
         );
         PartDefinition rightFront = frontLegs.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 1.0F, -0.5F, 0.3927F, 0.0F, 0.0F)
         );
         rightFront.addOrReplaceChild(
            "right_front_leg_r1",
            CubeListBuilder.create().texOffs(14, 8).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F)
         );
         PartDefinition backLegs = root.addOrReplaceChild("backlegs", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 2.0F));
         PartDefinition leftBack = backLegs.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
         );
         leftBack.addOrReplaceChild(
            "left_haunch",
            CubeListBuilder.create().texOffs(10, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(1.0F, 0.0F, 0.5F, 0.0F, -0.7854F, 0.0F)
         );
         PartDefinition rightBack = backLegs.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, 0.5F, 0.5F, 0.0F, 3.1416F, 0.0F)
         );
         rightBack.addOrReplaceChild(
            "right_haunch",
            CubeListBuilder.create().texOffs(0, 17).addBox(-2.0F, -0.5F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.5F, 0.0F, -0.9F, 0.0F, 0.7854F, 0.0F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(Rabbit rabbit, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Rabbit rabbit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float jump = rabbit.getJumpCompletion(this.partialTick);
         boolean idle = jump <= 0.0F && limbSwingAmount < 0.01F;
         float tiltTime = (ageInTicks + rabbit.getId() * 37) / 20.0F % 15.0F;
         boolean tilting = idle && tiltTime < 4.0F;
         if (!tilting) {
            this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
            this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         }

         if (jump > 0.0F) {
            KeyframeBabyRenderers26.play(this, BabyRabbitAnimation.HOP, jump * 0.75F);
         }

         if (tilting) {
            KeyframeBabyRenderers26.play(this, BabyRabbitAnimation.IDLE_HEAD_TILT, tiltTime);
         }
      }
   }

   public static class Rabbit26 extends RabbitRenderer {
      private final MobRenderer<Rabbit, KeyframeBabyRenderers26.BabyRabbitModel> baby;

      public Rabbit26(Context context) {
         super(context);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new KeyframeBabyRenderers26.BabyRabbitModel(context.bakeLayer(KeyframeBabyRenderers26.RABBIT_BABY)), 0.15F, r -> {
               if ("Toast".equals(ChatFormatting.stripFormatting(r.getName().getString()))) {
                  return KeyframeBabyRenderers26.tex("rabbit/rabbit_toast_baby");
               } else {
                  return KeyframeBabyRenderers26.tex("rabbit/rabbit_" + switch (r.getVariant()) {
                     case WHITE -> "white";
                     case BLACK -> "black";
                     case GOLD -> "gold";
                     case SALT -> "salt";
                     case WHITE_SPLOTCHED -> "white_splotched";
                     case EVIL -> "caerbannog";
                     default -> "brown";
                  } + "_baby");
               }
            }
         );
      }

      public void render(Rabbit rabbit, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (rabbit.isBaby()) {
            this.baby.render(rabbit, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(rabbit, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }
}
