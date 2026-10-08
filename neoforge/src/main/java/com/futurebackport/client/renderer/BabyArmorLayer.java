package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.component.DyedItemColor;

public class BabyArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
   public static final ModelLayerLocation HUMANOID_INNER = layer("inner");
   public static final ModelLayerLocation HUMANOID_OUTER = layer("outer");
   public static final ModelLayerLocation PIGLIN_INNER = layer("piglin_inner");
   public static final ModelLayerLocation PIGLIN_OUTER = layer("piglin_outer");
   private static final Map<EquipmentSlot, Set<String>> PARTS = Map.of(
      EquipmentSlot.HEAD,
      Set.of("head"),
      EquipmentSlot.CHEST,
      Set.of("body", "left_arm", "right_arm"),
      EquipmentSlot.LEGS,
      Set.of("left_leg", "right_leg", "waist"),
      EquipmentSlot.FEET,
      Set.of("left_foot", "right_foot")
   );
   private static final Map<ResourceLocation, ResourceLocation> TEXTURES = new HashMap<>();
   private final RenderLayer<T, M> adultLayer;
   private final BabyArmorLayer.ArmorModel inner;
   private final BabyArmorLayer.ArmorModel outer;
   private final float[] bodySkeleton;

   private static ModelLayerLocation layer(String name) {
      return new ModelLayerLocation(FutureBackport.id("humanoid_baby_armor"), name);
   }

   public BabyArmorLayer(RenderLayerParent<T, M> parent, RenderLayer<T, M> adultLayer, ModelPart inner, ModelPart outer, float[] bodySkeleton) {
      super(parent);
      this.adultLayer = adultLayer;
      this.inner = new BabyArmorLayer.ArmorModel(inner);
      this.outer = new BabyArmorLayer.ArmorModel(outer);
      this.bodySkeleton = bodySkeleton;
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource buffers,
      int light,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (!entity.isBaby()) {
         this.adultLayer.render(poseStack, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
      } else {
         for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slot) {
               BabyArmorLayer.ArmorModel model = slot == EquipmentSlot.LEGS ? this.inner : this.outer;
               model.pose((HumanoidModel<?>)this.getParentModel(), this.bodySkeleton, PARTS.get(slot));

               for (Layer layer : ((ArmorMaterial)armor.getMaterial().value()).layers()) {
                  ResourceLocation texture = babyTexture(layer.texture(false));
                  if (texture != null) {
                     int color = layer.dyeable() ? ARGB32.opaque(DyedItemColor.getOrDefault(stack, -6265536)) : -1;
                     model.root.render(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, color);
                  }
               }

               if (stack.hasFoil()) {
                  model.root.render(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
               }
            }
         }
      }
   }

   private static ResourceLocation babyTexture(ResourceLocation adultTexture) {
      return TEXTURES.computeIfAbsent(adultTexture, k -> {
         String path = adultTexture.getPath();
         int start = path.lastIndexOf(47) + 1;
         int end = path.indexOf("_layer_");
         if (end < start) {
            return null;
         } else {
            boolean overlay = path.endsWith("_overlay.png");
            String name = path.substring(start, end);
            if (name.equals("turtle")) {
               name = "turtle_scute";
            }

            ResourceLocation texture = FutureBackport.id("textures/entity/equipment/humanoid_baby/" + name + (overlay ? "_overlay" : "") + ".png");
            return Minecraft.getInstance().getResourceManager().getResource(texture).isPresent() ? texture : null;
         }
      });
   }

   public static LayerDefinition createMesh(CubeDeformation g, PartPose armOffset) {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, g), PartPose.offset(0.0F, 15.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, g), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "waist", CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F, g.extend(-0.1F)), PartPose.offset(0.0F, 19.0F, 0.0F)
      );
      root.addOrReplaceChild(
         "right_arm",
         CubeListBuilder.create().texOffs(30, 25).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g),
         PartPose.offset(-3.5F - armOffset.x, 15.5F + armOffset.y, armOffset.z)
      );
      root.addOrReplaceChild(
         "left_arm",
         CubeListBuilder.create().texOffs(30, 17).addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, g),
         PartPose.offset(3.5F + armOffset.x, 15.5F + armOffset.y, armOffset.z)
      );
      root.addOrReplaceChild(
         "inner_body", CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, g), PartPose.offset(0.0F, 18.0F, 0.0F)
      );
      PartDefinition leftLeg = root.addOrReplaceChild(
         "left_leg",
         CubeListBuilder.create().texOffs(18, 24).addBox(-2.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)),
         PartPose.offset(1.5F, 20.0F, 0.5F)
      );
      PartDefinition rightLeg = root.addOrReplaceChild(
         "right_leg",
         CubeListBuilder.create().texOffs(18, 17).addBox(-1.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, g.extend(-0.1F)),
         PartPose.offset(-1.5F, 20.0F, 0.5F)
      );
      leftLeg.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g), PartPose.ZERO);
      rightLeg.addOrReplaceChild(
         "left_foot", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-1.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, g).mirror(false), PartPose.ZERO
      );
      return LayerDefinition.create(mesh, 64, 64);
   }

   private static class ArmorModel {
      private static final String[][] FOLLOWS = new String[][]{
         {"head"}, {"body", "inner_body", "waist"}, {"right_arm"}, {"left_arm"}, {"right_leg"}, {"left_leg"}
      };
      final ModelPart root;
      private final Map<String, ModelPart> parts = new HashMap<>();
      private final Map<String, PartPose> rest = new HashMap<>();

      ArmorModel(ModelPart root) {
         this.root = root;

         for (String[] names : FOLLOWS) {
            for (String name : names) {
               ModelPart part = root.getChild(name);
               this.parts.put(name, part);
               this.rest.put(name, part.storePose());
            }
         }

         this.parts.put("left_foot", root.getChild("right_leg").getChild("left_foot"));
         this.parts.put("right_foot", root.getChild("left_leg").getChild("right_foot"));
      }

      void pose(HumanoidModel<?> body, float[] skeleton, Set<String> visible) {
         ModelPart[] bodyParts = new ModelPart[]{body.head, body.body, body.rightArm, body.leftArm, body.rightLeg, body.leftLeg};

         for (int i = 0; i < FOLLOWS.length; i++) {
            ModelPart source = bodyParts[i];

            for (String name : FOLLOWS[i]) {
               ModelPart part = this.parts.get(name);
               PartPose r = this.rest.get(name);
               part.x = r.x + source.x - skeleton[i * 3];
               part.y = r.y + source.y - skeleton[i * 3 + 1];
               part.z = r.z + source.z - skeleton[i * 3 + 2];
               part.xRot = source.xRot;
               part.yRot = source.yRot;
               part.zRot = source.zRot;
            }
         }

         this.parts.forEach((namex, partx) -> partx.visible = visible.contains(namex) || namex.equals("left_leg") || namex.equals("right_leg"));
         this.parts.get("left_leg").skipDraw = !visible.contains("left_leg");
         this.parts.get("right_leg").skipDraw = !visible.contains("right_leg");
         this.parts.get("inner_body").visible = false;
      }
   }
}
