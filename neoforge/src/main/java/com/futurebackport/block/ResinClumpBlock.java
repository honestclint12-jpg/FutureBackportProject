package com.futurebackport.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ResinClumpBlock extends MultifaceBlock {
   public static final MapCodec<ResinClumpBlock> CODEC = simpleCodec(ResinClumpBlock::new);
   private final MultifaceSpreader spreader = new MultifaceSpreader(this);

   public ResinClumpBlock(Properties properties) {
      super(properties);
   }

   protected MapCodec<ResinClumpBlock> codec() {
      return CODEC;
   }

   public MultifaceSpreader getSpreader() {
      return this.spreader;
   }
}
