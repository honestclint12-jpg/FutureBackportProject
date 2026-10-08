package com.futurebackport.client;

import com.futurebackport.item.SpearItem;
import com.futurebackport.network.SpearJabPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult.Type;
import com.futurebackport.platform.Services;
import net.minecraft.world.InteractionHand;

/** Turns the attack key into a spear jab while holding a spear. */
public final class SpearInput {
   private SpearInput() {
   }

   /**
    * The attack key was pressed. Returns {@code true} if the spear handled it; the caller must then cancel the vanilla
    * attack and its hand swing.
    */
   public static boolean onAttackKey(InteractionHand hand) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && player.getMainHandItem().getItem() instanceof SpearItem) {
         if (mc.hitResult == null || mc.hitResult.getType() != Type.BLOCK) {
            if (!player.isSpectator() && !player.isUsingItem() && !(player.getAttackStrengthScale(0.5F) < 1.0F)) {
               Services.NETWORK.sendToServer(new SpearJabPayload());
               player.resetAttackStrengthTicker();
               player.swing(hand);
            }

            return true;
         }
      }

      return false;
   }
}
