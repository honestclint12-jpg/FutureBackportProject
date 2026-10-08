package com.futurebackport.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class HarnessItem extends Item {
   private final DyeColor color;

   public HarnessItem(DyeColor color, Properties properties) {
      super(properties);
      this.color = color;
   }

   public DyeColor getColor() {
      return this.color;
   }
}
