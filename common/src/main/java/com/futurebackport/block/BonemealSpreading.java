package com.futurebackport.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public final class BonemealSpreading {
   private BonemealSpreading() {
   }

   public static boolean hasSpreadableNeighbourPos(LevelReader level, BlockPos pos, BlockState toPlace) {
      return find(Plane.HORIZONTAL.stream().toList(), level, pos, toPlace).isPresent();
   }

   public static Optional<BlockPos> findSpreadableNeighbourPos(Level level, BlockPos pos, BlockState toPlace) {
      return find(Plane.HORIZONTAL.shuffledCopy(level.getRandom()), level, pos, toPlace);
   }

   private static Optional<BlockPos> find(List<Direction> directions, LevelReader level, BlockPos pos, BlockState toPlace) {
      for (Direction direction : directions) {
         BlockPos neighbour = pos.relative(direction);
         if (level.isEmptyBlock(neighbour) && toPlace.canSurvive(level, neighbour)) {
            return Optional.of(neighbour);
         }
      }

      return Optional.empty();
   }
}
