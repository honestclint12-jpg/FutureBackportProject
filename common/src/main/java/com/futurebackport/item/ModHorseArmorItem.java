package com.futurebackport.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HorseArmorItem;

/** Horse armor whose texture lives in any namespace (vanilla's constructor only takes a minecraft texture name). */
public class ModHorseArmorItem extends HorseArmorItem {
   private final ResourceLocation texture;

   public ModHorseArmorItem(int protection, ResourceLocation texture, Properties properties) {
      super(protection, texture.getPath(), properties);
      this.texture = texture.withPath(path -> "textures/entity/horse/armor/horse_armor_" + path + ".png");
   }

   @Override
   public ResourceLocation getTexture() {
      return this.texture;
   }
}
