package com.futurebackport.client.renderer;

import com.futurebackport.block.ShelfBlock;
import com.futurebackport.block.entity.ShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
   private final ItemRenderer itemRenderer;

   public ShelfRenderer(Context context) {
      this.itemRenderer = context.getItemRenderer();
   }

   public void render(ShelfBlockEntity shelf, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      Direction facing = (Direction)shelf.getBlockState().getValue(ShelfBlock.FACING);
      float yRot = -facing.toYRot();
      int seed = (int)shelf.getBlockPos().asLong();
      NonNullList<ItemStack> items = shelf.getItems();

      for (int slot = 0; slot < items.size(); slot++) {
         ItemStack stack = (ItemStack)items.get(slot);
         if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.translate((slot - 1) * 0.3125F, shelf.getAlignItemsToBottom() ? -0.25F : 0.0F, -0.25F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            this.itemRenderer
               .renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, shelf.getLevel(), seed + slot);
            poseStack.popPose();
         }
      }
   }
}
