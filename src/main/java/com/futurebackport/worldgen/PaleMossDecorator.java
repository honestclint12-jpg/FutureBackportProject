package com.futurebackport.worldgen;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.HangingMossBlock;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModWorldgen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;

public class PaleMossDecorator extends TreeDecorator {
   public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter(d -> d.leavesProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter(d -> d.trunkProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter(d -> d.groundProbability)
         )
         .apply(i, PaleMossDecorator::new)
   );
   private static final ResourceKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, FutureBackport.id("pale_moss_patch")
   );
   private final float leavesProbability;
   private final float trunkProbability;
   private final float groundProbability;

   public PaleMossDecorator(float leavesProbability, float trunkProbability, float groundProbability) {
      this.leavesProbability = leavesProbability;
      this.trunkProbability = trunkProbability;
      this.groundProbability = groundProbability;
   }

   protected TreeDecoratorType<?> type() {
      return (TreeDecoratorType<?>)ModWorldgen.PALE_MOSS.get();
   }

   public void place(Context context) {
      RandomSource random = context.random();
      List<BlockPos> logs = Util.shuffledCopy(context.logs(), random);
      if (!logs.isEmpty()) {
         BlockPos origin = Collections.min(logs, Comparator.comparingInt(Vec3i::getY));
         if (random.nextFloat() < this.groundProbability && context.level() instanceof WorldGenLevel level) {
            level.registryAccess()
               .registry(Registries.CONFIGURED_FEATURE)
               .flatMap(registry -> registry.getHolder(PALE_MOSS_PATCH))
               .ifPresent(patch -> ((ConfiguredFeature)patch.value()).place(level, level.getLevel().getChunkSource().getGenerator(), random, origin.above()));
         }

         context.logs().forEach(pos -> {
            if (random.nextFloat() < this.trunkProbability && context.isAir(pos.below())) {
               addMossHanger(pos.below(), context);
            }
         });
         context.leaves().forEach(pos -> {
            if (random.nextFloat() < this.leavesProbability && context.isAir(pos.below())) {
               addMossHanger(pos.below(), context);
            }
         });
      }
   }

   private static void addMossHanger(BlockPos pos, Context context) {
      while (context.isAir(pos.below()) && context.random().nextFloat() >= 0.5F) {
         context.setBlock(pos, (BlockState)((HangingMossBlock)ModBlocks.PALE_HANGING_MOSS.get()).defaultBlockState().setValue(HangingMossBlock.TIP, false));
         pos = pos.below();
      }

      context.setBlock(pos, (BlockState)((HangingMossBlock)ModBlocks.PALE_HANGING_MOSS.get()).defaultBlockState().setValue(HangingMossBlock.TIP, true));
   }
}
