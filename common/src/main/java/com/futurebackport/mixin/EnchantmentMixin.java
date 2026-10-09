package com.futurebackport.mixin;

import com.futurebackport.item.SpearEnchanting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Anvils (and enchant_randomly loot) accept the spear's weapon enchantments. */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

   @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
   private void futurebackport$spearEnchantments(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      if (SpearEnchanting.allows((Enchantment)(Object)this, stack)) {
         cir.setReturnValue(true);
      }
   }
}
