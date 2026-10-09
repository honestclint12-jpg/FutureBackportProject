package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.FarmAnimalModels;
import com.futurebackport.entity.FarmAnimalVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;

public final class FarmAnimalRenderers {
   private FarmAnimalRenderers() {
   }

   private static ResourceLocation texture(String animal, FarmAnimalVariant variant, boolean baby) {
      return FutureBackport.id("textures/entity/" + animal + "/" + animal + "_" + variant.getSerializedName() + (baby ? "_baby" : "") + ".png");
   }

   public static class ChickenRenderer extends FarmAnimalRenderers.VariantRenderer<Chicken, FarmAnimalModels.ChickenModel> {
      public ChickenRenderer(Context context) {
         super(context, "chicken", new FarmAnimalModels.ChickenModel(context.bakeLayer(FarmAnimalModels.CHICKEN_BABY)), 0.3F);
         this.adult(FarmAnimalVariant.TEMPERATE, new FarmAnimalModels.ChickenModel(context.bakeLayer(FarmAnimalModels.CHICKEN)));
         this.adult(FarmAnimalVariant.WARM, new FarmAnimalModels.ChickenModel(context.bakeLayer(FarmAnimalModels.CHICKEN)));
         this.adult(FarmAnimalVariant.COLD, new FarmAnimalModels.ChickenModel(context.bakeLayer(FarmAnimalModels.CHICKEN_COLD)));
      }

      protected float getBob(Chicken chicken, float partialTicks) {
         float flap = Mth.lerp(partialTicks, chicken.oFlap, chicken.flap);
         float flapSpeed = Mth.lerp(partialTicks, chicken.oFlapSpeed, chicken.flapSpeed);
         return (Mth.sin(flap) + 1.0F) * flapSpeed;
      }
   }

   public static class CowRenderer extends FarmAnimalRenderers.VariantRenderer<Cow, FarmAnimalModels.Quadruped<Cow>> {
      public CowRenderer(Context context) {
         super(context, "cow", new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.COW_BABY)), 0.7F);
         this.adult(FarmAnimalVariant.TEMPERATE, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.COW)));
         this.adult(FarmAnimalVariant.WARM, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.COW_WARM)));
         this.adult(FarmAnimalVariant.COLD, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.COW_COLD)));
      }
   }

   public static class PigRenderer extends FarmAnimalRenderers.VariantRenderer<Pig, FarmAnimalModels.Quadruped<Pig>> {
      public PigRenderer(Context context) {
         super(context, "pig", new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.PIG_BABY)), 0.7F);
         this.adult(FarmAnimalVariant.TEMPERATE, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.PIG)));
         this.adult(FarmAnimalVariant.WARM, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.PIG)));
         this.adult(FarmAnimalVariant.COLD, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.PIG_COLD)));
         this.addLayer(new FarmAnimalRenderers.SaddleLayer(this, new FarmAnimalModels.Quadruped(context.bakeLayer(FarmAnimalModels.PIG_SADDLE))));
      }
   }

   private static class SaddleLayer extends RenderLayer<Pig, FarmAnimalModels.Quadruped<Pig>> {
      private static final ResourceLocation SADDLE = new ResourceLocation("textures/entity/pig/pig_saddle.png");
      private final FarmAnimalModels.Quadruped<Pig> model;

      SaddleLayer(RenderLayerParent<Pig, FarmAnimalModels.Quadruped<Pig>> parent, FarmAnimalModels.Quadruped<Pig> model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffer,
         int packedLight,
         Pig pig,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (pig.isSaddled() && !pig.isBaby()) {
            this.model.prepareMobModel(pig, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(pig, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model
               .renderToBuffer(
                  poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(SADDLE)), packedLight, LivingEntityRenderer.getOverlayCoords(pig, 0.0F)
               , 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }

   private abstract static class VariantRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
      private final String animal;
      private final Map<FarmAnimalVariant, M> adults = new EnumMap<>(FarmAnimalVariant.class);
      private final M baby;

      VariantRenderer(Context context, String animal, M baby, float shadow) {
         super(context, baby, shadow);
         this.animal = animal;
         this.baby = baby;
      }

      void adult(FarmAnimalVariant variant, M model) {
         this.adults.put(variant, model);
      }

      public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
         this.model = entity.isBaby() ? this.baby : this.adults.get(FarmAnimalVariant.get(entity));
         super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
      }

      public ResourceLocation getTextureLocation(T entity) {
         return FarmAnimalRenderers.texture(this.animal, FarmAnimalVariant.get(entity), entity.isBaby());
      }
   }
}
