package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.EntityModel;
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
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

public final class NetherBabyRenderers26 {
   public static final ModelLayerLocation STRIDER_BABY = new ModelLayerLocation(FutureBackport.id("strider_baby"), "main");
   public static final ModelLayerLocation HOGLIN_BABY = new ModelLayerLocation(FutureBackport.id("hoglin_baby"), "main");

   private NetherBabyRenderers26() {
   }

   static <T extends Mob, M extends EntityModel<T>> MobRenderer<T, M> babyRenderer(
      Context context, M model, float shadow, Function<T, ResourceLocation> texture
   ) {
      return babyRenderer(context, model, shadow, texture, e -> false);
   }

   static <T extends Mob, M extends EntityModel<T>> MobRenderer<T, M> babyRenderer(
      Context context, M model, float shadow, final Function<T, ResourceLocation> texture, final Predicate<T> shaking
   ) {
      return new MobRenderer<T, M>(context, model, shadow) {
         public ResourceLocation getTextureLocation(T entity) {
            return texture.apply(entity);
         }

         protected boolean isShaking(T entity) {
            return super.isShaking(entity) || shaking.test(entity);
         }
      };
   }

   public static class BabyHoglinModel<T extends Mob & HoglinBase> extends HierarchicalModel<T> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart rightEar;
      private final ModelPart leftEar;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;

      public BabyHoglinModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.rightEar = this.head.getChild("right_ear");
         this.leftEar = this.head.getChild("left_ear");
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition head = root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-5.0F, -2.2605F, -10.547F, 10.0F, 4.0F, 12.0F)
               .texOffs(44, 29)
               .addBox(-7.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F)
               .texOffs(52, 29)
               .addBox(5.0F, -4.0981F, -8.4879F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 13.0F, -7.0F, 0.8727F, 0.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
               .texOffs(0, 16)
               .addBox(-4.0F, -14.0F, -7.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.02F))
               .texOffs(24, 39)
               .addBox(0.0F, -18.0F, -8.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.02F)),
            PartPose.offset(0.0F, 24.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(32, 5).addBox(-5.1F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
            PartPose.offsetAndRotation(-5.0F, -1.0F, -1.5F, 0.0F, 0.0F, -0.8727F)
         );
         head.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-0.9F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F).mirror(false),
            PartPose.offsetAndRotation(5.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.8727F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, 4.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, 4.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(-2.5F, 18.0F, -4.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(12, 38).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(2.5F, 18.0F, -4.5F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(T mob, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.rightEar.zRot = ((float) -Math.PI * 2.0F / 9.0F) - limbSwingAmount * Mth.sin(limbSwing);
         this.leftEar.zRot = ((float) Math.PI * 2.0F / 9.0F) + limbSwingAmount * Mth.sin(limbSwing);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         float headbutt = 1.0F - Mth.abs(10 - 2 * mob.getAttackAnimationRemainingTicks()) / 10.0F;
         this.head.xRot = Mth.lerp(headbutt, 0.87266463F, -0.34906587F);
         this.head.y += headbutt * 2.5F;
         this.rightFrontLeg.xRot = Mth.cos(limbSwing) * 1.2F * limbSwingAmount;
         this.leftFrontLeg.xRot = Mth.cos(limbSwing + (float) Math.PI) * 1.2F * limbSwingAmount;
         this.rightHindLeg.xRot = this.leftFrontLeg.xRot;
         this.leftHindLeg.xRot = this.rightFrontLeg.xRot;
      }
   }

   public static class BabyStriderModel extends HierarchicalModel<Strider> {
      private final ModelPart root;
      private final ModelPart body;
      private final ModelPart rightLeg;
      private final ModelPart leftLeg;
      private final ModelPart frontBristle;
      private final ModelPart middleBristle;
      private final ModelPart backBristle;

      public BabyStriderModel(ModelPart root) {
         this.root = root;
         this.body = root.getChild("body");
         this.rightLeg = root.getChild("right_leg");
         this.leftLeg = root.getChild("left_leg");
         this.frontBristle = this.body.getChild("bristle2");
         this.middleBristle = this.body.getChild("bristle1");
         this.backBristle = this.body.getChild("bristle0");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition body = root.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.75F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 16.75F, 0.0F)
         );
         root.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-1.5F, 20.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(1.5F, 20.0F, 0.0F)
         );
         body.addOrReplaceChild(
            "bristle0",
            CubeListBuilder.create().texOffs(0, 21).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -4.25F, 2.0F)
         );
         body.addOrReplaceChild(
            "bristle1",
            CubeListBuilder.create().texOffs(0, 18).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -4.25F, 0.0F)
         );
         body.addOrReplaceChild(
            "bristle2",
            CubeListBuilder.create().texOffs(0, 15).addBox(-3.5F, -2.5F, 0.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -4.25F, -2.0F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Strider strider, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float speed = Math.min(limbSwingAmount, 0.25F);
         this.body.xRot = headPitch * (float) (Math.PI / 180.0);
         this.body.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.body.zRot = 0.1F * Mth.sin(limbSwing * 1.5F) * 4.0F * speed;
         this.leftLeg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F) * 2.0F * speed;
         this.rightLeg.xRot = Mth.sin(limbSwing * 1.5F * 0.5F + (float) Math.PI) * 2.0F * speed;
         this.leftLeg.zRot = 0.17453294F * Mth.cos(limbSwing * 1.5F * 0.5F) * speed;
         this.rightLeg.zRot = 0.17453294F * Mth.cos(limbSwing * 1.5F * 0.5F + (float) Math.PI) * speed;
         this.body.y = 17.25F - 1.0F * Mth.cos(limbSwing * 1.5F) * 2.0F * speed;
         this.leftLeg.y = 20.0F + 2.0F * Mth.sin(limbSwing * 1.5F * 0.5F + (float) Math.PI) * 2.0F * speed;
         this.rightLeg.y = 20.0F + 2.0F * Mth.sin(limbSwing * 1.5F * 0.5F) * 2.0F * speed;
         float flow = Mth.cos(limbSwing * 1.5F + (float) Math.PI) * speed;
         this.frontBristle.xRot = this.frontBristle.xRot + (flow * 0.6F + 0.1F * Mth.sin(ageInTicks * 0.4F));
         this.middleBristle.xRot = this.middleBristle.xRot + (flow * 1.2F + 0.1F * Mth.sin(ageInTicks * 0.2F));
         this.backBristle.xRot = this.backBristle.xRot + (flow * 1.3F + 0.05F * Mth.sin(ageInTicks * -0.4F));
      }
   }

   public static class Hoglin26 extends HoglinRenderer {
      private final MobRenderer<Hoglin, NetherBabyRenderers26.BabyHoglinModel<Hoglin>> baby;

      public Hoglin26(Context context) {
         super(context);
         ResourceLocation texture = FutureBackport.id("textures/entity/hoglin/hoglin_baby.png");
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new NetherBabyRenderers26.BabyHoglinModel(context.bakeLayer(NetherBabyRenderers26.HOGLIN_BABY)), 0.35F, h -> texture, Hoglin::isConverting
         );
      }

      public void render(Hoglin hoglin, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (hoglin.isBaby()) {
            this.baby.render(hoglin, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(hoglin, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Strider26 extends StriderRenderer {
      private static final ResourceLocation WARM = FutureBackport.id("textures/entity/strider/strider_baby.png");
      private static final ResourceLocation COLD = FutureBackport.id("textures/entity/strider/strider_cold_baby.png");
      private final MobRenderer<Strider, NetherBabyRenderers26.BabyStriderModel> baby;

      public Strider26(Context context) {
         super(context);
         this.baby = NetherBabyRenderers26.babyRenderer(
            context,
            new NetherBabyRenderers26.BabyStriderModel(context.bakeLayer(NetherBabyRenderers26.STRIDER_BABY)),
            0.25F,
            s -> s.isSuffocating() ? COLD : WARM
         );
      }

      public void render(Strider strider, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (strider.isBaby()) {
            this.baby.render(strider, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(strider, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Zoglin26 extends ZoglinRenderer {
      private final MobRenderer<Zoglin, NetherBabyRenderers26.BabyHoglinModel<Zoglin>> baby;

      public Zoglin26(Context context) {
         super(context);
         ResourceLocation texture = FutureBackport.id("textures/entity/hoglin/zoglin_baby.png");
         this.baby = NetherBabyRenderers26.babyRenderer(
            context, new NetherBabyRenderers26.BabyHoglinModel(context.bakeLayer(NetherBabyRenderers26.HOGLIN_BABY)), 0.35F, z -> texture
         );
      }

      public void render(Zoglin zoglin, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (zoglin.isBaby()) {
            this.baby.render(zoglin, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(zoglin, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }
}
