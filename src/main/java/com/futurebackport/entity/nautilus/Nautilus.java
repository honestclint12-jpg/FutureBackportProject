package com.futurebackport.entity.nautilus;

import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModSounds;
import com.futurebackport.registry.ModTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Nautilus extends AbstractNautilus {
   private static final int TOTAL_AIR_SUPPLY = 300;

   public Nautilus(EntityType<? extends Nautilus> type, Level level) {
      super(type, level);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new PanicGoal(this, 1.6) {
         public boolean canUse() {
            return Nautilus.this.isBaby() && super.canUse();
         }
      });
      this.goalSelector.addGoal(1, new BreedGoal(this, 0.4));
      this.goalSelector.addGoal(2, new TemptGoal(this, 1.3, s -> s.is(ModTags.NAUTILUS_FOOD), false));
      this.goalSelector.addGoal(3, new NautilusGoals.Charge(this, 0.6F, ModSounds.NAUTILUS_DASH));
      this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
         public boolean canUse() {
            return !Nautilus.this.isTame() && !Nautilus.this.isBaby() && Nautilus.this.isInWater() && super.canUse();
         }
      });
      this.targetSelector.addGoal(2, new NautilusGoals.HuntHostiles(this));
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      Nautilus baby = (Nautilus)((EntityType)ModEntities.NAUTILUS.get()).create(level);
      if (baby != null && this.isTame() && this.getOwnerUUID() != null) {
         baby.setOwnerUUID(this.getOwnerUUID());
         baby.setTame(true, true);
      }

      return baby;
   }

   public EntityDimensions getDefaultDimensions(Pose pose) {
      return this.isBaby() ? super.getDefaultDimensions(pose).scale(0.5F) : super.getDefaultDimensions(pose);
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby()
         ? (SoundEvent)(this.isUnderWater() ? ModSounds.BABY_NAUTILUS_AMBIENT : ModSounds.BABY_NAUTILUS_AMBIENT_ON_LAND).get()
         : (SoundEvent)(this.isUnderWater() ? ModSounds.NAUTILUS_AMBIENT : ModSounds.NAUTILUS_AMBIENT_ON_LAND).get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isBaby()
         ? (SoundEvent)(this.isUnderWater() ? ModSounds.BABY_NAUTILUS_HURT : ModSounds.BABY_NAUTILUS_HURT_ON_LAND).get()
         : (SoundEvent)(this.isUnderWater() ? ModSounds.NAUTILUS_HURT : ModSounds.NAUTILUS_HURT_ON_LAND).get();
   }

   protected SoundEvent getDeathSound() {
      return this.isBaby()
         ? (SoundEvent)(this.isUnderWater() ? ModSounds.BABY_NAUTILUS_DEATH : ModSounds.BABY_NAUTILUS_DEATH_ON_LAND).get()
         : (SoundEvent)(this.isUnderWater() ? ModSounds.NAUTILUS_DEATH : ModSounds.NAUTILUS_DEATH_ON_LAND).get();
   }

   @Override
   protected SoundEvent getDashSound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.NAUTILUS_DASH : ModSounds.NAUTILUS_DASH_ON_LAND).get();
   }

   @Override
   protected SoundEvent getDashReadySound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.NAUTILUS_DASH_READY : ModSounds.NAUTILUS_DASH_READY_ON_LAND).get();
   }

   @Override
   protected void playEatingSound() {
      this.makeSound((SoundEvent)(this.isBaby() ? ModSounds.BABY_NAUTILUS_EAT : ModSounds.NAUTILUS_EAT).get());
   }

   protected SoundEvent getSwimSound() {
      return (SoundEvent)(this.isBaby() ? ModSounds.BABY_NAUTILUS_SWIM : ModSounds.NAUTILUS_SWIM).get();
   }

   public int getMaxAirSupply() {
      return 300;
   }

   public void baseTick() {
      int air = this.getAirSupply();
      super.baseTick();
      if (!this.isNoAi() && this.level() instanceof ServerLevel) {
         if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(air - 1);
            if (this.getAirSupply() <= -20) {
               this.setAirSupply(0);
               this.hurt(this.damageSources().dryOut(), 2.0F);
            }
         } else {
            this.setAirSupply(300);
         }
      }
   }

   public boolean canBeLeashed() {
      return !this.isAggravated() && super.canBeLeashed();
   }
}
