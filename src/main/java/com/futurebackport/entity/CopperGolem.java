package com.futurebackport.entity;

import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.block.entity.CopperGolemStatueBlockEntity;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CopperGolem extends AbstractGolem {
   private static final long IGNORE_WEATHERING_TICK = -2L;
   private static final long UNSET_WEATHERING_TICK = -1L;
   private static final int WEATHERING_TICK_FROM = 504000;
   private static final int WEATHERING_TICK_TO = 552000;
   private static final EntityDataAccessor<Integer> DATA_WEATHER_STATE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(CopperGolem.class, EntityDataSerializers.INT);
   @Nullable
   private UUID lastLightningBoltUUID;
   private long nextWeatheringTick = -1L;
   int transportCooldown;
   private int idleAnimationStartTick = 0;
   public final AnimationState idleAnimationState = new AnimationState();
   public final AnimationState interactionGetItemAnimationState = new AnimationState();
   public final AnimationState interactionGetNoItemAnimationState = new AnimationState();
   public final AnimationState interactionDropItemAnimationState = new AnimationState();
   public final AnimationState interactionDropNoItemAnimationState = new AnimationState();
   private static final float TURN_TO_STATUE_CHANCE = 0.0058F;

   public CopperGolem(EntityType<? extends AbstractGolem> type, Level level) {
      super(type, level);
      if (this.getNavigation() instanceof GroundPathNavigation nav) {
         nav.setCanOpenDoors(true);
      }

      this.setPersistenceRequired();
      this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathType.DANGER_OTHER, 16.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
      this.transportCooldown = this.getRandom().nextInt(60, 100);
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MOVEMENT_SPEED, 0.2F)
         .add(Attributes.STEP_HEIGHT, 1.0)
         .add(Attributes.MAX_HEALTH, 12.0)
         .add(Attributes.FOLLOW_RANGE, 48.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new PanicGoal(this, 1.5));
      this.goalSelector.addGoal(1, new TransportItemsGoal(this));
      this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0) {
         public boolean canUse() {
            return CopperGolem.this.transportCooldown > 0 && super.canUse();
         }
      });
      this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_WEATHER_STATE, WeatherState.UNAFFECTED.ordinal());
      builder.define(DATA_STATE, CopperGolem.State.IDLE.ordinal());
   }

   public CopperGolem.State getState() {
      return CopperGolem.State.values()[this.entityData.get(DATA_STATE)];
   }

   public void setState(CopperGolem.State state) {
      this.entityData.set(DATA_STATE, state.ordinal());
   }

   public WeatherState getWeatherState() {
      return WeatherState.values()[this.entityData.get(DATA_WEATHER_STATE)];
   }

   public void setWeatherState(WeatherState state) {
      this.entityData.set(DATA_WEATHER_STATE, state.ordinal());
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putLong("next_weather_age", this.nextWeatheringTick);
      tag.putString("weather_state", this.getWeatherState().getSerializedName());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.nextWeatheringTick = tag.contains("next_weather_age") ? tag.getLong("next_weather_age") : -1L;

      for (WeatherState state : WeatherState.values()) {
         if (state.getSerializedName().equals(tag.getString("weather_state"))) {
            this.setWeatherState(state);
         }
      }
   }

   protected void customServerAiStep() {
      if (this.transportCooldown > 0) {
         this.transportCooldown--;
      }

      super.customServerAiStep();
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         if (!this.isNoAi()) {
            this.setupAnimationStates();
         }
      } else {
         this.updateWeathering(this.level().getGameTime());
      }
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.isEmpty() && !this.getMainHandItem().isEmpty()) {
         BehaviorUtils.throwItem(this, this.getMainHandItem(), player.position());
         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         Level level = this.level();
         if (level.isClientSide()) {
            return InteractionResult.PASS;
         } else if (stack.is(Items.HONEYCOMB) && this.nextWeatheringTick != -2L) {
            level.levelEvent(null, 3003, this.blockPosition(), 0);
            this.nextWeatheringTick = -2L;
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
         } else if (stack.is(ItemTags.AXES) && this.nextWeatheringTick == -2L) {
            level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
            level.levelEvent(null, 3004, this.blockPosition(), 0);
            this.nextWeatheringTick = -1L;
            stack.hurtAndBreak(1, player, CopperGolem.LivingEntityHand.slot(hand));
            return InteractionResult.SUCCESS;
         } else if (stack.is(ItemTags.AXES) && this.getWeatherState() != WeatherState.UNAFFECTED) {
            level.playSound(null, this, SoundEvents.AXE_SCRAPE, this.getSoundSource(), 1.0F, 1.0F);
            level.levelEvent(null, 3005, this.blockPosition(), 0);
            this.nextWeatheringTick = -1L;
            this.setWeatherState(WeatherState.values()[this.getWeatherState().ordinal() - 1]);
            stack.hurtAndBreak(1, player, CopperGolem.LivingEntityHand.slot(hand));
            return InteractionResult.SUCCESS;
         } else {
            return super.mobInteract(player, hand);
         }
      }
   }

   private void updateWeathering(long gameTime) {
      if (this.nextWeatheringTick != -2L) {
         if (this.nextWeatheringTick == -1L) {
            this.nextWeatheringTick = gameTime + this.random.nextIntBetweenInclusive(504000, 552000);
         } else {
            boolean fullyOxidized = this.getWeatherState() == WeatherState.OXIDIZED;
            if (gameTime >= this.nextWeatheringTick && !fullyOxidized) {
               WeatherState next = WeatherState.values()[this.getWeatherState().ordinal() + 1];
               this.setWeatherState(next);
               this.nextWeatheringTick = next == WeatherState.OXIDIZED ? 0L : this.nextWeatheringTick + this.random.nextIntBetweenInclusive(504000, 552000);
            }

            if (fullyOxidized && this.level().getBlockState(this.blockPosition()).isAir() && this.random.nextFloat() <= 0.0058F) {
               this.turnToStatue((ServerLevel)this.level());
            }
         }
      }
   }

   public void turnToStatue(ServerLevel level) {
      BlockPos pos = this.blockPosition();
      CopperGolemStatueBlock.Pose[] poses = CopperGolemStatueBlock.Pose.values();
      level.setBlock(
         pos,
         (BlockState)((BlockState)((Block)ModBlocks.COPPER_GOLEM_STATUE.weathering().get(WeatherState.OXIDIZED).get())
               .defaultBlockState()
               .setValue(CopperGolemStatueBlock.POSE, poses[this.random.nextInt(poses.length)]))
            .setValue(CopperGolemStatueBlock.FACING, Direction.fromYRot(this.getYRot())),
         3
      );
      if (level.getBlockEntity(pos) instanceof CopperGolemStatueBlockEntity statue) {
         statue.createStatue(this);
         if (!this.getMainHandItem().isEmpty()) {
            this.spawnAtLocation(this.getMainHandItem());
         }

         this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         this.playSound((SoundEvent)ModSounds.COPPER_GOLEM_BECOME_STATUE.get());
         if (this.isLeashed()) {
            this.dropLeash(true, level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS));
         }

         this.discard();
      }
   }

   private void setupAnimationStates() {
      switch (this.getState()) {
         case IDLE:
            this.interactionGetNoItemAnimationState.stop();
            this.interactionGetItemAnimationState.stop();
            this.interactionDropItemAnimationState.stop();
            this.interactionDropNoItemAnimationState.stop();
            if (this.idleAnimationStartTick == this.tickCount) {
               this.idleAnimationState.start(this.tickCount);
            } else if (this.idleAnimationStartTick == 0) {
               this.idleAnimationStartTick = this.tickCount + this.random.nextInt(200, 240);
            }

            if (this.tickCount == this.idleAnimationStartTick + 10) {
               if (!this.isSilent()) {
                  this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSpinHeadSound(), this.getSoundSource(), 1.0F, 1.0F, false);
               }

               this.idleAnimationStartTick = 0;
            }
            break;
         case GETTING_ITEM:
            this.startOnly(this.interactionGetItemAnimationState);
            break;
         case GETTING_NO_ITEM:
            this.startOnly(this.interactionGetNoItemAnimationState);
            break;
         case DROPPING_ITEM:
            this.startOnly(this.interactionDropItemAnimationState);
            break;
         case DROPPING_NO_ITEM:
            this.startOnly(this.interactionDropNoItemAnimationState);
      }
   }

   private void startOnly(AnimationState active) {
      this.idleAnimationState.stop();
      this.idleAnimationStartTick = 0;

      for (AnimationState state : new AnimationState[]{
         this.interactionGetItemAnimationState,
         this.interactionGetNoItemAnimationState,
         this.interactionDropItemAnimationState,
         this.interactionDropNoItemAnimationState
      }) {
         if (state != active) {
            state.stop();
         }
      }

      active.startIfStopped(this.tickCount);
   }

   public void spawn(WeatherState weatherState) {
      this.setWeatherState(weatherState);
      this.playSound((SoundEvent)ModSounds.COPPER_GOLEM_SPAWN.get());
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
      this.playSound((SoundEvent)ModSounds.COPPER_GOLEM_SPAWN.get());
      return super.finalizeSpawn(level, difficulty, spawnType, groupData);
   }

   private SoundEvent getSpinHeadSound() {
      return switch (this.getWeatherState()) {
         case WEATHERED -> (SoundEvent)ModSounds.COPPER_GOLEM_WEATHERED_SPIN.get();
         case OXIDIZED -> (SoundEvent)ModSounds.COPPER_GOLEM_OXIDIZED_SPIN.get();
         default -> (SoundEvent)ModSounds.COPPER_GOLEM_SPIN.get();
      };
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return switch (this.getWeatherState()) {
         case WEATHERED -> (SoundEvent)ModSounds.COPPER_GOLEM_WEATHERED_HURT.get();
         case OXIDIZED -> (SoundEvent)ModSounds.COPPER_GOLEM_OXIDIZED_HURT.get();
         default -> (SoundEvent)ModSounds.COPPER_GOLEM_HURT.get();
      };
   }

   protected SoundEvent getDeathSound() {
      return switch (this.getWeatherState()) {
         case WEATHERED -> (SoundEvent)ModSounds.COPPER_GOLEM_WEATHERED_DEATH.get();
         case OXIDIZED -> (SoundEvent)ModSounds.COPPER_GOLEM_OXIDIZED_DEATH.get();
         default -> (SoundEvent)ModSounds.COPPER_GOLEM_DEATH.get();
      };
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      SoundEvent step = switch (this.getWeatherState()) {
         case WEATHERED -> (SoundEvent)ModSounds.COPPER_GOLEM_WEATHERED_STEP.get();
         case OXIDIZED -> (SoundEvent)ModSounds.COPPER_GOLEM_OXIDIZED_STEP.get();
         default -> (SoundEvent)ModSounds.COPPER_GOLEM_STEP.get();
      };
      this.playSound(step, 1.0F, 1.0F);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.75F * this.getEyeHeight(), 0.0);
   }

   protected void actuallyHurt(DamageSource source, float amount) {
      super.actuallyHurt(source, amount);
      this.setState(CopperGolem.State.IDLE);
   }

   public void thunderHit(ServerLevel level, LightningBolt bolt) {
      super.thunderHit(level, bolt);
      if (!bolt.getUUID().equals(this.lastLightningBoltUUID)) {
         this.lastLightningBoltUUID = bolt.getUUID();
         if (this.getWeatherState() != WeatherState.UNAFFECTED) {
            this.nextWeatheringTick = -1L;
            this.setWeatherState(WeatherState.values()[this.getWeatherState().ordinal() - 1]);
         }
      }
   }

   private static final class LivingEntityHand {
      static EquipmentSlot slot(InteractionHand hand) {
         return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
      }
   }

   public static enum State {
      IDLE,
      GETTING_ITEM,
      GETTING_NO_ITEM,
      DROPPING_ITEM,
      DROPPING_NO_ITEM;
   }
}
