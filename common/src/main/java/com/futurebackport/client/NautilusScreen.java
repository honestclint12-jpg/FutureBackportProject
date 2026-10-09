package com.futurebackport.client;

import com.futurebackport.FutureBackport;
import com.futurebackport.menu.NautilusMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NautilusScreen extends AbstractContainerScreen<NautilusMenu> {
   private static final ResourceLocation BACKGROUND = FutureBackport.id("textures/gui/container/nautilus.png");
   /** 1.20.1 has no GUI sprite atlas: the slot frames come from the horse screen texture, as HorseInventoryScreen draws them. */
   private static final ResourceLocation HORSE_INVENTORY = new ResourceLocation("textures/gui/container/horse.png");
   private static final ResourceLocation ARMOR_ICON = FutureBackport.id("textures/gui/sprites/container/slot/nautilus_armor_inventory.png");
   private float mouseX;
   private float mouseY;

   public NautilusScreen(NautilusMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
   }

   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      int x = this.leftPos;
      int y = this.topPos;
      graphics.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);
      if (((NautilusMenu)this.menu).nautilus.isSaddleable()) {
         graphics.blit(HORSE_INVENTORY, x + 7, y + 17, 18, 220, 18, 18);
         graphics.blit(HORSE_INVENTORY, x + 7, y + 35, 0, 220, 18, 18);
         if (((NautilusMenu)this.menu).getSlot(1).getItem().isEmpty()) {
            graphics.blit(ARMOR_ICON, x + 8, y + 36, 0, 0, 16, 16, 16, 16);
         }
      }

      InventoryScreen.renderEntityInInventoryFollowsMouse(
         graphics, x + 52, y + 60, 13, (float)(x + 52) - this.mouseX, (float)(y + 25) - this.mouseY, ((NautilusMenu)this.menu).nautilus
      );
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
   }
}
