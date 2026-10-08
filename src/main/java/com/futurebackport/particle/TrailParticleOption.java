package com.futurebackport.particle;

import com.futurebackport.registry.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record TrailParticleOption(Vec3 target, int color, int duration) implements ParticleOptions {
   public static final MapCodec<TrailParticleOption> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(TrailParticleOption::target),
            Codec.INT.fieldOf("color").forGetter(TrailParticleOption::color),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TrailParticleOption::duration)
         )
         .apply(i, TrailParticleOption::new)
   );
   private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.DOUBLE, Vec3::x, ByteBufCodecs.DOUBLE, Vec3::y, ByteBufCodecs.DOUBLE, Vec3::z, Vec3::new
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TrailParticleOption> STREAM_CODEC = StreamCodec.composite(
      VEC3_STREAM_CODEC,
      TrailParticleOption::target,
      ByteBufCodecs.INT,
      TrailParticleOption::color,
      ByteBufCodecs.VAR_INT,
      TrailParticleOption::duration,
      TrailParticleOption::new
   );

   public ParticleType<TrailParticleOption> getType() {
      return (ParticleType<TrailParticleOption>)ModParticles.TRAIL.get();
   }
}
