package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.Parched;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class ParchedRenderer extends SkeletonRenderer<Parched> {
   private static final ResourceLocation TEXTURE = FutureBackport.id("textures/entity/skeleton/parched.png");

   public ParchedRenderer(Context context) {
      super(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
   }

   public ResourceLocation getTextureLocation(Parched parched) {
      return TEXTURE;
   }
}
