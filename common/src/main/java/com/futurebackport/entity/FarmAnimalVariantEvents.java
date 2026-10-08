package com.futurebackport.entity;

import com.futurebackport.platform.Services;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import com.futurebackport.network.FarmAnimalVariantPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

/** Gives cows, pigs and chickens a climate variant. Loader modules call these from their own events. */
public final class FarmAnimalVariantEvents {
   private FarmAnimalVariantEvents() {
   }

   /** A mob finished spawning (not by breeding): pick the variant for the biome it spawned in. */
   public static void onFinalizeSpawn(Mob mob, LevelAccessor level, MobSpawnType spawnType) {
      if (FarmAnimalVariant.hasVariants(mob) && spawnType != MobSpawnType.BREEDING) {
         FarmAnimalVariant.ATTACHMENT.set(mob, FarmAnimalVariant.forBiome(level, mob.blockPosition()));
      }
   }

   /** Babies inherit a random parent's variant. */
   public static void onBabySpawn(Mob parentA, Mob parentB, @Nullable AgeableMob child) {
      if (child != null && FarmAnimalVariant.hasVariants(child)) {
         Mob parent = child.getRandom().nextBoolean() ? parentA : parentB;
         FarmAnimalVariant.ATTACHMENT.set(child, FarmAnimalVariant.get(parent));
      }
   }

   /** A player started seeing an entity: send them its variant so it renders right. */
   public static void onStartTracking(Entity target, Player player) {
      if (FarmAnimalVariant.hasVariants(target) && player instanceof ServerPlayer serverPlayer) {
         Services.NETWORK.sendToPlayer(serverPlayer, new FarmAnimalVariantPayload(target.getId(), FarmAnimalVariant.get(target)));
      }
   }
}
