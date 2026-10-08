package com.futurebackport.network;

import com.futurebackport.FutureBackport;
import com.futurebackport.item.SpearItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpearJabPayload() implements CustomPacketPayload {
   public static final Type<SpearJabPayload> TYPE = new Type(FutureBackport.id("spear_jab"));
   public static final StreamCodec<ByteBuf, SpearJabPayload> STREAM_CODEC = StreamCodec.unit(new SpearJabPayload());

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(SpearJabPayload payload, IPayloadContext context) {
      Player player = context.player();
      if (!player.isSpectator() && !player.isUsingItem() && player.getMainHandItem().getItem() instanceof SpearItem spear) {
         if (!(player.getAttackStrengthScale(0.5F) < 1.0F)) {
            spear.jab((ServerLevel)player.level(), player);
         }
      }
   }
}
