package com.futurebackport.mixin;

import com.futurebackport.platform.Services;

import com.futurebackport.block.CactusFlowerBlock;
import com.futurebackport.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CactusBlock.class})
public abstract class CactusBlockMixin {
   @Inject(
      method = {"randomTick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void futurebackport$growWithFlowers(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      ci.cancel();
      Block self = (Block)(Object)this;
      BlockPos above = pos.above();
      if (level.isEmptyBlock(above)) {
         int height = 1;
         int age = (Integer)state.getValue(CactusBlock.AGE);

         while (level.getBlockState(pos.below(height)).is(self)) {
            if (++height == 3 && age == 15) {
               return;
            }
         }

         if (Services.PLATFORM.canCropGrow(level, above, state, true)) {
            if (age == 8 && ((CactusFlowerBlock)ModBlocks.CACTUS_FLOWER.get()).defaultBlockState().canSurvive(level, above)) {
               if (random.nextDouble() <= (height >= 3 ? 0.25 : 0.1)) {
                  level.setBlockAndUpdate(above, ((CactusFlowerBlock)ModBlocks.CACTUS_FLOWER.get()).defaultBlockState());
               }
            } else if (age == 15 && height < 3) {
               level.setBlockAndUpdate(above, self.defaultBlockState());
               BlockState reset = (BlockState)state.setValue(CactusBlock.AGE, 0);
               level.setBlock(pos, reset, 260);
               level.neighborChanged(reset, above, self, pos, false);
            }

            if (age < 15) {
               level.setBlock(pos, (BlockState)state.setValue(CactusBlock.AGE, age + 1), 260);
            }

            Services.PLATFORM.onCropGrown(level, pos, state);
         }
      }
   }
}
