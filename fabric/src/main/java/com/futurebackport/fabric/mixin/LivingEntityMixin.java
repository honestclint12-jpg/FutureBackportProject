package com.futurebackport.fabric.mixin;

import com.futurebackport.entity.HappyGhast;
import com.futurebackport.entity.nautilus.BreathOfTheNautilus;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Stands in for NeoForge's LivingBreatheEvent: ghastlings and Breath of the Nautilus count as water breathing, so air
 * neither drains nor refills underwater.
 */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/effect/MobEffectUtil;hasWaterBreathing(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean futurebackport$canBreathe(LivingEntity entity, Operation<Boolean> original) {
        if (entity instanceof HappyGhast ghast && !ghast.canDrown()) {
            return true;
        }
        return BreathOfTheNautilus.canBreathe(entity) || original.call(entity);
    }
}
