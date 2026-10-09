package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.model.FarmAnimalModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

public class PolarBearRenderer26 extends PolarBearRenderer {
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("polar_bear_baby"), "main");
   private static final ResourceLocation BABY_TEXTURE = FutureBackport.id("textures/entity/bear/polarbear_baby.png");
   private final MobRenderer<PolarBear, PolarBearRenderer26.BabyPolarBearModel> baby;

   public PolarBearRenderer26(Context context) {
      super(context);
      this.baby = new MobRenderer<PolarBear, PolarBearRenderer26.BabyPolarBearModel>(
         context, new PolarBearRenderer26.BabyPolarBearModel(context.bakeLayer(BABY)), 0.45F
      ) {
         public ResourceLocation getTextureLocation(PolarBear bear) {
            return PolarBearRenderer26.BABY_TEXTURE;
         }
      };
   }

   public void render(PolarBear bear, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      if (bear.isBaby()) {
         this.baby.render(bear, yaw, partialTick, poseStack, buffers, light);
      } else {
         super.render(bear, yaw, partialTick, poseStack, buffers, light);
      }
   }

   public static class BabyPolarBearModel extends FarmAnimalModels.Quadruped<PolarBear> {
      private float partialTick;

      public BabyPolarBearModel(ModelPart root) {
         super(root);
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 9).addBox(-4.0F, -3.5F, -6.0F, 8.0F, 7.0F, 12.0F), PartPose.offset(0.0F, 17.5F, 0.0F)
         );
         root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-3.0F, -2.625F, -4.25F, 6.0F, 5.0F, 4.0F)
               .texOffs(20, 3)
               .addBox(-2.0F, 0.375F, -6.25F, 4.0F, 2.0F, 2.0F)
               .texOffs(20, 0)
               .addBox(-4.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F)
               .texOffs(26, 0)
               .addBox(2.0F, -3.625F, -2.75F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, 18.625F, -5.75F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-2.5F, 21.5F, 4.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(12, 34).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(2.5F, 21.5F, 4.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-2.5F, 21.5F, -4.5F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(12, 28).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(2.5F, 21.5F, -4.5F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public void prepareMobModel(PolarBear bear, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(PolarBear bear, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root().getAllParts().forEach(ModelPart::resetPose);
         super.setupAnim(bear, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         float stand = bear.getStandingAnimationScale(this.partialTick);
         stand *= stand;
         float ageScale = 0.5F;
         ModelPart body = this.root().getChild("body");
         ModelPart head = this.root().getChild("head");
         ModelPart rightFront = this.root().getChild("right_front_leg");
         ModelPart leftFront = this.root().getChild("left_front_leg");
         body.xRot -= stand * (float) Math.PI * 0.35F;
         body.y += stand * ageScale * 2.0F;
         rightFront.y -= stand * ageScale * 20.0F;
         rightFront.z += stand * ageScale * 4.0F;
         rightFront.xRot -= stand * (float) Math.PI * 0.45F;
         leftFront.y = rightFront.y;
         leftFront.z = rightFront.z;
         leftFront.xRot -= stand * (float) Math.PI * 0.45F;
         head.y -= stand * 24.0F;
         head.z += stand * 13.0F;
         head.xRot += stand * (float) Math.PI * 0.15F;
      }
   }
}
