package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import com.futurebackport.worldgen.AttachedToLogsDecorator;
import com.futurebackport.worldgen.CreakingHeartDecorator;
import com.futurebackport.worldgen.FallenTreeFeature;
import com.futurebackport.worldgen.PaleMossDecorator;
import com.futurebackport.worldgen.PlaceOnGroundDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class ModWorldgen {
   public static final RegistrationProvider<Feature<?>> FEATURES = RegistrationProvider.create(Registries.FEATURE, "futurebackport");
   public static final RegistrationProvider<TreeDecoratorType<?>> TREE_DECORATORS = RegistrationProvider.create(Registries.TREE_DECORATOR_TYPE, "futurebackport");
   public static final RegistryEntry<Feature<?>, FallenTreeFeature> FALLEN_TREE = FEATURES.register("fallen_tree", FallenTreeFeature::new);
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<AttachedToLogsDecorator>> ATTACHED_TO_LOGS = TREE_DECORATORS.register(
      "attached_to_logs", () -> new TreeDecoratorType<>(AttachedToLogsDecorator.CODEC.codec())
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<PlaceOnGroundDecorator>> PLACE_ON_GROUND = TREE_DECORATORS.register(
      "place_on_ground", () -> new TreeDecoratorType<>(PlaceOnGroundDecorator.CODEC.codec())
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<PaleMossDecorator>> PALE_MOSS = TREE_DECORATORS.register(
      "pale_moss", () -> new TreeDecoratorType<>(PaleMossDecorator.CODEC.codec())
   );
   public static final RegistryEntry<TreeDecoratorType<?>, TreeDecoratorType<CreakingHeartDecorator>> CREAKING_HEART = TREE_DECORATORS.register(
      "creaking_heart", () -> new TreeDecoratorType<>(CreakingHeartDecorator.CODEC.codec())
   );
}
