package com.futurebackport.worldgen;

import com.futurebackport.block.CreakingHeartBlock;
import com.futurebackport.block.CreakingHeartState;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;

public class CreakingHeartDecorator extends TreeDecorator {
   public static final MapCodec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
      .fieldOf("probability")
      .xmap(CreakingHeartDecorator::new, d -> d.probability);
   private final float probability;

   public CreakingHeartDecorator(float probability) {
      this.probability = probability;
   }

   protected TreeDecoratorType<?> type() {
      return (TreeDecoratorType<?>)ModWorldgen.CREAKING_HEART.get();
   }

   public void place(Context context) {
      RandomSource random = context.random();
      List<BlockPos> logs = context.logs();
      if (!logs.isEmpty() && !(random.nextFloat() >= this.probability)) {
         List<BlockPos> candidates = new ArrayList<>(logs);
         Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
         candidates.stream()
            .filter(pos -> {
               for (Direction direction : Direction.values()) {
                  if (!context.level().isStateAtPosition(pos.relative(direction), state -> state.is(BlockTags.LOGS))) {
                     return false;
                  }
               }

               return true;
            })
            .findFirst()
            .ifPresent(
               pos -> context.setBlock(
                  pos,
                  (BlockState)((BlockState)((CreakingHeartBlock)ModBlocks.CREAKING_HEART.get())
                        .defaultBlockState()
                        .setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT))
                     .setValue(CreakingHeartBlock.NATURAL, true)
               )
            );
      }
   }
}
