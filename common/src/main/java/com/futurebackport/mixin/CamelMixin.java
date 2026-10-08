package com.futurebackport.mixin;

import com.futurebackport.entity.CamelHusk;
import com.futurebackport.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({Camel.class})
public class CamelMixin {
   @ModifyArg(
      method = {"tick"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
      ),
      index = 2
   )
   private SoundEvent futurebackport$dashReadySound(SoundEvent sound) {
      return (Object)this instanceof CamelHusk ? (SoundEvent)ModSounds.CAMEL_HUSK_DASH_READY.get() : sound;
   }
}
