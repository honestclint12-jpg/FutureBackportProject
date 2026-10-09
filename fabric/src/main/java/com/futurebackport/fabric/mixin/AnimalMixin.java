package com.futurebackport.fabric.mixin;

import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Stands in for NeoForge's BabyEntitySpawnEvent. */
@Mixin(Animal.class)
abstract class AnimalMixin {

    @WrapOperation(method = "spawnChildFromBreeding", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;"))
    private AgeableMob futurebackport$onBabySpawn(Animal self, ServerLevel level, AgeableMob mate, Operation<AgeableMob> original) {
        AgeableMob child = original.call(self, level, mate);
        FarmAnimalVariantEvents.onBabySpawn(self, (Animal) mate, child);
        return child;
    }
}
