package com.futurebackport.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AgeSwitchRenderer<T extends LivingEntity> extends EntityRenderer<T> {
   private final EntityRenderer<T> adult;
   private final EntityRenderer<T> baby;
   private final float adultShadow;
   private final float babyShadow;

   public AgeSwitchRenderer(Context context, EntityRenderer<T> adult, float adultShadow, EntityRenderer<T> baby, float babyShadow) {
      super(context);
      this.adult = adult;
      this.baby = baby;
      this.adultShadow = adultShadow;
      this.babyShadow = babyShadow;
   }

   private EntityRenderer<T> pick(T entity) {
      return entity.isBaby() ? this.baby : this.adult;
   }

   public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      EntityRenderer<T> renderer = this.pick(entity);
      this.shadowRadius = entity.isBaby() ? this.babyShadow : this.adultShadow;
      renderer.render(entity, yaw, partialTick, poseStack, buffers, light);
   }

   public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
      return this.pick(entity).shouldRender(entity, frustum, camX, camY, camZ);
   }

   public Vec3 getRenderOffset(T entity, float partialTick) {
      return this.pick(entity).getRenderOffset(entity, partialTick);
   }

   public ResourceLocation getTextureLocation(T entity) {
      return this.pick(entity).getTextureLocation(entity);
   }
}
