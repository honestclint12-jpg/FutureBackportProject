package com.futurebackport.mixin;

import com.futurebackport.block.MossyCarpetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({SimpleBlockFeature.class})
public abstract class SimpleBlockFeatureMixin {
   @Redirect(
      method = {"place"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
      )
   )
   private boolean futurebackport$placeMossyCarpet(WorldGenLevel level, BlockPos pos, BlockState state, int flags) {
      if (state.getBlock() instanceof MossyCarpetBlock carpet) {
         carpet.placeAt(level, pos, level.getRandom(), flags);
         return true;
      } else {
         return level.setBlock(pos, state, flags);
      }
   }
}
