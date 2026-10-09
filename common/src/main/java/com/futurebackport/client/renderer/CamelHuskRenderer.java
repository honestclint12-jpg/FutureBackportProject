package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;

public class CamelHuskRenderer extends CamelRenderer {
   private static final ResourceLocation TEXTURE = FutureBackport.id("textures/entity/camel/camel_husk.png");

   public CamelHuskRenderer(Context context) {
      super(context, ModelLayers.CAMEL);
   }

   public ResourceLocation getTextureLocation(Camel camel) {
      return TEXTURE;
   }
}
