package com.futurebackport.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** Lunge as a code enchantment (1.20.1 has no data-driven enchantments). Spears only; costs match the 1.21 data. */
public class LungeEnchantment extends Enchantment {

   public LungeEnchantment() {
      super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
   }

   @Override
   public int getMinCost(int level) {
      return 5 + (level - 1) * 8;
   }

   @Override
   public int getMaxCost(int level) {
      return 25 + (level - 1) * 8;
   }

   @Override
   public int getMaxLevel() {
      return 3;
   }

   @Override
   public boolean canEnchant(ItemStack stack) {
      return stack.getItem() instanceof SpearItem;
   }
}
