package com.futurebackport.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;

public class FallenTreeFeature extends Feature<FallenTreeFeature.Config> {
   public FallenTreeFeature() {
      super(FallenTreeFeature.Config.CODEC);
   }

   public boolean place(FeaturePlaceContext<FallenTreeFeature.Config> context) {
      FallenTreeFeature.Config config = (FallenTreeFeature.Config)context.config();
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos origin = context.origin();
      BlockPos stump = this.placeLog(config, level, random, origin.mutable(), Function.identity());
      decorate(level, random, Set.of(stump), config.stumpDecorators());
      Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
      int logLength = config.logLength().sample(random) - 2;
      MutableBlockPos start = origin.relative(direction, 2 + random.nextInt(2)).mutable();
      start.move(Direction.UP, 1);

      for (int i = 0; i < 6 && !this.mayPlaceOn(level, start); i++) {
         start.move(Direction.DOWN);
      }

      if (this.canPlaceEntireLog(level, logLength, start, direction)) {
         Set<BlockPos> log = new HashSet<>();

         for (int i = 0; i < logLength; i++) {
            log.add(
               this.placeLog(
                  config,
                  level,
                  random,
                  start,
                  state -> state.hasProperty(RotatedPillarBlock.AXIS) ? (BlockState)state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()) : state
               )
            );
            start.move(direction);
         }

         decorate(level, random, log, config.logDecorators());
      }

      return true;
   }

   private boolean canPlaceEntireLog(WorldGenLevel level, int logLength, MutableBlockPos start, Direction direction) {
      int gap = 0;

      for (int i = 0; i < logLength; i++) {
         if (!TreeFeature.validTreePos(level, start)) {
            return false;
         }

         if (!this.isOverSolidGround(level, start)) {
            if (++gap > 2) {
               return false;
            }
         } else {
            gap = 0;
         }

         start.move(direction);
      }

      start.move(direction.getOpposite(), logLength);
      return true;
   }

   private boolean mayPlaceOn(LevelAccessor level, BlockPos pos) {
      return TreeFeature.validTreePos(level, pos) && this.isOverSolidGround(level, pos);
   }

   private boolean isOverSolidGround(LevelAccessor level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
   }

   private BlockPos placeLog(
      FallenTreeFeature.Config config, WorldGenLevel level, RandomSource random, MutableBlockPos pos, Function<BlockState, BlockState> modifier
   ) {
      level.setBlock(pos, modifier.apply(config.trunkProvider().getState(random, pos)), 3);
      this.markAboveForPostProcessing(level, pos);
      return pos.immutable();
   }

   private static void decorate(WorldGenLevel level, RandomSource random, Set<BlockPos> logs, List<TreeDecorator> decorators) {
      if (!decorators.isEmpty()) {
         Context context = new Context(level, (pos, state) -> level.setBlock(pos, state, 19), random, logs, Set.of(), Set.of());
         decorators.forEach(decorator -> decorator.place(context));
      }
   }

   public record Config(BlockStateProvider trunkProvider, IntProvider logLength, List<TreeDecorator> stumpDecorators, List<TreeDecorator> logDecorators)
      implements FeatureConfiguration {
      public static final Codec<FallenTreeFeature.Config> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(FallenTreeFeature.Config::trunkProvider),
               IntProvider.codec(0, 16).fieldOf("log_length").forGetter(FallenTreeFeature.Config::logLength),
               TreeDecorator.CODEC.listOf().fieldOf("stump_decorators").forGetter(FallenTreeFeature.Config::stumpDecorators),
               TreeDecorator.CODEC.listOf().fieldOf("log_decorators").forGetter(FallenTreeFeature.Config::logDecorators)
            )
            .apply(i, FallenTreeFeature.Config::new)
      );
   }
}
