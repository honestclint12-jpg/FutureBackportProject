package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.NautilusModel;
import com.futurebackport.entity.nautilus.AbstractNautilus;
import com.futurebackport.entity.nautilus.Nautilus;
import com.futurebackport.entity.nautilus.ZombieNautilus;
import com.futurebackport.item.NautilusArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class NautilusRenderer<T extends AbstractNautilus> extends MobRenderer<T, NautilusModel<T>> {
   public static final ModelLayerLocation ADULT = layer("main");
   public static final ModelLayerLocation BABY = layer("baby");
   public static final ModelLayerLocation ARMOR = layer("armor");
   public static final ModelLayerLocation SADDLE = layer("saddle");
   public static final ModelLayerLocation CORAL = layer("coral");
   private static final ResourceLocation SADDLE_TEXTURE = FutureBackport.id("textures/entity/equipment/nautilus_saddle/saddle.png");
   private final NautilusModel<T> adult;
   private final NautilusModel<T> baby;
   private final Function<T, ResourceLocation> texture;

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id("nautilus"), name);
   }

   private NautilusRenderer(Context context, NautilusModel<T> adult, NautilusModel<T> baby, Function<T, ResourceLocation> texture) {
      super(context, adult, 0.7F);
      this.adult = adult;
      this.baby = baby;
      this.texture = texture;
      this.addLayer(new NautilusRenderer.ArmorLayer(this, new NautilusModel<>(context.bakeLayer(ARMOR))));
      this.addLayer(new NautilusRenderer.SaddleLayer(this, new NautilusModel<>(context.bakeLayer(SADDLE))));
   }

   public static NautilusRenderer<Nautilus> nautilus(Context context) {
      ResourceLocation adult = FutureBackport.id("textures/entity/nautilus/nautilus.png");
      ResourceLocation baby = FutureBackport.id("textures/entity/nautilus/nautilus_baby.png");
      return new NautilusRenderer<>(
         context, new NautilusModel<>(context.bakeLayer(ADULT)), new NautilusModel<>(context.bakeLayer(BABY)), n -> n.isBaby() ? baby : adult
      );
   }

   public static NautilusRenderer<ZombieNautilus> zombie(Context context) {
      ResourceLocation normal = FutureBackport.id("textures/entity/nautilus/zombie_nautilus.png");
      ResourceLocation coral = FutureBackport.id("textures/entity/nautilus/zombie_nautilus_coral.png");
      final NautilusModel<ZombieNautilus> normalModel = new NautilusModel<>(context.bakeLayer(ADULT));
      final NautilusModel<ZombieNautilus> coralModel = new NautilusModel<>(context.bakeLayer(CORAL));
      return new NautilusRenderer<ZombieNautilus>(context, normalModel, normalModel, z -> z.getVariant() == ZombieNautilus.Variant.WARM ? coral : normal) {
         public void render(ZombieNautilus z, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
            this.model = z.getVariant() == ZombieNautilus.Variant.WARM ? coralModel : normalModel;
            super.renderBody(z, yaw, partialTick, poseStack, buffers, light);
         }
      };
   }

   public void render(T nautilus, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      this.model = nautilus.isBaby() ? this.baby : this.adult;
      this.renderBody(nautilus, yaw, partialTick, poseStack, buffers, light);
   }

   void renderBody(T nautilus, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      super.render(nautilus, yaw, partialTick, poseStack, buffers, light);
   }

   public ResourceLocation getTextureLocation(T nautilus) {
      return this.texture.apply(nautilus);
   }

   private static class ArmorLayer<T extends AbstractNautilus> extends RenderLayer<T, NautilusModel<T>> {
      private final NautilusModel<T> model;

      ArmorLayer(RenderLayerParent<T, NautilusModel<T>> parent, NautilusModel<T> model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         T nautilus,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!nautilus.isBaby() && nautilus.getBodyArmorItem().getItem() instanceof NautilusArmorItem armor) {
            this.model.prepareMobModel(nautilus, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(nautilus, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(armor.getTexture())), light, OverlayTexture.NO_OVERLAY);
         }
      }
   }

   private static class SaddleLayer<T extends AbstractNautilus> extends RenderLayer<T, NautilusModel<T>> {
      private final NautilusModel<T> model;

      SaddleLayer(RenderLayerParent<T, NautilusModel<T>> parent, NautilusModel<T> model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         T nautilus,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!nautilus.isBaby() && nautilus.isSaddled()) {
            this.model.prepareMobModel(nautilus, limbSwing, limbSwingAmount, partialTick);
            this.model.setupAnim(nautilus, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model
               .renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutoutNoCull(NautilusRenderer.SADDLE_TEXTURE)), light, OverlayTexture.NO_OVERLAY);
         }
      }
   }
}
