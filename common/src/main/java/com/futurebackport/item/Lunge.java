package com.futurebackport.item;

import com.futurebackport.util.Backports;
import com.futurebackport.platform.registry.RegistryEntry;

import com.futurebackport.registry.ModEnchantments;
import com.futurebackport.registry.ModSounds;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public final class Lunge {
   private Lunge() {
   }

   public static void afterJab(ServerLevel level, Player player) {
      ItemStack weapon = player.getMainHandItem();
      {
         int lunge = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LUNGE.get(), weapon);
         if (lunge > 0) {
            if (!player.isPassenger() && !player.isFallFlying() && !player.isInWater()) {
               if (player.isCreative() || player.getFoodData().getFoodLevel() >= 7) {
                  Backports.hurtAndBreak(weapon, 1, player, EquipmentSlot.MAINHAND);
                  player.causeFoodExhaustion(4.0F * lunge);
                  Vec3 look = player.getLookAngle();
                  player.push(look.x * 0.458 * lunge, 0.0, look.z * 0.458 * lunge);
                  if (player instanceof ServerPlayer serverPlayer) {
                     serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(player));
                  }
                  RegistryEntry<SoundEvent, SoundEvent> sound = switch (lunge) {
                     case 1 -> ModSounds.SPEAR_LUNGE_1;
                     case 2 -> ModSounds.SPEAR_LUNGE_2;
                     default -> ModSounds.SPEAR_LUNGE_3;
                  };
                  level.playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)sound.get(), player.getSoundSource(), 1.0F, 1.0F);
               }
            }
         }
      }
   }
}
