package com.futurebackport.mixin;

import com.futurebackport.client.LeafParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LeavesBlock.class})
public class LeavesBlockMixin {
   @Inject(
      method = {"animateTick"},
      at = {@At("TAIL")}
   )
   private void futurebackport$fallingLeaves(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      LeafParticles.animateTick(state, level, pos, random);
   }
}
