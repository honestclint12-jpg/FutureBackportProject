package com.futurebackport.entity.nautilus;

import com.futurebackport.registry.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

public final class BreathOfTheNautilus {
   private BreathOfTheNautilus() {
   }

   @SubscribeEvent
   public static void onBreathe(LivingBreatheEvent event) {
      LivingEntity entity = event.getEntity();
      if (entity.hasEffect(ModEffects.BREATH_OF_THE_NAUTILUS.holder())) {
         event.setCanBreathe(true);
         if (!entity.hasEffect(MobEffects.WATER_BREATHING) && !entity.hasEffect(MobEffects.CONDUIT_POWER)) {
            event.setRefillAirAmount(0);
         }
      }
   }
}
