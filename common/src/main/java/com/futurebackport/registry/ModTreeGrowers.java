package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModTreeGrowers {
   public static final ResourceKey<ConfiguredFeature<?, ?>> PALE_OAK_BONEMEAL = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, FutureBackport.id("pale_oak_bonemeal")
   );
   public static final TreeGrower PALE_OAK = new TreeGrower("futurebackport:pale_oak", Optional.of(PALE_OAK_BONEMEAL), Optional.empty(), Optional.empty());
}
