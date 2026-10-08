package com.futurebackport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WeatheringCopperLanternBlock extends LanternBlock implements WeatheringCopper {
   private final WeatherState weatherState;

   public WeatheringCopperLanternBlock(WeatherState weatherState, Properties properties) {
      super(properties);
      this.weatherState = weatherState;
   }

   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.changeOverTime(state, level, pos, random);
   }

   protected boolean isRandomlyTicking(BlockState state) {
      return WeatheringCopper.getNext(state.getBlock()).isPresent();
   }

   public WeatherState getAge() {
      return this.weatherState;
   }
}
