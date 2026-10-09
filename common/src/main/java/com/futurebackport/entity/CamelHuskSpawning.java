package com.futurebackport.entity;

import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.ServerLevelAccessor;

public final class CamelHuskSpawning {
   public static final float CHANCE = 0.1F;

   private CamelHuskSpawning() {
   }

   public static void afterHuskSpawn(Husk husk, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType) {
      if (spawnType == MobSpawnType.NATURAL && !husk.isPassenger()) {
         BlockPos pos = husk.blockPosition();
         if (level.noCollision(((EntityType)ModEntities.CAMEL_HUSK.get()).getAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
            if (level.getRandom().nextFloat() < 0.1F) {
               mount(husk, level, difficulty, spawnType);
            }
         }
      }
   }

   public static CamelHusk mount(Husk husk, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType) {
      CamelHusk camel = (CamelHusk)((EntityType)ModEntities.CAMEL_HUSK.get()).create(husk.level());
      if (camel == null) {
         return null;
      } else {
         husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)ModItems.IRON_SPEAR.get()));
         camel.setPos(husk.getX(), husk.getY(), husk.getZ());
         camel.finalizeSpawn(level, difficulty, spawnType, null, null);
         husk.startRiding(camel, true);
         level.addFreshEntity(camel);
         Parched parched = (Parched)((EntityType)ModEntities.PARCHED.get()).create(husk.level());
         if (parched != null) {
            parched.moveTo(husk.getX(), husk.getY(), husk.getZ(), husk.getYRot(), 0.0F);
            parched.finalizeSpawn(level, difficulty, spawnType, null, null);
            parched.startRiding(camel, true);
            level.addFreshEntityWithPassengers(parched);
         }

         return camel;
      }
   }
}
