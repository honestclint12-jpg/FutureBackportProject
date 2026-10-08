package com.futurebackport.worldgen;

import com.futurebackport.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier.Phase;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record SetSpawnWeightModifier(HolderSet<Biome> biomes, EntityType<?> entityType, int weight) implements BiomeModifier {
   public static final MapCodec<SetSpawnWeightModifier> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(SetSpawnWeightModifier::biomes),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(SetSpawnWeightModifier::entityType),
            Codec.INT.fieldOf("weight").forGetter(SetSpawnWeightModifier::weight)
         )
         .apply(i, SetSpawnWeightModifier::new)
   );

   public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
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

   public MapCodec<? extends BiomeModifier> codec() {
      return (MapCodec<? extends BiomeModifier>)ModWorldgen.SET_SPAWN_WEIGHT.get();
   }
}
