package com.futurebackport.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class FireflyParticle extends TextureSheetParticle {
   private FireflyParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za) {
      super(level, x, y, z, xa, ya, za);
      this.speedUpWhenYMotionIsBlocked = true;
      this.friction = 0.96F;
      this.quadSize *= 0.75F;
      this.yd *= 0.8F;
      this.xd *= 0.8F;
      this.zd *= 0.8F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getLightColor(float partialTick) {
      return (int)(255.0F * fade(this.lifetimeProgress(this.age + partialTick), 0.1F, 0.3F));
   }

   public void tick() {
      super.tick();
      if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
         this.remove();
      } else {
         this.setAlpha(fade(this.lifetimeProgress(this.age), 0.3F, 0.5F));
         if (this.random.nextFloat() > 0.95F || this.age == 1) {
            this.setParticleSpeed(-0.05F + 0.1F * this.random.nextFloat(), -0.05F + 0.1F * this.random.nextFloat(), -0.05F + 0.1F * this.random.nextFloat());
         }
      }
   }

   private float lifetimeProgress(float age) {
      return Mth.clamp(age / this.lifetime, 0.0F, 1.0F);
   }

   private static float fade(float progress, float fadeIn, float fadeOut) {
      if (progress >= 1.0F - fadeIn) {
         return (1.0F - progress) / fadeIn;
      } else {
         return progress <= fadeOut ? progress / fadeOut : 1.0F;
      }
   }

   public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         RandomSource random = level.getRandom();
         FireflyParticle particle = new FireflyParticle(level, x, y, z, 0.5 - random.nextDouble(), random.nextBoolean() ? yd : -yd, 0.5 - random.nextDouble());
         particle.pickSprite(this.sprites);
         particle.setLifetime(random.nextIntBetweenInclusive(200, 300));
         particle.scale(1.5F);
         particle.setAlpha(0.0F);
         return particle;
      }
   }
}
