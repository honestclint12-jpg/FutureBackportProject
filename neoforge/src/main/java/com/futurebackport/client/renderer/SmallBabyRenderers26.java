package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.FarmAnimalModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.goat.Goat;

public final class SmallBabyRenderers26 {
   public static final ModelLayerLocation TURTLE_BABY = new ModelLayerLocation(FutureBackport.id("turtle_baby"), "main");
   public static final ModelLayerLocation GOAT_BABY = new ModelLayerLocation(FutureBackport.id("goat_baby"), "main");

   private SmallBabyRenderers26() {
   }

   private static <T extends Mob, M extends EntityModel<T>> MobRenderer<T, M> babyRenderer(
      Context context, M model, float shadow, final ResourceLocation texture
   ) {
      return new MobRenderer<T, M>(context, model, shadow) {
         public ResourceLocation getTextureLocation(T entity) {
            return texture;
         }
      };
   }

   public static class BabyGoatModel extends FarmAnimalModels.Quadruped<Goat> {
      public BabyGoatModel(ModelPart root) {
         super(root);
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(29, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 19.5F, 3.0F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(21, 12).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 19.5F, 3.0F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(21, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-1.5F, 19.5F, -2.0F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(29, 5).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(1.5F, 19.5F, -2.0F)
         );
         root.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -2.3F, -4.5F, 6.0F, 5.0F, 9.0F).texOffs(0, 24).addBox(-2.5F, -2.2F, -4.0F, 5.0F, 4.0F, 8.0F),
            PartPose.offset(0.0F, 17.8F, 0.0F)
         );
         PartDefinition head = root.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.8126F, -5.1548F, 4.0F, 4.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, 15.5F, -3.0F, 0.4363F, 0.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "right_horn",
            CubeListBuilder.create().texOffs(24, 0).mirror().addBox(0.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
            PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "left_horn",
            CubeListBuilder.create().texOffs(24, 0).mirror().addBox(2.0F, -4.5F, 0.0F, 1.0F, 2.0F, 1.0F).mirror(false),
            PartPose.offsetAndRotation(-1.5F, -1.5F, -1.0F, (float) (-Math.PI / 8), 0.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F).mirror(false),
            PartPose.offsetAndRotation(-1.7F, -2.3126F, 0.1452F, 0.0F, -0.5236F, 0.0F)
         );
         head.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(0, 12).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F),
            PartPose.offsetAndRotation(1.7F, -2.3126F, 0.1452F, 0.0F, 0.5236F, 0.0F)
         );
         head.addOrReplaceChild(
            "HeadMain", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.5F, -4.0F, 4.0F, 4.0F, 6.0F), PartPose.offset(0.0F, -1.3126F, -1.1548F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public void setupAnim(Goat goat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root().getAllParts().forEach(ModelPart::resetPose);
         super.setupAnim(goat, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         ModelPart head = this.root().getChild("head");
         head.getChild("left_horn").visible = goat.hasLeftHorn();
         head.getChild("right_horn").visible = goat.hasRightHorn();
         float ram = goat.getRammingXHeadRot();
         head.xRot = ram != 0.0F ? ram : (float) (Math.PI / 8);
      }
   }

   public static class BabyTurtleModel extends FarmAnimalModels.Quadruped<Turtle> {
      public BabyTurtleModel(ModelPart root) {
         super(root);
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.offset(0.0F, 22.9F, 1.0F)
         );
         root.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 22.9F, -1.0F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, 2.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(-1, 1).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, 2.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(8, 6).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(-2.0F, 23.9F, -0.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(8, 7).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F), PartPose.offset(2.0F, 23.9F, -0.5F)
         );
         return LayerDefinition.create(mesh, 16, 16);
      }

      public void setupAnim(Turtle turtle, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root().getAllParts().forEach(ModelPart::resetPose);
         ModelPart head = this.root().getChild("head");
         head.xRot = headPitch * (float) (Math.PI / 180.0);
         head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         ModelPart rf = this.root().getChild("right_front_leg");
         ModelPart lf = this.root().getChild("left_front_leg");
         ModelPart rh = this.root().getChild("right_hind_leg");
         ModelPart lh = this.root().getChild("left_hind_leg");
         if (!turtle.isInWater() && turtle.onGround()) {
            float layEgg = turtle.isLayingEgg() ? 4.0F : 1.0F;
            float amplitude = turtle.isLayingEgg() ? 2.0F : 1.0F;
            float swingPos = limbSwing * 5.0F;
            float front = Mth.cos(layEgg * swingPos);
            float hind = Mth.cos(swingPos);
            rf.yRot = -front * 8.0F * limbSwingAmount * amplitude;
            lf.yRot = front * 8.0F * limbSwingAmount * amplitude;
            rh.yRot = -hind * 3.0F * limbSwingAmount;
            lh.yRot = hind * 3.0F * limbSwingAmount;
         } else {
            float swing = Mth.cos(limbSwing * 0.6662F * 0.6F) * 0.5F * limbSwingAmount;
            rh.xRot = swing;
            lh.xRot = -swing;
            rf.zRot = -swing;
            lf.zRot = swing;
         }
      }
   }

   public static class Goat26 extends GoatRenderer {
      private final MobRenderer<Goat, SmallBabyRenderers26.BabyGoatModel> baby;

      public Goat26(Context context) {
         super(context);
         this.baby = SmallBabyRenderers26.babyRenderer(
            context,
            new SmallBabyRenderers26.BabyGoatModel(context.bakeLayer(SmallBabyRenderers26.GOAT_BABY)),
            0.35F,
            FutureBackport.id("textures/entity/goat/goat_baby.png")
         );
      }

      public void render(Goat goat, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (goat.isBaby()) {
            this.baby.render(goat, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(goat, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   public static class Turtle26 extends TurtleRenderer {
      private final MobRenderer<Turtle, SmallBabyRenderers26.BabyTurtleModel> baby;

      public Turtle26(Context context) {
         super(context);
         this.baby = SmallBabyRenderers26.babyRenderer(
            context,
            new SmallBabyRenderers26.BabyTurtleModel(context.bakeLayer(SmallBabyRenderers26.TURTLE_BABY)),
            0.2F,
            FutureBackport.id("textures/entity/turtle/turtle_baby.png")
         );
      }

      public void render(Turtle turtle, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (turtle.isBaby()) {
            this.baby.render(turtle, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(turtle, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }
}
