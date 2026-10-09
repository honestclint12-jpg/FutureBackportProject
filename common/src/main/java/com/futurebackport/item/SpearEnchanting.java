package com.futurebackport.item;

import com.futurebackport.registry.ModEnchantments;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Which enchantments spears accept. 1.20.1 decides by EnchantmentCategory (swords only for weapon enchantments), so
 * this replaces the 1.21 enchantable item tags. Used by the anvil and enchanting table mixins.
 */
public final class SpearEnchanting {

   private SpearEnchanting() {
   }

   private static Set<Enchantment> weaponEnchantments() {
      return Set.of(
         Enchantments.SHARPNESS,
         Enchantments.SMITE,
         Enchantments.BANE_OF_ARTHROPODS,
         Enchantments.FIRE_ASPECT,
         Enchantments.KNOCKBACK,
         Enchantments.MOB_LOOTING,
         Enchantments.SWEEPING_EDGE
      );
   }

   /** True if {@code enchantment} may go on {@code stack} though its category says no. */
   public static boolean allows(Enchantment enchantment, ItemStack stack) {
      return stack.getItem() instanceof SpearItem && (enchantment == ModEnchantments.LUNGE.get() || weaponEnchantments().contains(enchantment));
   }

   /** Fixes up the enchanting table's candidate list for {@code stack}. */
   public static void adjustTableResults(List<EnchantmentInstance> results, int power, ItemStack stack, boolean allowTreasure) {
      if (stack.getItem() instanceof SpearItem) {
         for (Enchantment enchantment : weaponEnchantments()) {
            addIfMissing(results, enchantment, power, allowTreasure);
         }

         addIfMissing(results, ModEnchantments.LUNGE.get(), power, allowTreasure);
      } else if (!stack.is(net.minecraft.world.item.Items.BOOK)) {
         results.removeIf(instance -> instance.enchantment == ModEnchantments.LUNGE.get());
      }
   }

   private static void addIfMissing(List<EnchantmentInstance> results, Enchantment enchantment, int power, boolean allowTreasure) {
      if (results.stream().anyMatch(instance -> instance.enchantment == enchantment)) {
         return;
      }

      if ((enchantment.isTreasureOnly() && !allowTreasure) || !enchantment.isDiscoverable()) {
         return;
      }

      for (int level = enchantment.getMaxLevel(); level >= enchantment.getMinLevel(); level--) {
         if (power >= enchantment.getMinCost(level) && power <= enchantment.getMaxCost(level)) {
            results.add(new EnchantmentInstance(enchantment, level));
            return;
         }
      }
   }
}
