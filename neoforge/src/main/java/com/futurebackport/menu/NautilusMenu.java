package com.futurebackport.menu;

import com.futurebackport.platform.Services;

import com.futurebackport.entity.nautilus.AbstractNautilus;
import com.futurebackport.item.NautilusArmorItem;
import com.futurebackport.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NautilusMenu extends AbstractContainerMenu {
   public final AbstractNautilus nautilus;
   private final Container container;

   public NautilusMenu(int id, Inventory playerInventory, final AbstractNautilus nautilus) {
      super((MenuType)ModMenus.NAUTILUS.get(), id);
      this.nautilus = nautilus;
      this.container = nautilus.getInventory();
      this.container.startOpen(playerInventory.player);
      this.addSlot(new Slot(this.container, 0, 8, 18) {
         public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.SADDLE) && !this.hasItem() && nautilus.isSaddleable();
         }

         public boolean isActive() {
            return nautilus.isSaddleable();
         }
      });
      this.addSlot(new Slot(this.container, 1, 8, 36) {
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof NautilusArmorItem && nautilus.isSaddleable();
         }

         public boolean isActive() {
            return nautilus.isSaddleable();
         }

         public int getMaxStackSize() {
            return 1;
         }
      });

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
      }
   }

   public static NautilusMenu fromNetwork(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
      return new NautilusMenu(id, playerInventory, (AbstractNautilus)playerInventory.player.level().getEntity(buf.readVarInt()));
   }

   public static void open(ServerPlayer player, AbstractNautilus nautilus) {
      Services.PLATFORM.openMenu(
         player,
         new SimpleMenuProvider((id, inventory, p) -> new NautilusMenu(id, inventory, nautilus), nautilus.getDisplayName()),
         buf -> buf.writeVarInt(nautilus.getId())
      );
   }

   public boolean stillValid(Player player) {
      return this.nautilus.isAlive() && player.canInteractWithEntity(this.nautilus, 4.0);
   }

   public void removed(Player player) {
      super.removed(player);
      this.container.stopOpen(player);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      Slot slot = (Slot)this.slots.get(index);
      if (!slot.hasItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = slot.getItem();
         ItemStack copy = stack.copy();
         if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (((Slot)this.slots.get(0)).mayPlace(stack)) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (!((Slot)this.slots.get(1)).mayPlace(stack)) {
               return ItemStack.EMPTY;
            }

            if (!this.moveItemStackTo(stack, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         }

         if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return copy;
      }
   }
}
