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
   private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
   private static final ResourceLocation SADDLE_SLOT = ResourceLocation.withDefaultNamespace("container/horse/saddle_slot");
   private static final ResourceLocation ARMOR_ICON = FutureBackport.id("container/slot/nautilus_armor_inventory");
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
         graphics.blitSprite(SADDLE_SLOT, x + 7, y + 17, 18, 18);
         graphics.blitSprite(SLOT, x + 7, y + 35, 18, 18);
         if (((NautilusMenu)this.menu).getSlot(1).getItem().isEmpty()) {
            graphics.blitSprite(ARMOR_ICON, x + 8, y + 36, 16, 16);
         }
      }

      InventoryScreen.renderEntityInInventoryFollowsMouse(
         graphics, x + 26, y + 18, x + 78, y + 70, 13, 0.25F, this.mouseX, this.mouseY, ((NautilusMenu)this.menu).nautilus
      );
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
   }
}
