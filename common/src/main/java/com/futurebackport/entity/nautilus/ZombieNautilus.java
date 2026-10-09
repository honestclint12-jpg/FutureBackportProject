package com.futurebackport.entity.nautilus;

import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.crafting.Ingredient;
import com.futurebackport.registry.ModSounds;
import com.futurebackport.registry.ModTags;
import java.util.EnumSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class ZombieNautilus extends AbstractNautilus {
   private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(ZombieNautilus.class, EntityDataSerializers.INT);

   public ZombieNautilus(EntityType<? extends ZombieNautilus> type, Level level) {
      super(type, level);
   }

   /** Undead (1.21 uses the zombies entity tag; 1.20.1 asks the mob). */
   @Override
   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   public static Builder createAttributes() {
      return AbstractNautilus.createAttributes().add(Attributes.MOVEMENT_SPEED, 1.1F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new TemptGoal(this, 0.9, Ingredient.of(ModTags.NAUTILUS_FOOD), false));
      this.goalSelector.addGoal(2, new NautilusGoals.Charge(this, 0.5F, ModSounds.ZOMBIE_NAUTILUS_DASH));
      this.goalSelector.addGoal(3, new ZombieNautilus.ChaseTarget());
      this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.targetSelector.addGoal(0, new ZombieNautilus.RiderTarget());
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
         public boolean canUse() {
            return !ZombieNautilus.this.isTame() && ZombieNautilus.this.isInWater() && super.canUse();
         }
      });
      this.targetSelector.addGoal(2, new NautilusGoals.HuntHostiles(this));
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT, ZombieNautilus.Variant.TEMPERATE.ordinal());
   }

   public ZombieNautilus.Variant getVariant() {
      return ZombieNautilus.Variant.values()[this.entityData.get(DATA_VARIANT)];
   }

   public void setVariant(ZombieNautilus.Variant variant) {
      this.entityData.set(DATA_VARIANT, variant.ordinal());
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("variant", this.getVariant() == ZombieNautilus.Variant.WARM ? "minecraft:warm" : "minecraft:temperate");
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setVariant(tag.getString("variant").endsWith("warm") ? ZombieNautilus.Variant.WARM : ZombieNautilus.Variant.TEMPERATE);
   }

   @Override
   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag
   ) {
      this.setVariant(
         level.getBiome(this.blockPosition()).is(ModTags.SPAWNS_CORAL_VARIANT_ZOMBIE_NAUTILUS) ? ZombieNautilus.Variant.WARM : ZombieNautilus.Variant.TEMPERATE
      );
      return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
   }

   public void aiStep() {
      if (this.isAlive() && this.isSunBurnTick() && this.getBodyArmorItem().isEmpty()) {
         this.setSecondsOnFire(8);
      }

      super.aiStep();
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return null;
   }

   public boolean canFallInLove() {
      return false;
   }

   public void setAge(int age) {
      super.setAge(Math.max(age, 0));
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.ZOMBIE_NAUTILUS_AMBIENT : ModSounds.ZOMBIE_NAUTILUS_AMBIENT_ON_LAND).get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.ZOMBIE_NAUTILUS_HURT : ModSounds.ZOMBIE_NAUTILUS_HURT_ON_LAND).get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.ZOMBIE_NAUTILUS_DEATH : ModSounds.ZOMBIE_NAUTILUS_DEATH_ON_LAND).get();
   }

   @Override
   protected SoundEvent getDashSound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.ZOMBIE_NAUTILUS_DASH : ModSounds.ZOMBIE_NAUTILUS_DASH_ON_LAND).get();
   }

   @Override
   protected SoundEvent getDashReadySound() {
      return (SoundEvent)(this.isUnderWater() ? ModSounds.ZOMBIE_NAUTILUS_DASH_READY : ModSounds.ZOMBIE_NAUTILUS_DASH_READY_ON_LAND).get();
   }

   @Override
   protected void playEatingSound() {
      this.playSound((SoundEvent)ModSounds.ZOMBIE_NAUTILUS_EAT.get(), this.getSoundVolume(), this.getVoicePitch());
   }

   protected SoundEvent getSwimSound() {
      return (SoundEvent)ModSounds.ZOMBIE_NAUTILUS_SWIM.get();
   }

   public boolean canBeLeashed(Player player) {
      return !this.isAggravated() && !this.isMobControlled() && super.canBeLeashed(player);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return !this.isTame() && !this.hasCustomName() && !this.isLeashed() && !this.isPersistenceRequired();
   }

   private class ChaseTarget extends Goal {
      ChaseTarget() {
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         return ZombieNautilus.this.getTarget() != null && ZombieNautilus.this.getTarget().isAlive() && !ZombieNautilus.this.isTame();
      }

      public void tick() {
         if (ZombieNautilus.this.getTarget() != null && ZombieNautilus.this.getNavigation().isDone()) {
            ZombieNautilus.this.getNavigation().moveTo(ZombieNautilus.this.getTarget(), 1.2);
         }
      }
   }

   private class RiderTarget extends Goal {
      public boolean canUse() {
         if (ZombieNautilus.this.getFirstPassenger() instanceof Mob rider
            && rider.getTarget() != null
            && rider.getTarget() != ZombieNautilus.this.getTarget()
            && !ZombieNautilus.this.isTame()) {
            ZombieNautilus.this.setTarget(rider.getTarget());
         }

         return false;
      }
   }

   public static enum Variant {
      TEMPERATE,
      WARM;
   }
}
