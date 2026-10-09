package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.block.entity.CopperGolemStatueBlockEntity;
import com.futurebackport.client.model.CopperGolemStatueModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;

public class CopperGolemStatueRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity> {
   public static final ModelLayerLocation RUNNING = new ModelLayerLocation(FutureBackport.id("copper_golem"), "running");
   public static final ModelLayerLocation SITTING = new ModelLayerLocation(FutureBackport.id("copper_golem"), "sitting");
   public static final ModelLayerLocation STAR = new ModelLayerLocation(FutureBackport.id("copper_golem"), "star");
   private final Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> models;

   public CopperGolemStatueRenderer(Context context) {
      this.models = bakeModels(context.getModelSet());
   }

   static Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> bakeModels(EntityModelSet modelSet) {
      Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> models = new EnumMap<>(CopperGolemStatueBlock.Pose.class);
      models.put(CopperGolemStatueBlock.Pose.STANDING, new CopperGolemStatueModel(modelSet.bakeLayer(CopperGolemRenderer.LAYER)));
      models.put(CopperGolemStatueBlock.Pose.RUNNING, new CopperGolemStatueModel(modelSet.bakeLayer(RUNNING)));
      models.put(CopperGolemStatueBlock.Pose.SITTING, new CopperGolemStatueModel(modelSet.bakeLayer(SITTING)));
      models.put(CopperGolemStatueBlock.Pose.STAR, new CopperGolemStatueModel(modelSet.bakeLayer(STAR)));
      return models;
   }

   public void render(CopperGolemStatueBlockEntity statue, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      BlockState state = statue.getBlockState();
      if (state.getBlock() instanceof CopperGolemStatueBlock block) {
         Direction var10 = (Direction)state.getValue(CopperGolemStatueBlock.FACING);
         poseStack.pushPose();
         poseStack.translate(0.5F, 0.0F, 0.5F);
         poseStack.mulPose(Axis.YP.rotationDegrees(-var10.getOpposite().toYRot()));
         draw(this.models.get(state.getValue(CopperGolemStatueBlock.POSE)), block.getWeatherState(), poseStack, buffers, light, overlay);
         poseStack.popPose();
      }
   }

   static void draw(CopperGolemStatueModel model, WeatherState weather, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      model.renderToBuffer(poseStack, buffers.getBuffer(model.renderType(CopperGolemRenderer.texture(weather))), light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
      private Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> models;

      public ItemRenderer() {
         super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
      }

      public void onResourceManagerReload(ResourceManager manager) {
         this.models = CopperGolemStatueRenderer.bakeModels(Minecraft.getInstance().getEntityModels());
      }

      public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
         if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CopperGolemStatueBlock block) {
            if (this.models == null) {
               this.models = CopperGolemStatueRenderer.bakeModels(Minecraft.getInstance().getEntityModels());
            }

            CompoundTag blockState = stack.getTagElement("BlockStateTag");
            CopperGolemStatueBlock.Pose pose = blockState == null
               ? null
               : CopperGolemStatueBlock.POSE.getValue(blockState.getString(CopperGolemStatueBlock.POSE.getName())).orElse(null);
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            CopperGolemStatueRenderer.draw(
               this.models.get(pose == null ? CopperGolemStatueBlock.Pose.STANDING : pose), block.getWeatherState(), poseStack, buffers, light, overlay
            );
            poseStack.popPose();
         }
      }
   }
}
