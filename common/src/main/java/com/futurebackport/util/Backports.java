package com.futurebackport.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Small helpers that newer Minecraft versions have built in. */
public final class Backports {

   private Backports() {
   }

   /** ItemStack.consume: shrinks the stack unless the player is in creative. */
   public static void consume(ItemStack stack, int amount, @Nullable LivingEntity entity) {
      if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
         stack.shrink(amount);
      }
   }

   /** ItemStack.consumeAndReturn: removes {@code amount} items and returns them (a copy in creative). */
   public static ItemStack consumeAndReturn(ItemStack stack, int amount, @Nullable LivingEntity entity) {
      ItemStack split = stack.copyWithCount(amount);
      consume(stack, amount, entity);
      return split;
   }

   /** ItemStack.hurtAndBreak(int, LivingEntity, EquipmentSlot). */
   public static void hurtAndBreak(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
      stack.hurtAndBreak(amount, entity, e -> e.broadcastBreakEvent(slot));
   }

   /** LivingEntity.getSlotForHand. */
   public static EquipmentSlot slotForHand(InteractionHand hand) {
      return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
   }
}
