package com.futurebackport.client.renderer;

import com.futurebackport.client.util.ArgbColor;
import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.FarmAnimalModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class SheepRenderer26 extends MobRenderer<Sheep, EntityModel<Sheep>> {
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("sheep_baby"), "main");
   private static final ResourceLocation ADULT_TEXTURE = new ResourceLocation("textures/entity/sheep/sheep.png");
   private static final ResourceLocation BABY_TEXTURE = FutureBackport.id("textures/entity/sheep/sheep_baby.png");
   private static final ResourceLocation ADULT_WOOL = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
   private static final ResourceLocation BABY_WOOL = FutureBackport.id("textures/entity/sheep/sheep_wool_baby.png");
   private static final ResourceLocation UNDERCOAT = FutureBackport.id("textures/entity/sheep/sheep_wool_undercoat.png");
   private final SheepModel<Sheep> adult = (SheepModel<Sheep>)this.model;
   private final SheepRenderer26.BabySheepModel baby;

   public SheepRenderer26(Context context) {
      super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
      this.baby = new SheepRenderer26.BabySheepModel(context.bakeLayer(BABY));
      this.addLayer(new SheepRenderer26.UndercoatLayer(this, new SheepModel(context.bakeLayer(ModelLayers.SHEEP))));
      this.addLayer(
         new SheepRenderer26.WoolLayer(
            this, new SheepFurModel(context.bakeLayer(ModelLayers.SHEEP_FUR)), new SheepRenderer26.BabySheepModel(context.bakeLayer(BABY))
         )
      );
   }

   public void render(Sheep sheep, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      this.model = (EntityModel)(sheep.isBaby() ? this.baby : this.adult);
      super.render(sheep, yaw, partialTick, poseStack, buffers, light);
   }

   public ResourceLocation getTextureLocation(Sheep sheep) {
      return sheep.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
   }

   static int woolColor(Sheep sheep, float partialTick) {
      if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) {
         int k = sheep.tickCount / 25 + sheep.getId();
         int count = DyeColor.values().length;
         float f = (sheep.tickCount % 25 + partialTick) / 25.0F;
         float[] from = Sheep.getColorArray(DyeColor.byId(k % count));
         float[] to = Sheep.getColorArray(DyeColor.byId((k + 1) % count));
         return ArgbColor.pack(from[0] * (1.0F - f) + to[0] * f, from[1] * (1.0F - f) + to[1] * f, from[2] * (1.0F - f) + to[2] * f, 1.0F);
      } else {
         float[] color = Sheep.getColorArray(sheep.getColor());
         return ArgbColor.pack(color[0], color[1], color[2], 1.0F);
      }
   }

   public static class BabySheepModel extends FarmAnimalModels.Quadruped<Sheep> {
      public BabySheepModel(ModelPart root) {
         super(root);
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -2.0F, -4.5F, 6.0F, 4.0F, 9.0F), PartPose.offset(0.0F, 17.0F, 0.5F)
         );
         root.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.5F, -3.5F, 5.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 15.5F, -2.5F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-2.0F, 19.0F, 3.0F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(24, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(2.0F, 19.0F, 3.0F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(-2.0F, 19.0F, -2.0F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(24, 5).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F), PartPose.offset(2.0F, 19.0F, -2.0F)
         );
         return LayerDefinition.create(mesh, 64, 32);
      }

      public void setupAnim(Sheep sheep, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root().getAllParts().forEach(ModelPart::resetPose);
         super.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         float partialTick = ageInTicks - sheep.tickCount;
         ModelPart head = this.root().getChild("head");
         head.y = head.y + sheep.getHeadEatPositionScale(partialTick) * 9.0F * 0.5F;
         float eatAngle = sheep.getHeadEatAngleScale(partialTick);
         if (eatAngle != 0.0F) {
            head.xRot = eatAngle;
         }
      }
   }

   private static class UndercoatLayer extends RenderLayer<Sheep, EntityModel<Sheep>> {
      private final SheepModel<Sheep> model;

      UndercoatLayer(RenderLayerParent<Sheep, EntityModel<Sheep>> parent, SheepModel<Sheep> model) {
         super(parent);
         this.model = model;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Sheep sheep,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         boolean jeb = sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString());
         if (!sheep.isInvisible() && !sheep.isBaby() && (jeb || sheep.getColor() != DyeColor.WHITE)) {
            coloredCutoutModelCopyLayerRender(
               this.getParentModel(),
               this.model,
               SheepRenderer26.UNDERCOAT,
               poseStack,
               buffers,
               light,
               sheep,
               limbSwing,
               limbSwingAmount,
               ageInTicks,
               netHeadYaw,
               headPitch,
               partialTick,
               ArgbColor.red(SheepRenderer26.woolColor(sheep, partialTick)), ArgbColor.green(SheepRenderer26.woolColor(sheep, partialTick)), ArgbColor.blue(SheepRenderer26.woolColor(sheep, partialTick)));
         }
      }
   }

   private static class WoolLayer extends RenderLayer<Sheep, EntityModel<Sheep>> {
      private final SheepFurModel<Sheep> adultWool;
      private final SheepRenderer26.BabySheepModel babyWool;

      WoolLayer(RenderLayerParent<Sheep, EntityModel<Sheep>> parent, SheepFurModel<Sheep> adultWool, SheepRenderer26.BabySheepModel babyWool) {
         super(parent);
         this.adultWool = adultWool;
         this.babyWool = babyWool;
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Sheep sheep,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!sheep.isSheared() && !sheep.isInvisible()) {
            EntityModel<Sheep> wool = (EntityModel<Sheep>)(sheep.isBaby() ? this.babyWool : this.adultWool);
            coloredCutoutModelCopyLayerRender(
               this.getParentModel(),
               wool,
               sheep.isBaby() ? SheepRenderer26.BABY_WOOL : SheepRenderer26.ADULT_WOOL,
               poseStack,
               buffers,
               light,
               sheep,
               limbSwing,
               limbSwingAmount,
               ageInTicks,
               netHeadYaw,
               headPitch,
               partialTick,
               ArgbColor.red(SheepRenderer26.woolColor(sheep, partialTick)), ArgbColor.green(SheepRenderer26.woolColor(sheep, partialTick)), ArgbColor.blue(SheepRenderer26.woolColor(sheep, partialTick)));
         }
      }
   }
}
