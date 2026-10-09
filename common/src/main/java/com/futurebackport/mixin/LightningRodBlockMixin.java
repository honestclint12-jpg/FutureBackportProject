package com.futurebackport.mixin;

import com.futurebackport.block.ModWeatheringCopper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({LightningRodBlock.class})
public abstract class LightningRodBlockMixin extends RodBlock {
   /** Vanilla's rod weathers like the mod's copper blocks (1.20.1's own oxidation map doesn't include it). */
   private static final ModWeatheringCopper FUTUREBACKPORT$UNAFFECTED = () -> WeatherState.UNAFFECTED;

   protected LightningRodBlockMixin(Properties properties) {
      super(properties);
   }

   public boolean isRandomlyTicking(BlockState state) {
      return state.is(Blocks.LIGHTNING_ROD);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (state.is(Blocks.LIGHTNING_ROD)) {
         FUTUREBACKPORT$UNAFFECTED.onRandomTick(state, level, pos, random);
      }
   }
}
