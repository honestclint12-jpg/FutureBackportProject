package com.futurebackport.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;

/** Backport of 1.20.5's ColorParticleOption: a particle carrying one ARGB color. */
public class ColorParticleOption implements ParticleOptions {
   private final ParticleType<ColorParticleOption> type;
   private final int color;

   private ColorParticleOption(ParticleType<ColorParticleOption> type, int color) {
      this.type = type;
      this.color = color;
   }

   public static ColorParticleOption create(ParticleType<ColorParticleOption> type, int color) {
      return new ColorParticleOption(type, color);
   }

   /** A particle type for this option, with the command, network and codec plumbing 1.20.1 needs. */
   public static ParticleType<ColorParticleOption> createType(boolean overrideLimiter) {
      Deserializer<ColorParticleOption> deserializer = new Deserializer<>() {
         @Override
         public ColorParticleOption fromCommand(ParticleType<ColorParticleOption> type, StringReader reader) {
            return new ColorParticleOption(type, -1);
         }

         @Override
         public ColorParticleOption fromNetwork(ParticleType<ColorParticleOption> type, FriendlyByteBuf buf) {
            return new ColorParticleOption(type, buf.readInt());
         }
      };
      return new ParticleType<>(overrideLimiter, deserializer) {
         private final Codec<ColorParticleOption> codec = Codec.INT.xmap(color -> new ColorParticleOption(this, color), option -> option.color);

         @Override
         public Codec<ColorParticleOption> codec() {
            return this.codec;
         }
      };
   }

   @Override
   public ParticleType<ColorParticleOption> getType() {
      return this.type;
   }

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeInt(this.color);
   }

   @Override
   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this.type) + " " + this.color;
   }

   public float getRed() {
      return FastColor.ARGB32.red(this.color) / 255.0F;
   }

   public float getGreen() {
      return FastColor.ARGB32.green(this.color) / 255.0F;
   }

   public float getBlue() {
      return FastColor.ARGB32.blue(this.color) / 255.0F;
   }

   public float getAlpha() {
      return FastColor.ARGB32.alpha(this.color) / 255.0F;
   }
}
