package com.futurebackport.network;

import com.futurebackport.FutureBackport;
import com.futurebackport.item.SpearItem;
import com.futurebackport.platform.network.Payload;
import com.futurebackport.platform.network.PayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record SpearJabPayload() implements Payload {
   public static final PayloadType<SpearJabPayload> TYPE = new PayloadType<>(
      FutureBackport.id("spear_jab"), SpearJabPayload.class, buf -> new SpearJabPayload());

   public PayloadType<SpearJabPayload> type() {
      return TYPE;
   }

   public void write(FriendlyByteBuf buf) {
   }

   public static void handle(SpearJabPayload payload, Player player) {
      if (!player.isSpectator() && !player.isUsingItem() && player.getMainHandItem().getItem() instanceof SpearItem spear) {
         if (!(player.getAttackStrengthScale(0.5F) < 1.0F)) {
            spear.jab((ServerLevel)player.level(), player);
         }
      }
   }
}
