package com.futurebackport.network;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.platform.network.Payload;
import com.futurebackport.platform.network.PayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record FarmAnimalVariantPayload(int entityId, FarmAnimalVariant variant) implements Payload {
   public static final PayloadType<FarmAnimalVariantPayload> TYPE = new PayloadType<>(
      FutureBackport.id("farm_animal_variant"), FarmAnimalVariantPayload.class, buf -> new FarmAnimalVariantPayload(buf.readVarInt(), FarmAnimalVariant.values()[buf.readVarInt()])
   );

   public PayloadType<FarmAnimalVariantPayload> type() {
      return TYPE;
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeVarInt(this.entityId);
      buf.writeVarInt(this.variant.ordinal());
   }

   public static void handle(FarmAnimalVariantPayload payload, Player player) {
      Entity entity = player.level().getEntity(payload.entityId());
      if (entity != null) {
         FarmAnimalVariant.ATTACHMENT.set(entity, payload.variant());
      }
   }
}
