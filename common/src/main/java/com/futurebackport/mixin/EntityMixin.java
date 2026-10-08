package com.futurebackport.mixin;

import com.futurebackport.entity.SoundVariants;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Entity.class})
public class EntityMixin {
   @ModifyVariable(
      method = {"playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private SoundEvent futurebackport$soundVariant(SoundEvent sound) {
      return SoundVariants.swap((Entity)(Object)this, sound);
   }
}
