package com.futurebackport.entity;

import com.futurebackport.network.FarmAnimalVariantPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FarmAnimalVariantEvents {
   @SubscribeEvent
   public void onFinalizeSpawn(FinalizeSpawnEvent event) {
      Mob mob = event.getEntity();
      if (FarmAnimalVariant.hasVariants(mob) && event.getSpawnType() != MobSpawnType.BREEDING) {
         mob.setData(FarmAnimalVariant.ATTACHMENT, FarmAnimalVariant.forBiome(event.getLevel(), mob.blockPosition()));
      }
   }

   @SubscribeEvent
   public void onBabySpawn(BabyEntitySpawnEvent event) {
      AgeableMob child = event.getChild();
      if (child != null && FarmAnimalVariant.hasVariants(child)) {
         Mob parent = child.getRandom().nextBoolean() ? event.getParentA() : event.getParentB();
         child.setData(FarmAnimalVariant.ATTACHMENT, FarmAnimalVariant.get(parent));
      }
   }

   @SubscribeEvent
   public void onStartTracking(StartTracking event) {
      Entity target = event.getTarget();
      if (FarmAnimalVariant.hasVariants(target) && event.getEntity() instanceof ServerPlayer player) {
         PacketDistributor.sendToPlayer(player, new FarmAnimalVariantPayload(target.getId(), FarmAnimalVariant.get(target)), new CustomPacketPayload[0]);
      }
   }
}
