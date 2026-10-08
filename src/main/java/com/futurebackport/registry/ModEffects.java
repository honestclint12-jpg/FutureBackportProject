package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {
   public static final RegistrationProvider<MobEffect> EFFECTS = RegistrationProvider.create(Registries.MOB_EFFECT, "futurebackport");
   public static final RegistryEntry<MobEffect, MobEffect> BREATH_OF_THE_NAUTILUS = EFFECTS.register(
      "breath_of_the_nautilus", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 65518) {}
   );
}
