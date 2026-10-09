package com.futurebackport.forge.worldgen;

import com.futurebackport.forge.ForgeBiomeModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

/** Keeps an entity's existing spawn entries in the selected biomes but changes their weight. */
public record SetSpawnWeightModifier(HolderSet<Biome> biomes, EntityType<?> entityType, int weight) implements BiomeModifier {
   public static final Codec<SetSpawnWeightModifier> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(SetSpawnWeightModifier::biomes),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(SetSpawnWeightModifier::entityType),
            Codec.INT.fieldOf("weight").forGetter(SetSpawnWeightModifier::weight)
         )
         .apply(i, SetSpawnWeightModifier::new)
   );

   @Override
   public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
      if (phase == Phase.MODIFY && this.biomes.contains(biome)) {
         MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
         MobCategory category = this.entityType.getCategory();
         List<SpawnerData> list = spawns.getSpawner(category);

         for (SpawnerData data : new ArrayList<>(list)) {
            if (data.type == this.entityType) {
               list.remove(data);
               list.add(new SpawnerData(data.type, this.weight, data.minCount, data.maxCount));
            }
         }
      }
   }

   @Override
   public Codec<? extends BiomeModifier> codec() {
      return ForgeBiomeModifiers.SET_SPAWN_WEIGHT.get();
   }
}
