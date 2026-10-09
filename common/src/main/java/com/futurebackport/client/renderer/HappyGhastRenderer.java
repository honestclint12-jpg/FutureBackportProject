package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.HappyGhastModels;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.item.HarnessItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class HappyGhastRenderer extends MobRenderer<HappyGhast, HappyGhastModels.GhastModel> {
   private static final ResourceLocation TEXTURE = FutureBackport.id("textures/entity/ghast/happy_ghast.png");
   private static final ResourceLocation BABY_TEXTURE = FutureBackport.id("textures/entity/ghast/happy_ghast_baby.png");
   private final HappyGhastModels.GhastModel adult = (HappyGhastModels.GhastModel)this.model;
   private final HappyGhastModels.GhastModel baby;

   public HappyGhastRenderer(Context context) {
      super(context, new HappyGhastModels.GhastModel(context.bakeLayer(HappyGhastModels.GHAST)), 2.0F);
      this.baby = new HappyGhastModels.GhastModel(context.bakeLayer(HappyGhastModels.BABY));
      this.addLayer(new HappyGhastRenderer.HarnessLayer(this, new HappyGhastModels.HarnessModel(context.bakeLayer(HappyGhastModels.HARNESS))));
   }

   public void render(HappyGhast ghast, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      this.model = ghast.isBaby() ? this.baby : this.adult;
      this.shadowRadius = ghast.isBaby() ? 0.5F : 2.0F;
      super.render(ghast, yaw, partialTicks, poseStack, buffer, packedLight);
   }

   protected void scale(HappyGhast ghast, PoseStack poseStack, float partialTick) {
      float scale = 4.0F * ghast.getAgeScale();
      poseStack.scale(scale, scale, scale);
   }

   public ResourceLocation getTextureLocation(HappyGhast ghast) {
      return ghast.isBaby() ? BABY_TEXTURE : TEXTURE;
   }

   private static class HarnessLayer extends RenderLayer<HappyGhast, HappyGhastModels.GhastModel> {
      private final HappyGhastModels.HarnessModel model;

      HarnessLayer(RenderLayerParent<HappyGhast, HappyGhastModels.GhastModel> parent, HappyGhastModels.HarnessModel model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffer,
         int packedLight,
         HappyGhast ghast,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (ghast.getItemBySlot(EquipmentSlot.BODY).getItem() instanceof HarnessItem harness && !ghast.isBaby()) {
            ResourceLocation texture = FutureBackport.id(
               "textures/entity/equipment/happy_ghast_body/" + harness.getColor().getSerializedName() + "_harness.png"
            );
            this.model.setupAnim(ghast, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model
               .renderToBuffer(
                  poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), packedLight, LivingEntityRenderer.getOverlayCoords(ghast, 0.0F)
               );
         }
      }
   }
}
