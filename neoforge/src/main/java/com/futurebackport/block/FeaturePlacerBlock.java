package com.futurebackport.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BonemealableBlock.Type;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class FeaturePlacerBlock extends Block implements BonemealableBlock {
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   public FeaturePlacerBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
      super(properties);
      this.feature = feature;
   }

   protected MapCodec<? extends Block> codec() {
      return simpleCodec(p -> new FeaturePlacerBlock(this.feature, p));
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
      return level.getBlockState(pos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      level.registryAccess()
         .registry(Registries.CONFIGURED_FEATURE)
         .flatMap(registry -> registry.getHolder(this.feature))
         .ifPresent(patch -> ((ConfiguredFeature)patch.value()).place(level, level.getChunkSource().getGenerator(), random, pos.above()));
   }

   public Type getType() {
      return Type.NEIGHBOR_SPREADER;
   }
}
