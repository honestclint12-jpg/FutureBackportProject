package com.futurebackport.mixin;

import com.futurebackport.block.CopperChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ChestBlockEntity.class})
public abstract class ChestBlockEntityMixin {
   @ModifyVariable(
      method = {"playSound"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private static SoundEvent futurebackport$copperChestSound(SoundEvent sound, Level level, BlockPos pos, BlockState state) {
      return state.getBlock() instanceof CopperChestBlock chest ? CopperChestBlock.hingeSound(chest.getWeatherState(), sound == SoundEvents.CHEST_OPEN) : sound;
   }
}
