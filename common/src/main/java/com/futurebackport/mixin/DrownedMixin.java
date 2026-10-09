package com.futurebackport.mixin;

import net.minecraft.nbt.CompoundTag;

import com.futurebackport.entity.nautilus.ZombieNautilus;
import com.futurebackport.registry.ModEntities;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Drowned.class})
public class DrownedMixin {
   @Inject(
      method = {"finalizeSpawn"},
      at = {@At("TAIL")}
   )
   private void futurebackport$zombieNautilusJockey(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData groupData, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> cir
   ) {
      Drowned drowned = (Drowned)(Object)this;
      if ((spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.STRUCTURE)
         && drowned.getMainHandItem().is(Items.TRIDENT)
         && !drowned.isBaby()
         && !drowned.isPassenger()
         && !(level.getRandom().nextFloat() >= 0.5F)
         && !level.getBiome(drowned.blockPosition()).is(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS)) {
         ZombieNautilus nautilus = (ZombieNautilus)((EntityType)ModEntities.ZOMBIE_NAUTILUS.get()).create(drowned.level());
         if (nautilus != null) {
            if (spawnType == MobSpawnType.STRUCTURE) {
               nautilus.setPersistenceRequired();
            }

            nautilus.moveTo(drowned.getX(), drowned.getY(), drowned.getZ(), drowned.getYRot(), 0.0F);
            nautilus.finalizeSpawn(level, difficulty, MobSpawnType.JOCKEY, null, null);
            drowned.startRiding(nautilus, true);
            level.addFreshEntity(nautilus);
         }
      }
   }
}
