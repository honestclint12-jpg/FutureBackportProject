package com.futurebackport.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({LightningRodBlock.class})
public abstract class LightningRodBlockMixin extends RodBlock {
   private static final WeatheringCopper FUTUREBACKPORT$UNAFFECTED = () -> WeatherState.UNAFFECTED;

   protected LightningRodBlockMixin(Properties properties) {
      super(properties);
   }

   protected boolean isRandomlyTicking(BlockState state) {
      return state.is(Blocks.LIGHTNING_ROD);
   }

   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (state.is(Blocks.LIGHTNING_ROD)) {
         FUTUREBACKPORT$UNAFFECTED.changeOverTime(state, level, pos, random);
      }
   }
}
