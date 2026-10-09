package com.futurebackport.entity;

import com.futurebackport.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class Parched extends AbstractSkeleton {
   public Parched(EntityType<? extends AbstractSkeleton> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
   }

   public static boolean checkSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random) && (MobSpawnType.isSpawner(spawnType) || level.canSeeSky(pos));
   }

   protected AbstractArrow getArrow(ItemStack arrow, float velocity, @Nullable ItemStack weapon) {
      AbstractArrow projectile = super.getArrow(arrow, velocity, weapon);
      if (projectile instanceof Arrow tipped) {
         tipped.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600));
      }

      return projectile;
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)ModSounds.PARCHED_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return (SoundEvent)ModSounds.PARCHED_HURT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.PARCHED_DEATH.get();
   }

   public SoundEvent getStepSound() {
      return (SoundEvent)ModSounds.PARCHED_STEP.get();
   }

   protected int getHardAttackInterval() {
      return 50;
   }

   protected int getAttackInterval() {
      return 70;
   }

   protected boolean isSunBurnTick() {
      return false;
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() != MobEffects.WEAKNESS && super.canBeAffected(effect);
   }
}
