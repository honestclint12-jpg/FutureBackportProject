package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.CopperGolemModel;
import com.futurebackport.entity.CopperGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

public class CopperGolemRenderer extends MobRenderer<CopperGolem, CopperGolemModel> {
   public static final ModelLayerLocation LAYER = new ModelLayerLocation(FutureBackport.id("copper_golem"), "main");
   private static final Map<WeatherState, ResourceLocation> TEXTURES = Map.of(
      WeatherState.UNAFFECTED,
      texture("copper_golem"),
      WeatherState.EXPOSED,
      texture("copper_golem_exposed"),
      WeatherState.WEATHERED,
      texture("copper_golem_weathered"),
      WeatherState.OXIDIZED,
      texture("copper_golem_oxidized")
   );
   private static final Map<WeatherState, ResourceLocation> EYES = Map.of(
      WeatherState.UNAFFECTED,
      texture("copper_golem_eyes"),
      WeatherState.EXPOSED,
      texture("copper_golem_eyes_exposed"),
      WeatherState.WEATHERED,
      texture("copper_golem_eyes_weathered"),
      WeatherState.OXIDIZED,
      texture("copper_golem_eyes_oxidized")
   );

   private static ResourceLocation texture(String name) {
      return FutureBackport.id("textures/entity/copper_golem/" + name + ".png");
   }

   public CopperGolemRenderer(Context context) {
      super(context, new CopperGolemModel(context.bakeLayer(LAYER)), 0.5F);
      this.addLayer(new CopperGolemRenderer.EyesLayer(this));
      this.addLayer(new ItemInHandLayer(this, context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(CopperGolem golem) {
      return TEXTURES.get(golem.getWeatherState());
   }

   public static ResourceLocation texture(WeatherState state) {
      return TEXTURES.get(state);
   }

   private static class EyesLayer extends RenderLayer<CopperGolem, CopperGolemModel> {
      EyesLayer(RenderLayerParent<CopperGolem, CopperGolemModel> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         CopperGolem golem,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!golem.isInvisible()) {
            VertexConsumer consumer = buffers.getBuffer(RenderType.eyes(CopperGolemRenderer.EYES.get(golem.getWeatherState())));
            ((CopperGolemModel)this.getParentModel()).renderToBuffer(poseStack, consumer, 15728640, LivingEntityRenderer.getOverlayCoords(golem, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}
