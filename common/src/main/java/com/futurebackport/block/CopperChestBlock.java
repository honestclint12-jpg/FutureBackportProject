package com.futurebackport.block;

import com.futurebackport.block.entity.CopperChestBlockEntity;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestBlock extends ChestBlock {
   private final WeatherState weatherState;

   public CopperChestBlock(WeatherState weatherState, Properties properties) {
      super(properties, ModBlockEntities.COPPER_CHEST::get);
      this.weatherState = weatherState;
   }

   public WeatherState getWeatherState() {
      return this.weatherState;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CopperChestBlockEntity(pos, state);
   }

   public static SoundEvent hingeSound(WeatherState state, boolean open) {
      return switch (state) {
         case WEATHERED -> open ? (SoundEvent)ModSounds.COPPER_CHEST_WEATHERED_OPEN.get() : (SoundEvent)ModSounds.COPPER_CHEST_WEATHERED_CLOSE.get();
         case OXIDIZED -> open ? (SoundEvent)ModSounds.COPPER_CHEST_OXIDIZED_OPEN.get() : (SoundEvent)ModSounds.COPPER_CHEST_OXIDIZED_CLOSE.get();
         default -> open ? (SoundEvent)ModSounds.COPPER_CHEST_OPEN.get() : (SoundEvent)ModSounds.COPPER_CHEST_CLOSE.get();
      };
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return neighborState.getBlock() instanceof CopperChestBlock
            && neighborState.getBlock() != this
            && state.getValue(TYPE) != ChestType.SINGLE
            && getConnectedDirection(state) == direction
            && neighborState.hasProperty(TYPE)
            && neighborState.getValue(TYPE) != ChestType.SINGLE
         ? neighborState.getBlock().withPropertiesOf(state)
         : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public static class Weathering extends CopperChestBlock implements ModWeatheringCopper {
      public Weathering(WeatherState weatherState, Properties properties) {
         super(weatherState, properties);
      }

      public boolean isRandomlyTicking(BlockState state) {
         return ModWeatheringCopper.canOxidize(state);
      }

      public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
         if (state.getValue(TYPE) != ChestType.RIGHT
            && level.getBlockEntity(pos) instanceof ChestBlockEntity chest
            && ChestBlockEntity.getOpenCount(level, pos) == 0) {
            this.onRandomTick(state, level, pos, random);
         }
      }

      public WeatherState getAge() {
         return this.getWeatherState();
      }
   }
}
