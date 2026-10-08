package com.futurebackport.entity;

import com.futurebackport.platform.Services;
import com.futurebackport.platform.attachment.DataAttachment;

import com.futurebackport.FutureBackport;
import com.futurebackport.network.FarmAnimalVariantPayload;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

public enum FarmAnimalVariant implements StringRepresentable {
   TEMPERATE("temperate"),
   WARM("warm"),
   COLD("cold");

   public static final Codec<FarmAnimalVariant> CODEC = StringRepresentable.fromEnum(FarmAnimalVariant::values);
   /** Saved with the entity and kept on death. Setting it directly does not sync to clients; use {@link #set}. */
   public static final DataAttachment<FarmAnimalVariant> ATTACHMENT = Services.ATTACHMENTS.register(
      "futurebackport", "farm_animal_variant", () -> TEMPERATE, CODEC, true
   );
   private static final TagKey<Biome> SPAWNS_WARM = TagKey.create(Registries.BIOME, FutureBackport.id("spawns_warm_variant_farm_animals"));
   private static final TagKey<Biome> SPAWNS_COLD = TagKey.create(Registries.BIOME, FutureBackport.id("spawns_cold_variant_farm_animals"));
   private final String name;

   private FarmAnimalVariant(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public static boolean hasVariants(Entity entity) {
      return entity instanceof Cow && !(entity instanceof MushroomCow) || entity instanceof Pig || entity instanceof Chicken;
   }

   public static FarmAnimalVariant get(Entity entity) {
      return ATTACHMENT.get(entity);
   }

   public static void set(Entity entity, FarmAnimalVariant variant) {
      ATTACHMENT.set(entity, variant);
      if (!entity.level().isClientSide()) {
         Services.NETWORK.sendToPlayersTrackingEntity(entity, new FarmAnimalVariantPayload(entity.getId(), variant));
      }
   }

   public static FarmAnimalVariant forBiome(LevelAccessor level, BlockPos pos) {
      Holder<Biome> biome = level.getBiome(pos);
      if (biome.is(SPAWNS_COLD)) {
         return COLD;
      } else {
         return biome.is(SPAWNS_WARM) ? WARM : TEMPERATE;
      }
   }
}
