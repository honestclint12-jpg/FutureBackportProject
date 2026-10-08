package com.futurebackport.worldgen;

import com.futurebackport.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;

public class AttachedToLogsDecorator extends TreeDecorator {
   public static final MapCodec<AttachedToLogsDecorator> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability),
            BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(d -> d.blockProvider),
            ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(d -> d.directions)
         )
         .apply(i, AttachedToLogsDecorator::new)
   );
   private final float probability;
   private final BlockStateProvider blockProvider;
   private final List<Direction> directions;

   public AttachedToLogsDecorator(float probability, BlockStateProvider blockProvider, List<Direction> directions) {
      this.probability = probability;
      this.blockProvider = blockProvider;
      this.directions = directions;
   }

   public void place(Context context) {
      RandomSource random = context.random();

      for (BlockPos log : Util.shuffledCopy(context.logs(), random)) {
         BlockPos pos = log.relative((Direction)Util.getRandom(this.directions, random));
         if (random.nextFloat() <= this.probability && context.isAir(pos)) {
            context.setBlock(pos, this.blockProvider.getState(random, pos));
         }
      }
   }

   protected TreeDecoratorType<?> type() {
      return (TreeDecoratorType<?>)ModWorldgen.ATTACHED_TO_LOGS.get();
   }
}
