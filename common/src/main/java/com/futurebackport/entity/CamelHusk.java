package com.futurebackport.entity;

import com.futurebackport.registry.ModSounds;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.AgeableMob.AgeableMobGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CamelHusk extends Camel {
   private static final Map<SoundEvent, Supplier<SoundEvent>> SOUND_SWAPS = Map.of(
      SoundEvents.CAMEL_DASH, ModSounds.CAMEL_HUSK_DASH, SoundEvents.CAMEL_SIT, ModSounds.CAMEL_HUSK_SIT, SoundEvents.CAMEL_STAND, ModSounds.CAMEL_HUSK_STAND
   );

   public CamelHusk(EntityType<? extends Camel> type, Level level) {
      super(type, level);
   }

   public static boolean checkSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
      return level.getDifficulty() != Difficulty.PEACEFUL
         && (MobSpawnType.ignoresLightRequirements(spawnType) || Monster.isDarkEnoughToSpawn(level, pos, random))
         && Mob.checkMobSpawnRules(type, level, spawnType, pos, random)
         && (MobSpawnType.isSpawner(spawnType) || level.canSeeSky(pos));
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return true;
   }

   public boolean isMobControlled() {
      return this.getFirstPassenger() instanceof Mob;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      this.setPersistenceRequired();
      return super.mobInteract(player, hand);
   }

   public boolean canBeLeashed() {
      return !this.isMobControlled() && super.canBeLeashed();
   }

   public boolean isFood(ItemStack stack) {
      return stack.is(Items.RABBIT_FOOT);
   }

   public boolean canMate(Animal partner) {
      return false;
   }

   @Nullable
   public Camel getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return null;
   }

   public boolean canFallInLove() {
      return false;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
      return super.finalizeSpawn(level, difficulty, spawnType, new AgeableMobGroupData(false));
   }

   protected SoundEvent getAmbientSound() {
      return (SoundEvent)ModSounds.CAMEL_HUSK_AMBIENT.get();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.CAMEL_HUSK_DEATH.get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return (SoundEvent)ModSounds.CAMEL_HUSK_HURT.get();
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      if (state.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
         this.playSound((SoundEvent)ModSounds.CAMEL_HUSK_STEP_SAND.get(), 0.4F, 1.0F);
      } else {
         this.playSound((SoundEvent)ModSounds.CAMEL_HUSK_STEP.get(), 0.4F, 1.0F);
      }
   }

   protected SoundEvent getEatingSound() {
      return (SoundEvent)ModSounds.CAMEL_HUSK_EAT.get();
   }

   public SoundEvent getSaddleSoundEvent() {
      return (SoundEvent)ModSounds.CAMEL_HUSK_SADDLE.get();
   }

   public void makeSound(@Nullable SoundEvent sound) {
      if (sound != null) {
         Supplier<SoundEvent> swapped = SOUND_SWAPS.get(sound);
         if (swapped != null) {
            sound = swapped.get();
         }
      }

      super.makeSound(sound);
   }
}
