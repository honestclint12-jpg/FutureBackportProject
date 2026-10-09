package com.futurebackport.client.particle;

import com.futurebackport.particle.TrailParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;

public class TrailParticle extends TextureSheetParticle {
   private final Vec3 target;

   private TrailParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, Vec3 target, int color) {
      super(level, x, y, z, xd, yd, zd);
      this.rCol = ARGB32.red(color) / 255.0F * (0.875F + this.random.nextFloat() * 0.25F);
      this.gCol = ARGB32.green(color) / 255.0F * (0.875F + this.random.nextFloat() * 0.25F);
      this.bCol = ARGB32.blue(color) / 255.0F * (0.875F + this.random.nextFloat() * 0.25F);
      this.quadSize = 0.26F;
      this.target = target;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         double alpha = 1.0 / (this.lifetime - this.age);
         this.x = Mth.lerp(alpha, this.x, this.target.x());
         this.y = Mth.lerp(alpha, this.y, this.target.y());
         this.z = Mth.lerp(alpha, this.z, this.target.z());
      }
   }

   public int getLightColor(float partialTick) {
      return 15728880;
   }

   public record Provider(SpriteSet sprites) implements ParticleProvider<TrailParticleOption> {
      public Particle createParticle(TrailParticleOption options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         TrailParticle particle = new TrailParticle(level, x, y, z, xd, yd, zd, options.target(), options.color());
         particle.pickSprite(this.sprites);
         particle.setLifetime(options.duration());
         return particle;
      }
   }
}
