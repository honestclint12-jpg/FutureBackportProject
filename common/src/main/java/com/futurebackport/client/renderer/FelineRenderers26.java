package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class FelineRenderers26 {
   public static final ModelLayerLocation BABY = new ModelLayerLocation(FutureBackport.id("feline_baby"), "main");

   private FelineRenderers26() {
   }

   public static class BabyFelineModel<T extends Animal> extends HierarchicalModel<T> {
      private final ModelPart root;
      private final ModelPart head;
      private final ModelPart body;
      private final ModelPart tail1;
      private final ModelPart tail2;
      private final ModelPart leftHindLeg;
      private final ModelPart rightHindLeg;
      private final ModelPart leftFrontLeg;
      private final ModelPart rightFrontLeg;
      private float partialTick;

      public BabyFelineModel(ModelPart root) {
         this.root = root;
         this.head = root.getChild("head");
         this.body = root.getChild("body");
         this.tail1 = root.getChild("tail1");
         this.tail2 = root.getChild("tail2");
         this.leftHindLeg = root.getChild("left_hind_leg");
         this.rightHindLeg = root.getChild("right_hind_leg");
         this.leftFrontLeg = root.getChild("left_front_leg");
         this.rightFrontLeg = root.getChild("right_front_leg");
      }

      public static LayerDefinition createBabyLayer() {
         MeshDefinition mesh = new MeshDefinition();
         PartDefinition root = mesh.getRoot();
         root.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
               .texOffs(0, 0)
               .addBox(-2.5F, -3.0F, -2.875F, 5.0F, 4.0F, 4.0F)
               .texOffs(18, 0)
               .addBox(-2.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
               .texOffs(24, 0)
               .addBox(1.0F, -4.0F, -0.875F, 1.0F, 1.0F, 2.0F)
               .texOffs(18, 3)
               .addBox(-1.5F, -1.0F, -3.875F, 3.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, 20.0F, -3.125F)
         );
         root.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(18, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, -1.5F)
         );
         root.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(12, 18).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, -1.5F)
         );
         root.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(18, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, 2.5F)
         );
         root.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, -1.5F, -3.5F, 4.0F, 3.0F, 7.0F), PartPose.offset(0.0F, 20.5F, 0.5F)
         );
         root.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(12, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 22.0F, 2.5F)
         );
         root.addOrReplaceChild(
            "tail1",
            CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -0.107F, 0.0849F, 1.0F, 1.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 19.107F, 3.9151F, -0.567232F, 0.0F, 0.0F)
         );
         root.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.ZERO);
         return LayerDefinition.create(mesh, 32, 32);
      }

      public ModelPart root() {
         return this.root;
      }

      public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
         this.partialTick = partialTick;
      }

      public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
         this.root.getAllParts().forEach(ModelPart::resetPose);
         float ageScale = 0.5F;
         boolean crouching = entity.isCrouching();
         boolean sprinting = entity.isSprinting();
         boolean sitting = entity instanceof Cat cat && cat.isInSittingPose();
         if (crouching) {
            this.body.y += 1.0F * ageScale;
            this.head.y += 2.0F * ageScale;
            this.tail1.y += 1.0F * ageScale;
            this.tail2.y += -4.0F * ageScale;
            this.tail2.z += 2.0F * ageScale;
            this.tail1.xRot = (float) (Math.PI / 2);
            this.tail2.xRot = (float) (Math.PI / 2);
         } else if (sprinting) {
            this.tail2.y = this.tail1.y;
            this.tail2.z += 2.0F * ageScale;
            this.tail1.xRot = (float) (Math.PI / 2);
            this.tail2.xRot = (float) (Math.PI / 2);
         }

         this.head.xRot = headPitch * (float) (Math.PI / 180.0);
         this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
         if (!sitting) {
            if (sprinting) {
               this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
               this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + 0.3F) * limbSwingAmount;
               this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI + 0.3F) * limbSwingAmount;
               this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
               this.tail2.xRot = 1.7278761F + (float) (Math.PI / 10) * Mth.cos(limbSwing) * limbSwingAmount;
            } else {
               this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
               this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
               this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
               this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
               this.tail2.xRot = 1.7278761F + (crouching ? 0.47123894F : (float) (Math.PI / 4)) * Mth.cos(limbSwing) * limbSwingAmount;
            }
         } else {
            this.body.xRot += -0.43633232F;
            this.body.y++;
            this.head.z += 0.75F;
            this.tail1.xRot += 0.5454154F;
            this.tail1.y += 4.0F;
            this.tail1.z -= 0.9F;
            this.leftHindLeg.z -= 0.9F;
            this.rightHindLeg.z -= 0.9F;
         }

         if (entity instanceof Cat catx) {
            float lie = catx.getLieDownAmount(this.partialTick);
            float lieTail = catx.getLieDownAmountTail(this.partialTick);
            float relax = catx.getRelaxStateOneAmount(this.partialTick);
            if (lie > 0.0F) {
               this.body.x++;
               this.head.xRot = Mth.rotLerp(lie, this.head.xRot, (float) (Math.PI / 18));
               this.head.zRot = Mth.rotLerp(lie, this.head.zRot, (float) (-Math.PI * 5.0 / 12.0));
               this.head.x++;
               this.head.y += 0.75F;
               this.head.z -= 0.5F;
               this.rightFrontLeg.xRot = (float) (-Math.PI / 4);
               this.rightFrontLeg.x += 3.5F;
               this.rightFrontLeg.y -= 0.5F;
               this.leftFrontLeg.xRot = (float) (-Math.PI / 2);
               this.leftFrontLeg.x++;
               this.leftFrontLeg.y--;
               this.leftFrontLeg.z -= 2.0F;
               this.rightHindLeg.xRot = (float) (Math.PI * 2.0 / 9.0);
               this.rightHindLeg.yRot = (float) (Math.PI / 9);
               this.rightHindLeg.zRot = (float) (-Math.PI / 9);
               this.rightHindLeg.x += 2.5F;
               this.rightHindLeg.y -= 0.25F;
               this.rightHindLeg.z += 0.5F;
               this.leftHindLeg.x++;
               this.leftHindLeg.z--;
               this.tail1.xRot = this.tail1.xRot + Mth.rotLerp(lieTail, this.tail1.xRot, (float) (-Math.PI / 6));
               this.tail1.yRot = this.tail1.yRot + Mth.rotLerp(lieTail, this.tail1.yRot, 0.0F);
               this.tail1.zRot = this.tail1.zRot + Mth.rotLerp(lieTail, this.tail1.zRot, (float) (-Math.PI / 18));
               this.tail1.x++;
               this.tail1.y += 0.5F;
               this.tail1.z -= 0.25F;
            }

            if (relax > 0.0F) {
               this.head.xRot = Mth.rotLerp(relax, this.head.xRot, -0.58177644F);
            }
         }
      }
   }

   private static class BabyRenderer<T extends Animal> extends MobRenderer<T, FelineRenderers26.BabyFelineModel<T>> {
      private final Function<T, ResourceLocation> texture;

      BabyRenderer(Context context, Function<T, ResourceLocation> texture) {
         super(context, new FelineRenderers26.BabyFelineModel(context.bakeLayer(FelineRenderers26.BABY)), 0.4F);
         this.texture = texture;
      }

      public ResourceLocation getTextureLocation(T entity) {
         return this.texture.apply(entity);
      }

      protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick) {
         super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick);
         if (entity instanceof Cat cat) {
            float lie = cat.getLieDownAmount(partialTick);
            if (lie > 0.0F) {
               poseStack.translate(0.4F * lie, 0.15F * lie, 0.1F * lie);
               poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(lie, 0.0F, 90.0F)));
               BlockPos pos = entity.blockPosition();

               for (Player player : entity.level().getEntitiesOfClass(Player.class, new AABB(pos).inflate(2.0, 2.0, 2.0))) {
                  if (player.isSleeping()) {
                     poseStack.translate(0.15F * lie, 0.0F, 0.0F);
                     break;
                  }
               }
            }
         }
      }
   }

   public static class Cat26 extends CatRenderer {
      private final Map<ResourceLocation, ResourceLocation> babyTextures = new HashMap<>();
      private final FelineRenderers26.BabyRenderer<Cat> baby;

      public Cat26(Context context) {
         super(context);
         this.baby = new FelineRenderers26.BabyRenderer<>(context, cat -> this.babyTextures.computeIfAbsent(cat.getResourceLocation(), adult -> {
            ResourceLocation baby = FutureBackport.id(adult.getPath().replace("/cat/", "/cat/cat_").replace(".png", "_baby.png"));
            return Minecraft.getInstance().getResourceManager().getResource(baby).isPresent() ? baby : adult;
         }));
         this.baby.addLayer(new FelineRenderers26.CollarLayer(this.baby));
      }

      public void render(Cat cat, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (cat.isBaby()) {
            this.baby.render(cat, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(cat, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }

   private static class CollarLayer extends RenderLayer<Cat, FelineRenderers26.BabyFelineModel<Cat>> {
      private static final ResourceLocation COLLAR = FutureBackport.id("textures/entity/cat/cat_collar_baby.png");

      CollarLayer(RenderLayerParent<Cat, FelineRenderers26.BabyFelineModel<Cat>> parent) {
         super(parent);
      }

      public void render(
         PoseStack poseStack,
         MultiBufferSource buffers,
         int light,
         Cat cat,
         float limbSwing,
         float limbSwingAmount,
         float partialTick,
         float ageInTicks,
         float netHeadYaw,
         float headPitch
      ) {
         if (cat.isTame() && !cat.isInvisible()) {
            poseStack.pushPose();
            poseStack.scale(1.01F, 1.01F, 1.01F);
            float[] collar = cat.getCollarColor().getTextureDiffuseColors();
            ((FelineRenderers26.BabyFelineModel)this.getParentModel())
               .renderToBuffer(
                  poseStack,
                  buffers.getBuffer(RenderType.entityCutoutNoCull(COLLAR)),
                  light,
                  OverlayTexture.NO_OVERLAY,
                  collar[0],
                  collar[1],
                  collar[2],
                  1.0F
               );
            poseStack.popPose();
         }
      }
   }

   public static class Ocelot26 extends OcelotRenderer {
      private static final ResourceLocation BABY_TEXTURE = FutureBackport.id("textures/entity/cat/ocelot_baby.png");
      private final FelineRenderers26.BabyRenderer<Ocelot> baby;

      public Ocelot26(Context context) {
         super(context);
         this.baby = new FelineRenderers26.BabyRenderer<>(context, ocelot -> BABY_TEXTURE);
      }

      public void render(Ocelot ocelot, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
         if (ocelot.isBaby()) {
            this.baby.render(ocelot, yaw, partialTick, poseStack, buffers, light);
         } else {
            super.render(ocelot, yaw, partialTick, poseStack, buffers, light);
         }
      }
   }
}
