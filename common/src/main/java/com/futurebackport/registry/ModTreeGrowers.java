package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class ModTreeGrowers {
   public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_OAK_BONEMEAL = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, FutureBackport.id("pale_oak_bonemeal")
   );
   /** Pale oak only grows as a 2x2 tree; a lone sapling does nothing (1.20.1 has no TreeGrower record). */
   public static final AbstractTreeGrower PALE_OAK = new AbstractMegaTreeGrower() {
      @Nullable
      @Override
      protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
         return null;
      }

      @Nullable
      @Override
      protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
         return PALE_OAK_BONEMEAL;
      }
   };
}
