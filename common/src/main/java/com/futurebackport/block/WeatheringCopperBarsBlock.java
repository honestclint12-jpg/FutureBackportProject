package com.futurebackport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WeatheringCopperBarsBlock extends IronBarsBlock implements ModWeatheringCopper {
   private final WeatherState weatherState;

   public WeatheringCopperBarsBlock(WeatherState weatherState, Properties properties) {
      super(properties);
      this.weatherState = weatherState;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.onRandomTick(state, level, pos, random);
   }

   public boolean isRandomlyTicking(BlockState state) {
      return ModWeatheringCopper.canOxidize(state);
   }

   public WeatherState getAge() {
      return this.weatherState;
   }
}
