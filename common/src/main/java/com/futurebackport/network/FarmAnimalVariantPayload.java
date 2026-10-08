package com.futurebackport.network;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.FarmAnimalVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record FarmAnimalVariantPayload(int entityId, FarmAnimalVariant variant) implements CustomPacketPayload {
   public static final Type<FarmAnimalVariantPayload> TYPE = new Type(FutureBackport.id("farm_animal_variant"));
   public static final StreamCodec<FriendlyByteBuf, FarmAnimalVariantPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      FarmAnimalVariantPayload::entityId,
      ByteBufCodecs.VAR_INT.map(i -> FarmAnimalVariant.values()[i], Enum::ordinal),
      FarmAnimalVariantPayload::variant,
      FarmAnimalVariantPayload::new
   );

   public Type<FarmAnimalVariantPayload> type() {
      return TYPE;
   }

   public static void handle(FarmAnimalVariantPayload payload, Player player) {
      Entity entity = player.level().getEntity(payload.entityId());
      if (entity != null) {
         FarmAnimalVariant.ATTACHMENT.set(entity, payload.variant());
      }
   }
}
