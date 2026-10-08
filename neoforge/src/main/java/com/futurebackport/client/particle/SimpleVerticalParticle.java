package com.futurebackport.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SimpleVerticalParticle extends TextureSheetParticle {
   private SimpleVerticalParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, boolean upwards) {
      super(level, x, y, z, xd, yd, zd);
      this.xd = xd;
      this.yd = yd + (upwards ? 0.03 : -0.03);
      this.zd = zd;
      this.gravity = 0.0F;
      this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.5F);
      this.lifetime = 8;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public record PauseProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         SimpleVerticalParticle particle = new SimpleVerticalParticle(level, x, y, z, xd, yd, zd, false);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }

   public record ResetProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         SimpleVerticalParticle particle = new SimpleVerticalParticle(level, x, y, z, xd, yd, zd, true);
         particle.pickSprite(this.sprites);
         return particle;
      }
   }
}
