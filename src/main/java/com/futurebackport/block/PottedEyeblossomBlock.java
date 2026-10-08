package com.futurebackport.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PottedEyeblossomBlock extends FlowerPotBlock {
   private final boolean open;
   private final Supplier<? extends Block> opposite;

   public PottedEyeblossomBlock(boolean open, Supplier<? extends Block> flower, Supplier<? extends Block> opposite, Properties properties) {
      super(() -> (FlowerPotBlock)Blocks.FLOWER_POT, flower, properties);
      this.open = open;
      this.opposite = opposite;
   }

   protected boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (EyeblossomBlock.shouldBeOpen(level, this.open) != this.open) {
         level.setBlock(pos, this.opposite.get().defaultBlockState(), 3);
         EyeblossomBlock.Type newType = this.open ? EyeblossomBlock.Type.CLOSED : EyeblossomBlock.Type.OPEN;
         newType.spawnTransformParticle(level, pos, random);
         level.playSound(null, pos, newType.longSwitchSound.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }
}
