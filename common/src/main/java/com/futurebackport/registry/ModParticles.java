package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

import com.futurebackport.particle.TrailParticleOption;
import com.futurebackport.particle.ColorParticleOption;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public class ModParticles {
   public static final RegistrationProvider<ParticleType<?>> PARTICLES = RegistrationProvider.create(Registries.PARTICLE_TYPE, "futurebackport");
   public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PALE_OAK_LEAVES = PARTICLES.register(
      "pale_oak_leaves", () -> new SimpleParticleType(false)
   );
   public static final RegistryEntry<ParticleType<?>, ParticleType<ColorParticleOption>> TINTED_LEAVES = PARTICLES.register(
      "tinted_leaves", () -> ColorParticleOption.createType(false)
   );
   public static final RegistryEntry<ParticleType<?>, SimpleParticleType> COPPER_FIRE_FLAME = PARTICLES.register(
      "copper_fire_flame", () -> new SimpleParticleType(false)
   );
   public static final RegistryEntry<ParticleType<?>, SimpleParticleType> FIREFLY = PARTICLES.register("firefly", () -> new SimpleParticleType(false));
   public static final RegistryEntry<ParticleType<?>, SimpleParticleType> PAUSE_MOB_GROWTH = PARTICLES.register(
      "pause_mob_growth", () -> new SimpleParticleType(false)
   );
   public static final RegistryEntry<ParticleType<?>, SimpleParticleType> RESET_MOB_GROWTH = PARTICLES.register(
      "reset_mob_growth", () -> new SimpleParticleType(false)
   );
   public static final RegistryEntry<ParticleType<?>, ParticleType<TrailParticleOption>> TRAIL = PARTICLES.register(
      "trail", () -> new ParticleType<TrailParticleOption>(false, TrailParticleOption.DESERIALIZER) {
         public Codec<TrailParticleOption> codec() {
            return TrailParticleOption.CODEC;
         }
      }
   );
}
