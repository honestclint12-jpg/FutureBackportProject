package com.futurebackport.fabric.mixin;

import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.futurebackport.entity.SoundVariants;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stands in for NeoForge's FinalizeSpawnEvent. */
@Mixin(Mob.class)
abstract class MobMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void futurebackport$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                             @Nullable SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        FarmAnimalVariantEvents.onFinalizeSpawn(self, level, spawnType);
        SoundVariants.onFinalizeSpawn(self, level);
    }
}
