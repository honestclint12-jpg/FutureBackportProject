package com.futurebackport.block;

import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class FireflyBushBlock extends SpreadingBushBlock {

   public FireflyBushBlock(Properties properties) {
      super(properties);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(30) == 0 && isMoonVisible(level) && level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) <= pos.getY()) {
         level.playLocalSound(pos, (SoundEvent)ModSounds.FIREFLY_BUSH_IDLE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
      }

      if (level.getMaxLocalRawBrightness(pos) <= 13 && random.nextDouble() <= 0.7) {
         double x = pos.getX() + random.nextDouble() * 10.0 - 5.0;
         double y = pos.getY() + random.nextDouble() * 5.0;
         double z = pos.getZ() + random.nextDouble() * 10.0 - 5.0;
         level.addParticle((ParticleOptions)ModParticles.FIREFLY.get(), x, y, z, 0.0, 0.0, 0.0);
      }
   }

   static boolean isMoonVisible(Level level) {
      if (level.dimensionType().hasFixedTime()) {
         return false;
      } else {
         float time = level.getTimeOfDay(1.0F);
         return time > 0.25F && time < 0.75F;
      }
   }
}
