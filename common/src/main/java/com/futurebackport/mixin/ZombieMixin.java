package com.futurebackport.mixin;

import net.minecraft.nbt.CompoundTag;

import com.futurebackport.entity.CamelHuskSpawning;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Zombie.class})
public class ZombieMixin {
   @Inject(
      method = {"finalizeSpawn"},
      at = {@At("TAIL")}
   )
   private void futurebackport$camelHusk(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir
   ) {
      if ((Object)this instanceof Husk husk) {
         CamelHuskSpawning.afterHuskSpawn(husk, level, difficulty, spawnType);
      }
   }
}
