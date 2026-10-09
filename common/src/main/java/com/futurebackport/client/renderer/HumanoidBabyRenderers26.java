package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.BabyHumanoids;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;

public final class HumanoidBabyRenderers26 {
   public static final ModelLayerLocation ZOMBIE_BABY = layer("zombie_baby");
   public static final ModelLayerLocation DROWNED_BABY_OUTER = new ModelLayerLocation(FutureBackport.id("zombie_baby"), "outer");
   public static final ModelLayerLocation ZOMBIE_VILLAGER_BABY = layer("zombie_villager_baby");
   public static final ModelLayerLocation PIGLIN_BABY = layer("piglin_baby");

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id(name), "main");
   }

   private static ResourceLocation tex(String path) {
      return FutureBackport.id("textures/entity/" + path + ".png");
   }

   private HumanoidBabyRenderers26() {
   }

   static <T extends LivingEntity, M extends HumanoidModel<T>> void wrapArmor(
      List<RenderLayer<T, M>> layers, RenderLayerParent<T, M> parent, Context context, boolean piglin, BabyHumanoids.Skeleton skeleton
   ) {
      for (int i = 0; i < layers.size(); i++) {
         if (layers.get(i) instanceof HumanoidArmorLayer armor) {
            layers.set(
               i,
               new BabyArmorLayer<>(
                  parent,
                  armor,
                  context.bakeLayer(piglin ? BabyArmorLayer.PIGLIN_INNER : BabyArmorLayer.HUMANOID_INNER),
                  context.bakeLayer(piglin ? BabyArmorLayer.PIGLIN_OUTER : BabyArmorLayer.HUMANOID_OUTER),
                  skeleton.positions()
               )
            );
         }
      }
   }

   public static class Drowned26 extends DrownedRenderer {
      private final DrownedModel<Drowned> adult = (DrownedModel<Drowned>)this.model;
      private final DrownedModel<Drowned> baby;

      public Drowned26(Context context) {
         super(context);
         this.baby = new BabyHumanoids.Drowned26<>(context.bakeLayer(HumanoidBabyRenderers26.ZOMBIE_BABY));
         HumanoidBabyRenderers26.wrapArmor(this.layers, this, context, false, BabyHumanoids.Skeleton.ZOMBIE);
         final DrownedModel<Drowned> babyOuter = new BabyHumanoids.Drowned26<>(context.bakeLayer(HumanoidBabyRenderers26.DROWNED_BABY_OUTER));
         final ResourceLocation outerTexture = HumanoidBabyRenderers26.tex("zombie/drowned_outer_layer_baby");

         for (int i = 0; i < this.layers.size(); i++) {
            if (this.layers.get(i) instanceof DrownedOuterLayer outer) {
               final RenderLayer<Drowned, DrownedModel<Drowned>> adultOuter = outer;
               this.layers
                  .set(
                     i,
                     new RenderLayer<Drowned, DrownedModel<Drowned>>(this) {
                        public void render(
                           PoseStack poseStack,
                           MultiBufferSource buffers,
                           int light,
                           Drowned drowned,
                           float limbSwing,
                           float limbSwingAmount,
                           float partialTick,
                           float ageInTicks,
                           float netHeadYaw,
                           float headPitch
                        ) {
                           if (!drowned.isBaby()) {
                              adultOuter.render(poseStack, buffers, light, drowned, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                           } else {
                              coloredCutoutModelCopyLayerRender(
                                 this.getParentModel(),
                                 babyOuter,
                                 outerTexture,
                                 poseStack,
                                 buffers,
                                 light,
                                 drowned,
                                 limbSwing,
                                 limbSwingAmount,
                                 ageInTicks,
                                 netHeadYaw,
                                 headPitch,
                                 partialTick,
                                 -1
                              );
                           }
                        }
                     }
                  );
            }
         }
      }

      public void render(Drowned drowned, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = drowned.isBaby() ? this.baby : this.adult;
         super.render(drowned, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(Zombie drowned) {
         return drowned.isBaby() ? HumanoidBabyRenderers26.tex("zombie/drowned_baby") : super.getTextureLocation(drowned);
      }
   }

   public static class Husk26 extends HuskRenderer {
      private final ZombieModel<Zombie> adult = (ZombieModel<Zombie>)this.model;
      private final ZombieModel<Zombie> baby;

      public Husk26(Context context) {
         super(context);
         this.baby = new BabyHumanoids.Zombie26<>(context.bakeLayer(HumanoidBabyRenderers26.ZOMBIE_BABY));
         HumanoidBabyRenderers26.wrapArmor(this.layers, this, context, false, BabyHumanoids.Skeleton.ZOMBIE);
      }

      public void render(Zombie husk, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = husk.isBaby() ? this.baby : this.adult;
         super.render(husk, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(Zombie husk) {
         return husk.isBaby() ? HumanoidBabyRenderers26.tex("zombie/husk_baby") : super.getTextureLocation(husk);
      }
   }

   public static class Piglin26 extends PiglinRenderer {
      private static final Map<EntityType<?>, ResourceLocation> BABY_TEXTURES = Map.of(
         EntityType.PIGLIN,
         HumanoidBabyRenderers26.tex("piglin/piglin_baby"),
         EntityType.ZOMBIFIED_PIGLIN,
         HumanoidBabyRenderers26.tex("piglin/zombified_piglin_baby")
      );
      private final PiglinModel<Mob> adult = (PiglinModel<Mob>)this.model;
      private final PiglinModel<Mob> baby;

      public Piglin26(Context context, ModelLayerLocation layer, ModelLayerLocation inner, ModelLayerLocation outer, boolean zombified) {
         super(context, layer, inner, outer, zombified);
         this.baby = new BabyHumanoids.Piglin26<>(context.bakeLayer(HumanoidBabyRenderers26.PIGLIN_BABY));
         if (zombified) {
            this.baby.rightEar.visible = false;
         }

         HumanoidBabyRenderers26.wrapArmor(this.layers, this, context, true, BabyHumanoids.Skeleton.PIGLIN);
      }

      public static HumanoidBabyRenderers26.Piglin26 piglin(Context context) {
         return new HumanoidBabyRenderers26.Piglin26(context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
      }

      public static HumanoidBabyRenderers26.Piglin26 zombified(Context context) {
         return new HumanoidBabyRenderers26.Piglin26(
            context, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true
         );
      }

      public void render(Mob piglin, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = piglin.isBaby() && BABY_TEXTURES.containsKey(piglin.getType()) ? this.baby : this.adult;
         super.render(piglin, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(Mob piglin) {
         ResourceLocation baby = piglin.isBaby() ? BABY_TEXTURES.get(piglin.getType()) : null;
         return baby != null ? baby : super.getTextureLocation(piglin);
      }
   }

   public static class Zombie26 extends ZombieRenderer {
      private final ZombieModel<Zombie> adult = (ZombieModel<Zombie>)this.model;
      private final ZombieModel<Zombie> baby;

      public Zombie26(Context context) {
         super(context);
         this.baby = new BabyHumanoids.Zombie26<>(context.bakeLayer(HumanoidBabyRenderers26.ZOMBIE_BABY));
         HumanoidBabyRenderers26.wrapArmor(this.layers, this, context, false, BabyHumanoids.Skeleton.ZOMBIE);
      }

      public void render(Zombie zombie, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = zombie.isBaby() ? this.baby : this.adult;
         super.render(zombie, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(Zombie zombie) {
         return zombie.isBaby() ? HumanoidBabyRenderers26.tex("zombie/zombie_baby") : super.getTextureLocation(zombie);
      }
   }

   public static class ZombieVillager26 extends ZombieVillagerRenderer {
      private final ZombieVillagerModel<ZombieVillager> adult = (ZombieVillagerModel<ZombieVillager>)this.model;
      private final ZombieVillagerModel<ZombieVillager> baby;

      public ZombieVillager26(Context context) {
         super(context);
         this.baby = new BabyHumanoids.ZombieVillager26<>(context.bakeLayer(HumanoidBabyRenderers26.ZOMBIE_VILLAGER_BABY));
         HumanoidBabyRenderers26.wrapArmor(this.layers, this, context, false, BabyHumanoids.Skeleton.ZOMBIE_VILLAGER);

         for (int i = 0; i < this.layers.size(); i++) {
            if (this.layers.get(i) instanceof VillagerProfessionLayer clothes) {
               this.layers
                  .set(
                     i,
                     new RenderLayer<ZombieVillager, ZombieVillagerModel<ZombieVillager>>(this) {
                        public void render(
                           PoseStack poseStack,
                           MultiBufferSource buffers,
                           int light,
                           ZombieVillager zombie,
                           float limbSwing,
                           float limbSwingAmount,
                           float partialTick,
                           float ageInTicks,
                           float netHeadYaw,
                           float headPitch
                        ) {
                           if (!zombie.isBaby()) {
                              clothes.render(poseStack, buffers, light, zombie, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                           } else if (!zombie.isInvisible()) {
                              ResourceLocation type = BuiltInRegistries.VILLAGER_TYPE.getKey(zombie.getVillagerData().getType());
                              ResourceLocation texture = HumanoidBabyRenderers26.tex("zombie_villager/baby/" + type.getPath());
                              if (Minecraft.getInstance().getResourceManager().getResource(texture).isPresent()) {
                                 renderColoredCutoutModel(this.getParentModel(), texture, poseStack, buffers, light, zombie, -1);
                              }
                           }
                        }
                     }
                  );
            }
         }
      }

      public void render(ZombieVillager zombie, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         this.model = zombie.isBaby() ? this.baby : this.adult;
         super.render(zombie, yaw, partialTick, poseStack, buffers, light);
      }

      public ResourceLocation getTextureLocation(ZombieVillager zombie) {
         return zombie.isBaby() ? HumanoidBabyRenderers26.tex("zombie_villager/zombie_villager_baby") : super.getTextureLocation(zombie);
      }
   }
}
