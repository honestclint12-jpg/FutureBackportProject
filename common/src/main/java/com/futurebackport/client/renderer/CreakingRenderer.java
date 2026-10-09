package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.CreakingModel;
import com.futurebackport.entity.Creaking;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class CreakingRenderer extends MobRenderer<Creaking, CreakingModel> {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(FutureBackport.id("creaking"), "main");
   public static final ModelLayerLocation EYES_LAYER = new ModelLayerLocation(FutureBackport.id("creaking"), "eyes");
   private static final ResourceLocation TEXTURE = FutureBackport.id("textures/entity/creaking/creaking.png");
   private static final ResourceLocation EYES_TEXTURE = FutureBackport.id("textures/entity/creaking/creaking_eyes.png");

   public CreakingRenderer(Context context) {
      super(context, new CreakingModel(context.bakeLayer(LAYER)), 0.6F);
      this.addLayer(new CreakingRenderer.EyesLayer(this, new CreakingModel(context.bakeLayer(EYES_LAYER))));
   }

   public ResourceLocation getTextureLocation(Creaking creaking) {
      return TEXTURE;
   }

   protected void setupRotations(Creaking creaking, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
      if (creaking.isTearingDown()) {
         int deathTime = creaking.deathTime;
         creaking.deathTime = 0;
         super.setupRotations(creaking, poseStack, bob, yBodyRot, partialTick, scale);
         creaking.deathTime = deathTime;
      } else {
         super.setupRotations(creaking, poseStack, bob, yBodyRot, partialTick, scale);
      }
   }

   protected float getWhiteOverlayProgress(Creaking creaking, float partialTicks) {
      return 0.0F;
   }

   private static class EyesLayer extends RenderLayer<Creaking, CreakingModel> {
      private final CreakingModel model;

      EyesLayer(RenderLayerParent<Creaking, CreakingModel> parent, CreakingModel model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffer,
         int packedLight,
         Creaking creaking,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         boolean glowing = creaking.isTearingDown() ? creaking.hasGlowingEyes() : creaking.isActive();
         if (glowing && !creaking.isInvisible()) {
            ((CreakingModel)this.getParentModel()).copyPropertiesTo(this.model);
            this.model.prepareMobModel(creaking, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(creaking, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(CreakingRenderer.EYES_TEXTURE));
            this.model.renderToBuffer(poseStack, consumer, 15728880, LivingEntityRenderer.getOverlayCoords(creaking, 0.0F));
         }
      }
   }
}
