package com.futurebackport.entity;

import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

public final class CopperGolemBuilding {
   private static final Map<Block, WeatherState> COPPER = Map.of(
      Blocks.COPPER_BLOCK,
      WeatherState.UNAFFECTED,
      Blocks.EXPOSED_COPPER,
      WeatherState.EXPOSED,
      Blocks.WEATHERED_COPPER,
      WeatherState.WEATHERED,
      Blocks.OXIDIZED_COPPER,
      WeatherState.OXIDIZED
   );
   private static final Map<Block, WeatherState> WAXED_COPPER = Map.of(
      Blocks.WAXED_COPPER_BLOCK,
      WeatherState.UNAFFECTED,
      Blocks.WAXED_EXPOSED_COPPER,
      WeatherState.EXPOSED,
      Blocks.WAXED_WEATHERED_COPPER,
      WeatherState.WEATHERED,
      Blocks.WAXED_OXIDIZED_COPPER,
      WeatherState.OXIDIZED
   );

   private CopperGolemBuilding() {
   }

   @Nullable
   public static WeatherState weatherStateOf(BlockState state) {
      WeatherState weather = COPPER.get(state.getBlock());
      return weather != null ? weather : WAXED_COPPER.get(state.getBlock());
   }

   public static void trySpawn(Level level, BlockPos pumpkinPos) {
      BlockState pumpkin = level.getBlockState(pumpkinPos);
      if (pumpkin.is(Blocks.CARVED_PUMPKIN) || pumpkin.is(Blocks.JACK_O_LANTERN)) {
         BlockPos copperPos = pumpkinPos.below();
         BlockState copper = level.getBlockState(copperPos);
         WeatherState weather = weatherStateOf(copper);
         if (weather != null) {
            CopperGolem golem = (CopperGolem)((EntityType)ModEntities.COPPER_GOLEM.get()).create(level);
            if (golem != null) {
               level.setBlock(pumpkinPos, Blocks.AIR.defaultBlockState(), 2);
               level.levelEvent(2001, pumpkinPos, Block.getId(pumpkin));
               golem.moveTo(pumpkinPos.getX() + 0.5, pumpkinPos.getY() + 0.05, pumpkinPos.getZ() + 0.5, 0.0F, 0.0F);
               level.addFreshEntity(golem);

               for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, golem.getBoundingBox().inflate(5.0))) {
                  CriteriaTriggers.SUMMONED_ENTITY.trigger(player, golem);
               }

               level.blockUpdated(pumpkinPos, Blocks.AIR);
               Map<WeatherState, DeferredBlock<Block>> chests = WAXED_COPPER.containsKey(copper.getBlock())
                  ? ModBlocks.COPPER_CHEST.waxed()
                  : ModBlocks.COPPER_CHEST.weathering();
               BlockState chest = (BlockState)((Block)chests.get(weather).get())
                  .defaultBlockState()
                  .setValue(ChestBlock.FACING, (Direction)pumpkin.getValue(CarvedPumpkinBlock.FACING));
               level.setBlock(copperPos, chest, 2);
               golem.spawn(weather);
            }
         }
      }
   }
}
