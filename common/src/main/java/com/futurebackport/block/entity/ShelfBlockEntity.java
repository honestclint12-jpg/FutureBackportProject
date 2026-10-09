package com.futurebackport.block.entity;

import com.futurebackport.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.jetbrains.annotations.Nullable;

public class ShelfBlockEntity extends BlockEntity implements Container {
   private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
   private boolean alignItemsToBottom;

   public ShelfBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.SHELF.get(), pos, state);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.items.clear();
      ContainerHelper.loadAllItems(tag, this.items);
      this.alignItemsToBottom = tag.getBoolean("align_items_to_bottom");
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items, true);
      tag.putBoolean("align_items_to_bottom", this.alignItemsToBottom);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag tag = new CompoundTag();
      ContainerHelper.saveAllItems(tag, this.items, true);
      tag.putBoolean("align_items_to_bottom", this.alignItemsToBottom);
      return tag;
   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public boolean getAlignItemsToBottom() {
      return this.alignItemsToBottom;
   }

   public ItemStack swapItemNoUpdate(int slot, ItemStack held) {
      ItemStack previous = (ItemStack)this.items.get(slot);
      this.items.set(slot, held);
      return previous;
   }

   public void setChanged(@Nullable GameEvent event) {
      super.setChanged();
      if (this.level != null) {
         if (event != null) {
            this.level.gameEvent(event, this.worldPosition, Context.of(this.getBlockState()));
         }

         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public void setChanged() {
      this.setChanged(GameEvent.BLOCK_ACTIVATE);
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int slot) {
      return (ItemStack)this.items.get(slot);
   }

   public ItemStack removeItem(int slot, int amount) {
      ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);
      if (!removed.isEmpty()) {
         this.setChanged();
      }

      return removed;
   }

   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   public void setItem(int slot, ItemStack stack) {
      this.items.set(slot, stack);
      this.setChanged();
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public void clearContent() {
      this.items.clear();
   }



}
