package com.futurebackport.block;

import com.futurebackport.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CopperTorchBlock extends TorchBlock {
   public CopperTorchBlock(Properties properties) {
      super(properties, ParticleTypes.FLAME);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.7;
      double z = pos.getZ() + 0.5;
      level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
      level.addParticle((ParticleOptions)ModParticles.COPPER_FIRE_FLAME.get(), x, y, z, 0.0, 0.0, 0.0);
   }

   public static class Wall extends WallTorchBlock {
      public Wall(Properties properties) {
         super(properties, ParticleTypes.FLAME);
      }

      public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
         Direction opposite = ((Direction)state.getValue(FACING)).getOpposite();
         double x = pos.getX() + 0.5 + 0.27 * opposite.getStepX();
         double y = pos.getY() + 0.7 + 0.22;
         double z = pos.getZ() + 0.5 + 0.27 * opposite.getStepZ();
         level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
         level.addParticle((ParticleOptions)ModParticles.COPPER_FIRE_FLAME.get(), x, y, z, 0.0, 0.0, 0.0);
      }
   }
}
