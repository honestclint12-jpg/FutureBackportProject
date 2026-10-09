package com.futurebackport.mixin;

import com.futurebackport.item.SpearEnchanting;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Enchanting tables offer spears their weapon enchantments and Lunge, and keep Lunge off everything else. */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

   @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
   private static void futurebackport$spearEnchantments(
      int power, ItemStack stack, boolean allowTreasure, CallbackInfoReturnable<List<EnchantmentInstance>> cir
   ) {
      SpearEnchanting.adjustTableResults(cir.getReturnValue(), power, stack, allowTreasure);
   }
}
