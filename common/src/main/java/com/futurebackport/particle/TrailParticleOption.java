package com.futurebackport.particle;

import com.futurebackport.registry.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record TrailParticleOption(Vec3 target, int color, int duration) implements ParticleOptions {
   public static final Codec<TrailParticleOption> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Vec3.CODEC.fieldOf("target").forGetter(TrailParticleOption::target),
            Codec.INT.fieldOf("color").forGetter(TrailParticleOption::color),
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TrailParticleOption::duration)
         )
         .apply(i, TrailParticleOption::new)
   );
   public static final Deserializer<TrailParticleOption> DESERIALIZER = new Deserializer<>() {
      @Override
      public TrailParticleOption fromCommand(ParticleType<TrailParticleOption> type, StringReader reader) {
         return new TrailParticleOption(Vec3.ZERO, -1, 20);
      }

      @Override
      public TrailParticleOption fromNetwork(ParticleType<TrailParticleOption> type, FriendlyByteBuf buf) {
         return new TrailParticleOption(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt(), buf.readVarInt());
      }
   };

   public ParticleType<TrailParticleOption> getType() {
      return ModParticles.TRAIL.get();
   }

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeDouble(this.target.x);
      buf.writeDouble(this.target.y);
      buf.writeDouble(this.target.z);
      buf.writeInt(this.color);
      buf.writeVarInt(this.duration);
   }

   @Override
   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + this.target + " " + this.color + " " + this.duration;
   }
}
