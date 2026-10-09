package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.CamelBabyAnimation;
import com.futurebackport.client.model.FarmAnimalModels;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.ChestedHorseRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.MushroomCow.MushroomType;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;

public final class MountBabyRenderers26 {
   public static final ModelLayerLocation HORSE_BABY = layer("horse_baby");
   public static final ModelLayerLocation DONKEY_BABY = layer("donkey_baby");
   public static final ModelLayerLocation CAMEL_BABY = layer("camel_baby");

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id(name), "main");
   }

   private static ResourceLocation tex(String path) {
      return FutureBackport.id("textures/entity/" + path + ".png");
   }

   private MountBabyRenderers26() {
   }

   public static AgeSwitchRenderer<Horse> horse(Context context) {
      MobRenderer<Horse, MountBabyRenderers26.BabyEquineModel<Horse>> baby = NetherBabyRenderers26.babyRenderer(
         context, new MountBabyRenderers26.BabyEquineModel<>(context.bakeLayer(HORSE_BABY), false), 0.4F, h -> {
            return tex("horse/horse_" + switch (h.getVariant()) {
               case WHITE -> "white";
               case CREAMY -> "creamy";
               case CHESTNUT -> "chestnut";
               case BROWN -> "brown";
               case BLACK -> "black";
               case GRAY -> "gray";
               case DARK_BROWN -> "darkbrown";
               default -> throw new IllegalStateException();
            } + "_baby");
         }
      );
      baby.addLayer(new MountBabyRenderers26.MarkingLayer(baby));
      return new AgeSwitchRenderer(context, new HorseRenderer(context), 0.75F, baby, 0.4F);
   }

   public static class BabyCamelModel extends HierarchicalModel<Camel> {
      private final ModelPart root;
      private final ModelPart head;
      private float partialTick;

      public BabyCamelModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("body").getChild("head");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.5F, -4.0F, -8.0F, 9.0F, 8.0F, 16.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(50, 38).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 9.0F, 0.0F), PartPose.offset(0.0F, -1.5F, 8.05F)
         );
         PartDefinition head = body.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(20, 0)
               .addBox(-2.5F, -3.0F, -7.5F, 5.0F, 5.0F, 7.0F)
               .texOffs(0, 0)
               .addBox(-2.5F, -12.0F, -7.5F, 5.0F, 9.0F, 5.0F)
               .texOffs(0, 14)
               .addBox(-2.5F, -12.0F, -10.5F, 5.0F, 4.0F, 3.0F),
            PartPose.offset(0.0F, 1.0F, -7.5F)
         );
         head.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(37, 0).addBox(-3.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -11.0F, -4.0F)
         );
         head.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(47, 0).addBox(0.0F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -11.0F, -4.0F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(36, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, -5.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(48, 14).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, -5.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(3.0F, 11.5F, 5.5F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 13.0F, 3.0F), PartPose.offset(-3.0F, 11.5F, 5.5F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(Camel camel, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Camel camel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float yRot = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
         float xRot = Mth.clamp(headPitch, -25.0F, 45.0F);
         if (camel.getJumpCooldown() > 0) {
            xRot = Mth.clamp(xRot + 45.0F * (camel.getJumpCooldown() - this.partialTick) / 55.0F, -25.0F, 70.0F);
         }

         this.head.yRot = yRot * (float) (Math.PI / 180.0);
         this.head.xRot = xRot * (float) (Math.PI / 180.0);
         if (camel.isCamelVisuallySitting()) {
            this.animate(camel.sitAnimationState, CamelBabyAnimation.CAMEL_BABY_SIT, ageInTicks);
            this.animate(camel.sitPoseAnimationState, CamelBabyAnimation.CAMEL_BABY_SIT_POSE, ageInTicks);
         } else {
            this.animateWalk(CamelBabyAnimation.CAMEL_BABY_WALK, limbSwing, limbSwingAmount, 2.0F, 2.5F);
         }

         this.animate(camel.sitUpAnimationState, CamelBabyAnimation.CAMEL_BABY_STANDUP, ageInTicks);
         this.animate(camel.idleAnimationState, CamelBabyAnimation.CAMEL_BABY_IDLE, ageInTicks);
         this.animate(camel.dashAnimationState, CamelBabyAnimation.CAMEL_BABY_DASH, ageInTicks);
      }
   }

   public static class BabyEquineModel<T extends AbstractHorse> extends HierarchicalModel<T> {
      private static final float AGE_SCALE = 0.5F;
      private final boolean donkey;
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart headParts;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;
      private final ModelPart tail;
      private float partialTick;

      public BabyEquineModel(ModelPart root, boolean donkey) {
         this.root = root;
         this.donkey = donkey;
         this.body = root.getChild("body");
         ModelPart legs = donkey ? this.body : root;
         this.headParts = legs.getChild("head_parts");
         this.rightHindLeg = legs.getChild("right_hind_leg");
         this.leftHindLeg = legs.getChild("left_hind_leg");
         this.rightFrontLeg = legs.getChild("right_front_leg");
         this.leftFrontLeg = legs.getChild("left_front_leg");
         this.tail = this.body.getChild("tail");
      }

      public static LayerDefinition createHorseLayer() {
         CubeDeformation g = CubeDeformation.NONE;
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -3.5F, -7.0F, 8.0F, 7.0F, 14.0F, g), PartPose.offset(0.0F, 12.5F, 0.0F)
         );
         body.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(24, 34).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F, g),
            PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.7418F, 0.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, 5.4F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, 5.4F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(2.4F, 16.0F, -5.4F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F, g), PartPose.offset(-2.4F, 16.0F, -5.4F)
         );
         PartDefinition neck = root.addOrReplaceChild(
            "head_parts",
            CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F, g),
            PartPose.offsetAndRotation(0.0F, 10.0F, -6.0F, 0.6109F, 0.0F, 0.0F)
         );
         PartDefinition head = neck.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.9484F, -6.705F, 6.0F, 4.0F, 9.0F, g), PartPose.offset(0.0F, -6.0516F, -0.2951F)
         );
         head.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -2.5F, -0.8F, 2.0F, 3.0F, 1.0F, g),
            PartPose.offsetAndRotation(2.0F, -4.2484F, 1.9451F, 0.0F, 0.0F, 0.2618F)
         );
         head.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 3.0F, 1.0F, g),
            PartPose.offsetAndRotation(-2.0F, -4.2484F, 1.645F, 0.0F, 0.0F, -0.2618F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public static LayerDefinition createDonkeyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -3.0F, -7.0F, 8.0F, 6.0F, 14.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.0F, 14.0F, 0.0F)
         );
         PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 6.5F));
         tail.addOrReplaceChild(
            "tail_r1",
            CubeListBuilder.create().texOffs(24, 33).addBox(-2.5F, -1.0F, -0.5F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(12, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.25F, 3.5F, 5.25F)
         );
         body.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.4F, 3.5F, 5.4F)
         );
         body.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(12, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.4F, 3.5F, -5.3F)
         );
         body.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, -1.5F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.4F, 3.5F, -5.4F)
         );
         PartDefinition neck = body.addOrReplaceChild("head_parts", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, -5.0F));
         neck.addOrReplaceChild(
            "neck_r1",
            CubeListBuilder.create().texOffs(30, 9).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F)
         );
         PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -3.0F));
         head.addOrReplaceChild(
            "head_r1",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.6F, -8.4F, 6.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3927F, 0.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(2.0F, -3.5F, -1.0F, 0.48F, 0.0F, 0.48F)
         );
         head.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(22, 0).mirror().addBox(-2.0F, -6.5F, -0.3F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offsetAndRotation(-2.0F, -3.5F, -1.0F, 0.48F, 0.0F, -0.48F)
         );
         body.addOrReplaceChild("right_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
         body.addOrReplaceChild("left_chest", CubeListBuilder.create(), PartPose.offset(-1.0F, 10.0F, 0.0F));
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(T horse, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(T horse, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float yRot = Mth.clamp(netHeadYaw, -20.0F, 20.0F);
         float headRotX = (this.donkey ? -30.0F : headPitch) * (float) (Math.PI / 180.0);
         if (limbSwingAmount > 0.2F && !this.donkey) {
            headRotX += Mth.cos(limbSwing * 0.8F) * 0.15F * limbSwingAmount;
         }

         float eating = horse.getEatAnim(this.partialTick);
         float standing = horse.getStandAnim(this.partialTick);
         float feeding = horse.getMouthAnim(this.partialTick);
         float iStanding = 1.0F - standing;
         this.headParts.yRot = yRot * (float) (Math.PI / 180.0);
         float legAnim = Mth.cos((horse.isInWater() ? 0.2F : 1.0F) * limbSwing * 0.6662F + (float) Math.PI);
         float legXRot = legAnim * 0.8F * limbSwingAmount;
         float baseHead = (1.0F - Math.max(standing, eating)) * ((float) (Math.PI / 6) + headRotX + feeding * Mth.sin(ageInTicks) * 0.05F);
         this.headParts.xRot = standing * ((float) (Math.PI / 12) + headRotX)
            + eating * ((this.donkey ? (float) (Math.PI / 2) : 2.1816616F) + Mth.sin(ageInTicks) * 0.05F)
            + baseHead;
         this.headParts.yRot = standing * yRot * (float) (Math.PI / 180.0) + (1.0F - Math.max(standing, eating)) * this.headParts.yRot;
         if (this.donkey) {
            this.headParts.y = Mth.lerp(eating, this.headParts.y, -1.2F);
            this.headParts.z = Mth.lerp(standing, this.headParts.z, -3.6F);
         } else {
            this.headParts.y = this.headParts.y + Mth.lerp(eating, Mth.lerp(standing, 0.0F, -2.0F), 2.0F);
            this.headParts.z = Mth.lerp(standing, this.headParts.z, -4.0F);
         }

         this.body.xRot = standing * (float) (-Math.PI / 4) + iStanding * this.body.xRot;
         this.leftFrontLeg.y = this.leftFrontLeg.y - (this.donkey ? 1.0F : 4.0F) * standing;
         this.leftFrontLeg.z = this.leftFrontLeg.z + (this.donkey ? 0.5F : 0.0F) * standing;
         this.rightFrontLeg.y = this.leftFrontLeg.y;
         this.rightFrontLeg.z = this.leftFrontLeg.z;
         float standAngle = (this.donkey ? (float) (Math.PI / 3) : (float) (Math.PI / 12)) * standing;
         float bob = Mth.cos(ageInTicks * 0.6F + (float) Math.PI);
         float legStandXRot = this.donkey ? 0.0F : (float) (-Math.PI / 3);
         this.leftHindLeg.xRot = standAngle - legAnim * 0.5F * limbSwingAmount * iStanding;
         this.rightHindLeg.xRot = standAngle + legAnim * 0.5F * limbSwingAmount * iStanding;
         this.leftFrontLeg.xRot = (legStandXRot + bob) * standing + legXRot * iStanding;
         this.rightFrontLeg.xRot = (legStandXRot - bob) * standing - legXRot * iStanding;
         if (this.donkey) {
            this.leftHindLeg.y = Mth.lerp(standing, this.leftHindLeg.y, -0.3F);
            this.rightHindLeg.y = Mth.lerp(standing, this.leftHindLeg.y, -0.3F);
         }

         this.tail.xRot = (this.donkey ? (float) (-Math.PI / 4) : (float) (-Math.PI / 2)) + (float) (Math.PI / 6) + limbSwingAmount * 0.75F;
         this.tail.y += limbSwingAmount * 0.5F;
         this.tail.z += limbSwingAmount * 2.0F * 0.5F;
         this.tail.yRot = horse.tailCounter > 0 ? Mth.cos(ageInTicks * 0.7F) : 0.0F;
      }
   }

   public static class Camel26 extends CamelRenderer {
      private final MobRenderer<Camel, MountBabyRenderers26.BabyCamelModel> baby;

      public Camel26(Context context) {
         super(context, ModelLayers.CAMEL);
         ResourceLocation texture = MountBabyRenderers26.tex("camel/camel_baby");
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new MountBabyRenderers26.BabyCamelModel(context.bakeLayer(MountBabyRenderers26.CAMEL_BABY)), 0.35F, c -> texture
         );
      }

      public void render(Camel camel, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (camel.isBaby()) {
            this.baby.render(camel, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(camel, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Chested26<T extends AbstractChestedHorse> extends ChestedHorseRenderer<T> {
      private final MobRenderer<T, MountBabyRenderers26.BabyEquineModel<T>> baby;
      private final float scale;

      public Chested26(Context context, float scale, ModelLayerLocation layer, String babyTexture) {
         super(context, scale, layer);
         this.scale = scale;
         final ResourceLocation texture = MountBabyRenderers26.tex("horse/" + babyTexture);
         final float s = scale;
         this.baby = new MobRenderer<T, MountBabyRenderers26.BabyEquineModel<T>>(
            context, new MountBabyRenderers26.BabyEquineModel<>(context.bakeLayer(MountBabyRenderers26.DONKEY_BABY), true), 0.4F
         ) {
            public ResourceLocation getTextureLocation(T entity) {
               return texture;
            }

            protected void scale(T entity, PoseStack poseStack, float partialTick) {
               poseStack.scale(s, s, s);
            }
         };
      }

      public void render(T horse, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (horse.isBaby()) {
            this.baby.render(horse, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(horse, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   private static class MarkingLayer extends RenderLayer<Horse, MountBabyRenderers26.BabyEquineModel<Horse>> {
      private static final Map<Markings, ResourceLocation> TEXTURES = Map.of(
         Markings.WHITE,
         MountBabyRenderers26.tex("horse/horse_markings_white_baby"),
         Markings.WHITE_FIELD,
         MountBabyRenderers26.tex("horse/horse_markings_whitefield_baby"),
         Markings.WHITE_DOTS,
         MountBabyRenderers26.tex("horse/horse_markings_whitedots_baby"),
         Markings.BLACK_DOTS,
         MountBabyRenderers26.tex("horse/horse_markings_blackdots_baby")
      );

      MarkingLayer(RenderLayerParent<Horse, MountBabyRenderers26.BabyEquineModel<Horse>> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Horse horse,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         ResourceLocation texture = TEXTURES.get(horse.getMarkings());
         if (texture != null && !horse.isInvisible()) {
            ((MountBabyRenderers26.BabyEquineModel)this.getParentModel())
               .renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucent(texture)), light, LivingEntityRenderer.getOverlayCoords(horse, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   public static class Mooshroom26 extends MushroomCowRenderer {
      private final MobRenderer<MushroomCow, FarmAnimalModels.Quadruped<MushroomCow>> baby;

      public Mooshroom26(Context context) {
         super(context);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context,
            new FarmAnimalModels.Quadruped<>(context.bakeLayer(FarmAnimalModels.COW_BABY)),
            0.35F,
            m -> m.getVariant() == MushroomType.BROWN
               ? MountBabyRenderers26.tex("cow/mooshroom_brown_baby")
               : MountBabyRenderers26.tex("cow/mooshroom_red_baby")
         );
      }

      public void render(MushroomCow cow, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (cow.isBaby()) {
            this.baby.render(cow, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(cow, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Undead26 extends UndeadHorseRenderer {
      private final MobRenderer<AbstractHorse, MountBabyRenderers26.BabyEquineModel<AbstractHorse>> baby;

      public Undead26(Context context, ModelLayerLocation layer, String babyTexture) {
         super(context, layer);
         ResourceLocation texture = MountBabyRenderers26.tex("horse/" + babyTexture);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new MountBabyRenderers26.BabyEquineModel<>(context.bakeLayer(MountBabyRenderers26.HORSE_BABY), false), 0.4F, h -> texture
         );
      }

      public void render(AbstractHorse horse, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (horse.isBaby()) {
            this.baby.render(horse, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(horse, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }
}
