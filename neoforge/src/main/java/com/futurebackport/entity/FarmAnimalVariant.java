package com.futurebackport.entity;

import com.futurebackport.FutureBackport;
import com.futurebackport.network.FarmAnimalVariantPayload;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public enum FarmAnimalVariant implements StringRepresentable {
   TEMPERATE("temperate"),
   WARM("warm"),
   COLD("cold");

   public static final Codec<FarmAnimalVariant> CODEC = StringRepresentable.fromEnum(FarmAnimalVariant::values);
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "futurebackport");
   public static final Supplier<AttachmentType<FarmAnimalVariant>> ATTACHMENT = ATTACHMENTS.register(
      "farm_animal_variant", () -> AttachmentType.builder(() -> TEMPERATE).serialize(CODEC).copyOnDeath().build()
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
      return (FarmAnimalVariant)entity.getData(ATTACHMENT);
   }

   public static void set(Entity entity, FarmAnimalVariant variant) {
      entity.setData(ATTACHMENT, variant);
      if (!entity.level().isClientSide()) {
         PacketDistributor.sendToPlayersTrackingEntity(entity, new FarmAnimalVariantPayload(entity.getId(), variant), new CustomPacketPayload[0]);
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
