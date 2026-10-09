package com.futurebackport.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ParticleLeavesBlock extends LeavesBlock {
   private final float leafParticleChance;
   private final Supplier<? extends ParticleOptions> leafParticle;

   public ParticleLeavesBlock(float leafParticleChance, Supplier<? extends ParticleOptions> leafParticle, Properties properties) {
      super(properties);
      this.leafParticleChance = leafParticleChance;
      this.leafParticle = leafParticle;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      if (random.nextFloat() < this.leafParticleChance) {
         BlockPos below = pos.below();
         if (!isFaceFull(level.getBlockState(below).getCollisionShape(level, below), Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, pos, random, this.leafParticle.get());
         }
      }
   }
}
