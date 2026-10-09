package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerRenderer26 extends VillagerRenderer {
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("villager_baby"), "main");
   private final VillagerRenderer26.BabyRenderer baby;

   public VillagerRenderer26(Context context) {
      super(context);
      this.baby = new VillagerRenderer26.BabyRenderer(context);
   }

   public void render(Villager villager, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      if (villager.isBaby()) {
         this.baby.render(villager, yaw, partialTick, poseStack, buffers, light);
      } else {
         super.render(villager, yaw, partialTick, poseStack, buffers, light);
      }
   }

   private static class BabyRenderer extends MobRenderer<Villager, VillagerRenderer26.BabyVillagerModel> {
      private static final ResourceLocation TEXTURE = FutureBackport.id("textures/entity/villager/villager_baby.png");

      BabyRenderer(Context context) {
         super(context, new VillagerRenderer26.BabyVillagerModel(context.bakeLayer(VillagerRenderer26.BABY)), 0.25F);
         this.addLayer(new VillagerRenderer26.TypeLayer(this));
      }

      public ResourceLocation getTextureLocation(Villager villager) {
         return TEXTURE;
      }
   }

   public static class BabyVillagerModel extends HierarchicalModel<Villager> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart rightLeg;
      private final ModelPart leftLeg;

      public BabyVillagerModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.rightLeg = root.getChild("right_leg");
         this.leftLeg = root.getChild("left_leg");
      }

      public static LayerDefinition createBodyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         PartDefinition arms = root.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, 17.5F, 0.0F));
         arms.addOrReplaceChild(
            "right_hand",
            CubeListBuilder.create()
               .texOffs(36, 15)
               .addBox(-1.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F)
               .texOffs(16, 15)
               .addBox(5.0F, -2.4925F, -1.8401F, 2.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(-3.0F, 1.4025F, -0.9599F, -1.0472F, 0.0F, 0.0F)
         );
         arms.addOrReplaceChild(
            "middlearm_r1",
            CubeListBuilder.create().texOffs(24, 17).addBox(-2.0F, -0.9924F, -0.9825F, 4.0F, 2.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 0.9024F, -1.8175F, -1.0472F, 0.0F, 0.0F)
         );
         root.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-1.0F, 21.5F, 0.0F)
         );
         root.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(1.0F, 21.5F, 0.0F)
         );
         PartDefinition head = root.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -3.5F, 8.0F, 8.0F, 7.0F), PartPose.offset(0.0F, 16.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "hat",
            CubeListBuilder.create().texOffs(0, 30).addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, -4.0F, 0.0F)
         );
         head.addOrReplaceChild(
            "hat_rim", CubeListBuilder.create().texOffs(0, 45).addBox(-7.0F, -0.5F, -6.0F, 14.0F, 1.0F, 12.0F), PartPose.offset(0.0F, -4.5F, 0.0F)
         );
         head.addOrReplaceChild(
            "nose", CubeListBuilder.create().texOffs(23, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -2.0F, -4.0F)
         );
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 15).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 18.75F, 0.0F)
         );
         root.addOrReplaceChild(
            "bb_main",
            CubeListBuilder.create().texOffs(16, 21).addBox(-2.5F, -8.0F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.2F)),
            PartPose.offset(0.5F, 24.0F, 0.0F)
         );
         return LayerDefinition.create(mesh, 64, 64);
      }

      public ModelPart root() {
         return this.root;
      }

      public void setupAnim(Villager villager, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         if (villager.getUnhappyCounter() > 0) {
            this.head.zRot = 0.3F * Mth.sin(0.45F * ageInTicks);
            this.head.xRot = 0.4F;
         }

         this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
         this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
      }
   }

   private static class TypeLayer extends RenderLayer<Villager, VillagerRenderer26.BabyVillagerModel> {
      private final Map<VillagerType, Optional<ResourceLocation>> textures = new HashMap<>();

      TypeLayer(RenderLayerParent<Villager, VillagerRenderer26.BabyVillagerModel> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Villager villager,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (!villager.isInvisible()) {
            this.textures.computeIfAbsent(villager.getVillagerData().getType(), type -> {
               ResourceLocation key = BuiltInRegistries.VILLAGER_TYPE.getKey(type);
               ResourceLocation texture = FutureBackport.id("textures/entity/villager/baby/" + key.getPath() + ".png");
               return Minecraft.getInstance().getResourceManager().getResource(texture).map(r -> texture);
            }).ifPresent(texture -> renderColoredCutoutModel(this.getParentModel(), texture, poseStack, buffers, light, villager, 1.0F, 1.0F, 1.0F));
         }
      }
   }
}
