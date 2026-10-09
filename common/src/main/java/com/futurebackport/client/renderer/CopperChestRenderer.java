package com.futurebackport.client.renderer;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CopperChestBlock;
import com.futurebackport.block.entity.CopperChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestRenderer extends ChestRenderer<CopperChestBlockEntity> {
   public CopperChestRenderer(Context context) {
      super(context);
   }

   /** NeoForge's hook for the chest texture; Fabric calls {@link #material} through a mixin instead. */
   protected Material getMaterial(CopperChestBlockEntity chest, ChestType type) {
      return material(chest, type);
   }

   public static Material material(CopperChestBlockEntity chest, ChestType type) {
      WeatherState state = chest.getBlockState().getBlock() instanceof CopperChestBlock copper ? copper.getWeatherState() : WeatherState.UNAFFECTED;
      String stage = state == WeatherState.UNAFFECTED ? "" : "_" + state.name().toLowerCase(Locale.ROOT);

      String half = switch (type) {
         case LEFT -> "_left";
         case RIGHT -> "_right";
         default -> "";
      };
      return new Material(Sheets.CHEST_SHEET, FutureBackport.id("entity/chest/copper" + stage + half));
   }

   public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
      public ItemRenderer() {
         super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
      }

      public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
         if (stack.getItem() instanceof BlockItem blockItem) {
            CopperChestBlockEntity chest = new CopperChestBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
            Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(chest, poseStack, buffer, packedLight, packedOverlay);
         }
      }

      public void onResourceManagerReload(ResourceManager resourceManager) {
      }
   }
}
