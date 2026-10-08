package com.futurebackport.fabric.mixin;

import com.futurebackport.entity.AgeLock;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stands in for NeoForge's EntityTickEvent.Post, which the age lock only uses for ageable mobs. */
@Mixin(AgeableMob.class)
abstract class AgeableMobMixin {

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void futurebackport$tick(CallbackInfo ci) {
        AgeLock.onTick((AgeableMob) (Object) this);
    }
}
