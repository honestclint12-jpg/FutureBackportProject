package com.futurebackport.block;

import com.futurebackport.registry.ModDataMaps;
import java.util.Optional;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WeatheringCopper whose next stage comes from {@link ModDataMaps}. Vanilla 1.20.1 only knows its own copper blocks
 * (a fixed map), so the mod's blocks look themselves up here instead.
 */
public interface ModWeatheringCopper extends WeatheringCopper {

   @Override
   default Optional<BlockState> getNext(BlockState state) {
      return ModDataMaps.nextOxidized(state.getBlock()).map(next -> next.withPropertiesOf(state));
   }

   static boolean canOxidize(BlockState state) {
      return ModDataMaps.nextOxidized(state.getBlock()).isPresent();
   }
}
