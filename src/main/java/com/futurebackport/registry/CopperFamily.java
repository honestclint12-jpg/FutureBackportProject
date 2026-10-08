package com.futurebackport.registry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

public record CopperFamily(String baseName, Map<WeatherState, DeferredBlock<Block>> weathering, Map<WeatherState, DeferredBlock<Block>> waxed) {
   public static CopperFamily register(
      String baseName,
      BiFunction<WeatherState, Properties, ? extends Block> weatheringFactory,
      BiFunction<WeatherState, Properties, ? extends Block> waxedFactory,
      Function<WeatherState, Properties> properties
   ) {
      Map<WeatherState, DeferredBlock<Block>> weathering = new EnumMap<>(WeatherState.class);
      Map<WeatherState, DeferredBlock<Block>> waxed = new EnumMap<>(WeatherState.class);
      boolean vanillaBase = baseName.equals("lightning_rod");

      for (WeatherState state : WeatherState.values()) {
         String name = name(state, baseName);
         if (!vanillaBase || state != WeatherState.UNAFFECTED) {
            weathering.put(state, ModBlocks.BLOCKS.register(name, () -> weatheringFactory.apply(state, properties.apply(state))));
         }

         waxed.put(state, ModBlocks.BLOCKS.register("waxed_" + name, () -> waxedFactory.apply(state, properties.apply(state))));
      }

      return new CopperFamily(baseName, weathering, waxed);
   }

   public static String name(WeatherState state, String baseName) {
      return state == WeatherState.UNAFFECTED ? baseName : state.name().toLowerCase(Locale.ROOT) + "_" + baseName;
   }

   public static MapColor copperColor(WeatherState state) {
      return switch (state) {
         case UNAFFECTED -> Blocks.COPPER_BLOCK.defaultMapColor();
         case EXPOSED -> Blocks.EXPOSED_COPPER.defaultMapColor();
         case WEATHERED -> Blocks.WEATHERED_COPPER.defaultMapColor();
         case OXIDIZED -> Blocks.OXIDIZED_COPPER.defaultMapColor();
         default -> throw new MatchException(null, null);
      };
   }

   public List<DeferredBlock<Block>> all() {
      List<DeferredBlock<Block>> list = new ArrayList<>();

      for (WeatherState state : WeatherState.values()) {
         if (this.weathering.containsKey(state)) {
            list.add(this.weathering.get(state));
         }

         list.add(this.waxed.get(state));
      }

      return list;
   }
}
