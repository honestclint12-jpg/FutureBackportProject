package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.animal.Wolf;

public class WolfRenderer26 extends WolfRenderer {
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("wolf_baby"), "main");
   private final WolfRenderer26.BabyRenderer baby;

   public WolfRenderer26(Context context) {
      super(context);
      this.baby = new WolfRenderer26.BabyRenderer(context);
   }

   public void render(Wolf wolf, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      if (wolf.isBaby()) {
         this.baby.render(wolf, yaw, partialTick, poseStack, buffers, light);
      } else {
         super.render(wolf, yaw, partialTick, poseStack, buffers, light);
      }
   }

   private static class BabyRenderer extends MobRenderer<Wolf, WolfRenderer26.BabyWolfModel> {
      private static final ResourceLocation COLLAR = FutureBackport.id("textures/entity/wolf/wolf_collar_baby.png");
      private final Map<ResourceLocation, ResourceLocation> babyTextures = new HashMap<>();

      BabyRenderer(Context context) {
         super(context, new WolfRenderer26.BabyWolfModel(context.bakeLayer(WolfRenderer26.BABY)), 0.5F);
         this.addLayer(new WolfRenderer26.CollarLayer(this));
      }

      protected float getBob(Wolf wolf, float partialTick) {
         return wolf.getTailAngle();
      }

      public void render(Wolf wolf, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (wolf.isWet()) {
            float shade = wolf.getWetShade(partialTick);
            ((WolfRenderer26.BabyWolfModel)this.model).color = ARGB32.colorFromFloat(1.0F, shade, shade, shade);
         }

         super.render(wolf, yaw, partialTick, poseStack, buffers, light);
         ((WolfRenderer26.BabyWolfModel)this.model).color = -1;
      }

      public ResourceLocation getTextureLocation(Wolf wolf) {
         ResourceLocation adult = wolf.getTexture();
         return this.babyTextures.computeIfAbsent(adult, a -> {
            ResourceLocation baby = FutureBackport.id(a.getPath().replace(".png", "_baby.png"));
            return Minecraft.getInstance().getResourceManager().getResource(baby).isPresent() ? baby : a;
         });
      }
   }

   public static class BabyWolfModel extends HierarchicalModel<Wolf> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart body;
      private final ModelPart rightHindLeg;
      private final ModelPart leftHindLeg;
      private final ModelPart rightFrontLeg;
      private final ModelPart leftFrontLeg;
      private final ModelPart tail;
      int color = -1;
      private float partialTick;

      public BabyWolfModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.body = root.getChild("body");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftHindLeg = root.getChild("left_hind_leg");
         this.rightFrontLeg = root.getChild("right_front_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
         this.tail = root.getChild("tail");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition head = root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 12)
               .addBox(-2.99F, -3.25F, -3.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.025F))
               .texOffs(17, 12)
               .addBox(-1.5F, -0.24F, -5.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 18.25F, -4.0F)
         );
         head.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(0, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(-2.0F, -4.25F, -0.5F)
         );
         head.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(20, 5).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(2.0F, -4.25F, -0.5F)
         );
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 4.0F, 8.0F), PartPose.offset(0.0F, 19.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, 3.0F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(8, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, 3.0F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.5F, 21.0F, -3.0F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.5F, 21.0F, -3.0F)
         );
         PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 19.0F, 3.0F, -0.5236F, 0.0F, 0.0F));
         tail.addOrReplaceChild(
            "tail_r1",
            CubeListBuilder.create().texOffs(22, 16).addBox(-1.0F, -5.7F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -0.6F, 0.2F, -3.1F, 0.0F, 0.0F)
         );
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(Wolf wolf, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(Wolf wolf, float limbSwing, float limbSwingAmount, float tailAngle, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.tail.yRot = wolf.isAngry() ? 0.0F : Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         if (wolf.isInSittingPose()) {
            float ageScale = 0.5F;
            this.body.y += 4.0F * ageScale;
            this.body.z -= 2.0F * ageScale;
            this.body.xRot = -0.21460181F;
            this.tail.y += 9.0F * ageScale;
            this.tail.z -= 2.0F * ageScale;
            this.rightHindLeg.y += 6.7F * ageScale;
            this.rightHindLeg.z -= 5.0F * ageScale;
            this.rightHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.leftHindLeg.y += 6.7F * ageScale;
            this.leftHindLeg.z -= 5.0F * ageScale;
            this.leftHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.rightFrontLeg.xRot = 5.811947F;
            this.rightFrontLeg.x += 0.01F * ageScale;
            this.rightFrontLeg.y += 1.0F * ageScale;
            this.leftFrontLeg.xRot = 5.811947F;
            this.leftFrontLeg.x -= 0.01F * ageScale;
            this.leftFrontLeg.y += 1.0F * ageScale;
         } else {
            this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
            this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
            this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         }

         this.body.zRot = wolf.getBodyRollAngle(this.partialTick, -0.16F);
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.head.zRot = wolf.getHeadRollAngle(this.partialTick) + wolf.getBodyRollAngle(this.partialTick, 0.0F);
         this.tail.xRot = tailAngle;
         this.tail.zRot = wolf.getBodyRollAngle(this.partialTick, -0.2F);
      }

      public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int tint) {
         super.renderToBuffer(poseStack, buffer, light, overlay, this.color == -1 ? tint : ARGB32.multiply(tint, this.color));
      }
   }

   private static class CollarLayer extends RenderLayer<Wolf, WolfRenderer26.BabyWolfModel> {
      CollarLayer(RenderLayerParent<Wolf, WolfRenderer26.BabyWolfModel> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Wolf wolf,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (wolf.isTame() && !wolf.isInvisible()) {
            ((WolfRenderer26.BabyWolfModel)this.getParentModel())
               .renderToBuffer(
                  poseStack,
                  buffers.getBuffer(RenderType.entityCutoutNoCull(WolfRenderer26.BabyRenderer.COLLAR)),
                  light,
                  OverlayTexture.NO_OVERLAY,
                  wolf.getCollarColor().getTextureDiffuseColor()
               );
         }
      }
   }
}
