package com.futurebackport.mixin;

import com.futurebackport.entity.CopperGolemBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CarvedPumpkinBlock.class})
public class CarvedPumpkinBlockMixin {
   @Inject(
      method = {"trySpawnGolem"},
      at = {@At("TAIL")}
   )
   private void futurebackport$copperGolem(Level level, BlockPos pos, CallbackInfo ci) {
      CopperGolemBuilding.trySpawn(level, pos);
   }

   @Inject(
      method = {"canSpawnGolem"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void futurebackport$canSpawnCopperGolem(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (!(Boolean)cir.getReturnValue() && CopperGolemBuilding.weatherStateOf(level.getBlockState(pos.below())) != null) {
         cir.setReturnValue(true);
      }
   }
}
