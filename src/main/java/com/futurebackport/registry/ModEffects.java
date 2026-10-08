package com.futurebackport.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "futurebackport");
   public static final DeferredHolder<MobEffect, MobEffect> BREATH_OF_THE_NAUTILUS = EFFECTS.register(
      "breath_of_the_nautilus", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 65518) {}
   );
}
