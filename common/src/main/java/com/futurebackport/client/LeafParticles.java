package com.futurebackport.client;

import com.futurebackport.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.futurebackport.particle.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LeafParticles {
   private static final float CHANCE = 0.01F;
   private static final int AZALEA_COLOR = -9399763;

   private LeafParticles() {
   }

   public static void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      Block block = state.getBlock();
      boolean azalea = block == Blocks.AZALEA_LEAVES || block == Blocks.FLOWERING_AZALEA_LEAVES;
      boolean tinted = block == Blocks.OAK_LEAVES
         || block == Blocks.SPRUCE_LEAVES
         || block == Blocks.BIRCH_LEAVES
         || block == Blocks.JUNGLE_LEAVES
         || block == Blocks.ACACIA_LEAVES
         || block == Blocks.DARK_OAK_LEAVES
         || block == Blocks.MANGROVE_LEAVES;
      if ((azalea || tinted) && !(random.nextFloat() >= 0.01F)) {
         BlockPos below = pos.below();
         if (!Block.isFaceFull(level.getBlockState(below).getCollisionShape(level, below), Direction.UP)) {
            int color = azalea ? -9399763 : Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
            ParticleUtils.spawnParticleBelow(level, pos, random, ColorParticleOption.create((ParticleType)ModParticles.TINTED_LEAVES.get(), color));
         }
      }
   }
}
