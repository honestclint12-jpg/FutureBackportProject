package com.futurebackport.entity.nautilus;

import com.futurebackport.registry.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class BreathOfTheNautilus {
   private BreathOfTheNautilus() {
   }

   /** The effect lets its holder breathe underwater... */
   public static boolean canBreathe(LivingEntity entity) {
      return entity.hasEffect(ModEffects.BREATH_OF_THE_NAUTILUS.get());
   }

   /** ...but, unlike water breathing or conduit power, does not refill air while active. */
   public static boolean blocksAirRefill(LivingEntity entity) {
      return canBreathe(entity) && !entity.hasEffect(MobEffects.WATER_BREATHING) && !entity.hasEffect(MobEffects.CONDUIT_POWER);
   }
}
