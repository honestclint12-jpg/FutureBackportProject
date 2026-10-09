package com.futurebackport.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class VariantThrownEgg extends ThrownEgg {
   private final FarmAnimalVariant variant;
   private final Item item;

   public VariantThrownEgg(Level level, LivingEntity shooter, FarmAnimalVariant variant, Item item) {
      super(level, shooter);
      this.variant = variant;
      this.item = item;
   }

   public VariantThrownEgg(Level level, double x, double y, double z, FarmAnimalVariant variant, Item item) {
      super(level, x, y, z);
      this.variant = variant;
      this.item = item;
   }

   protected Item getDefaultItem() {
      return this.item == null ? super.getDefaultItem() : this.item;
   }

   protected void onHit(HitResult result) {
      Type type = result.getType();
      if (type == Type.ENTITY) {
         this.onHitEntity((EntityHitResult)result);
      } else if (type == Type.BLOCK) {
         this.onHitBlock((BlockHitResult)result);
      }

      if (!this.level().isClientSide()) {
         if (this.random.nextInt(8) == 0) {
            int count = this.random.nextInt(32) == 0 ? 4 : 1;

            for (int i = 0; i < count; i++) {
               Chicken chick = (Chicken)EntityType.CHICKEN.create(this.level());
               if (chick != null) {
                  chick.setAge(-24000);
                  chick.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                  FarmAnimalVariant.ATTACHMENT.set(chick, this.variant);
                  this.level().addFreshEntity(chick);
               }
            }
         }

         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }
   }

   public void handleEntityEvent(byte id) {
      if (id == 3) {
         for (int i = 0; i < 8; i++) {
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  (this.random.nextFloat() - 0.5) * 0.08,
                  (this.random.nextFloat() - 0.5) * 0.08,
                  (this.random.nextFloat() - 0.5) * 0.08
               );
         }
      } else {
         super.handleEntityEvent(id);
      }
   }
}
