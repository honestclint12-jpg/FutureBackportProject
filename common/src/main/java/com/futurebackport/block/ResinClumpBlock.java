package com.futurebackport.block;

import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ResinClumpBlock extends MultifaceBlock {
   private final MultifaceSpreader spreader = new MultifaceSpreader(this);

   public ResinClumpBlock(Properties properties) {
      super(properties);
   }

   public MultifaceSpreader getSpreader() {
      return this.spreader;
   }
}
