package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import com.futurebackport.worldgen.AttachedToLogsDecorator;
import com.futurebackport.worldgen.CreakingHeartDecorator;
import com.futurebackport.worldgen.FallenTreeFeature;
import com.futurebackport.worldgen.PaleMossDecorator;
import com.futurebackport.worldgen.PlaceOnGroundDecorator;
import com.futurebackport.worldgen.SetSpawnWeightModifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

public class ModWorldgen {
   public static final RegistrationProvider<Feature<?>> FEATURES = RegistrationProvider.create(Registries.FEATURE, "futurebackport");
   public static final RegistrationProvider<TreeDecoratorType<?>> TREE_DECORATORS = RegistrationProvider.create(Registries.TREE_DECORATOR_TYPE, "futurebackport");
   public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(
      Keys.BIOME_MODIFIER_SERIALIZERS, "futurebackport"
   );
   public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SetSpawnWeightModifier>> SET_SPAWN_WEIGHT = BIOME_MODIFIERS.register(
      "set_spawn_weight", () -> SetSpawnWeightModifier.CODEC
   );
   public static final RegistryEntry<Feature<?>, FallenTreeFeature> FALLEN_TREE = FEATURES.register("fallen_tree", FallenTreeFeature::new);
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<AttachedToLogsDecorator>> ATTACHED_TO_LOGS = TREE_DECORATORS.register(
      "attached_to_logs", () -> new TreeDecoratorType(AttachedToLogsDecorator.CODEC)
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<PlaceOnGroundDecorator>> PLACE_ON_GROUND = TREE_DECORATORS.register(
      "place_on_ground", () -> new TreeDecoratorType(PlaceOnGroundDecorator.CODEC)
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<PaleMossDecorator>> PALE_MOSS = TREE_DECORATORS.register(
      "pale_moss", () -> new TreeDecoratorType(PaleMossDecorator.CODEC)
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<CreakingHeartDecorator>> CREAKING_HEART = TREE_DECORATORS.register(
      "creaking_heart", () -> new TreeDecoratorType(CreakingHeartDecorator.CODEC)
   );
}
