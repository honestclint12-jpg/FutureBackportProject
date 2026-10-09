package com.futurebackport.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import com.futurebackport.block.CreakingHeartBlock;
import com.futurebackport.block.CreakingHeartState;
import com.futurebackport.block.entity.CreakingHeartBlockEntity;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

public class Creaking extends Monster {
   public static final int ORANGE = 16545810;
   public static final int GRAY = 6250335;
   private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
   public final AnimationState attackAnimationState = new AnimationState();
   public final AnimationState invulnerabilityAnimationState = new AnimationState();
   public final AnimationState deathAnimationState = new AnimationState();
   private int attackAnimationRemainingTicks;
   private int invulnerabilityAnimationRemainingTicks;
   private boolean eyesGlowing;
   private int nextFlickerTime;
   private int playerStuckCounter;

   public Creaking(EntityType<? extends Creaking> type, Level level) {
      super(type, level);
      this.setMaxUpStep(1.0625F);
      this.lookControl = new Creaking.CreakingLookControl(this);
      this.moveControl = new Creaking.CreakingMoveControl(this);
      this.jumpControl = new Creaking.CreakingJumpControl(this);
      ((GroundPathNavigation)this.getNavigation()).setCanFloat(true);
      this.xpReward = 0;
   }

   /** Eye height as a fraction of the (age-scaled) height; 1.20.1 has no EntityType eyeHeight. */
   protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
      return dimensions.height * (2.3F / 2.7F);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 1.0)
         .add(Attributes.MOVEMENT_SPEED, 0.4F)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   public void setTransient(BlockPos pos) {
      this.setHomePos(pos);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
   }

   public boolean isHeartBound() {
      return this.getHomePos() != null;
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this) {
         public void clientTick() {
            if (Creaking.this.canMove()) {
               super.clientTick();
            }
         }
      };
   }

   protected Provider<Creaking> brainProvider() {
      return CreakingAi.brainProvider();
   }

   protected Brain<?> makeBrain(Dynamic<?> dynamic) {
      return CreakingAi.makeBrain(this, (Brain<Creaking>) this.brainProvider().makeBrain(dynamic));
   }

   @SuppressWarnings("unchecked")
   public Brain<Creaking> getBrain() {
      return (Brain<Creaking>) super.getBrain();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CAN_MOVE, true);
      this.entityData.define(IS_ACTIVE, false);
      this.entityData.define(IS_TEARING_DOWN, false);
      this.entityData.define(HOME_POS, Optional.empty());
   }

   public boolean canMove() {
      return (Boolean)this.entityData.get(CAN_MOVE);
   }

   public boolean doHurtTarget(Entity target) {
      if (!(target instanceof LivingEntity)) {
         return false;
      } else {
         this.attackAnimationRemainingTicks = 15;
         this.level().broadcastEntityEvent(this, (byte)4);
         return super.doHurtTarget(target);
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      BlockPos home = this.getHomePos();
      if (home == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || this.level().isClientSide()) {
         return super.hurt(source, amount);
      } else if (!this.isInvulnerableTo(source) && this.invulnerabilityAnimationRemainingTicks <= 0 && !this.isDeadOrDying()) {
         Player player = this.blameSourceForDamage(source);
         Entity direct = source.getDirectEntity();
         if (!(direct instanceof LivingEntity) && !(direct instanceof Projectile) && player == null) {
            return false;
         } else {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.level().broadcastEntityEvent(this, (byte)66);
            this.gameEvent(GameEvent.ENTITY_ROAR);
            if (this.level().getBlockEntity(home) instanceof CreakingHeartBlockEntity heart && heart.isProtector(this)) {
               if (player != null) {
                  heart.creakingHurt();
               }

               this.playHurtSound(source);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Player blameSourceForDamage(DamageSource source) {
      Entity attacker = source.getEntity();
      if (attacker instanceof LivingEntity living) {
         this.setLastHurtByMob(living);
      }

      Player player = attacker instanceof Player p ? p : (attacker instanceof TamableAnimal pet && pet.getOwner() instanceof Player owner ? owner : null);
      if (player != null) {
         this.setLastHurtByPlayer(player);
      }

      return player;
   }

   public boolean isPushable() {
      return super.isPushable() && this.canMove();
   }

   public void push(double x, double y, double z) {
      if (this.canMove()) {
         super.push(x, y, z);
      }
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("creakingBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      CreakingAi.updateActivity(this);
   }

   public void aiStep() {
      if (this.invulnerabilityAnimationRemainingTicks > 0) {
         this.invulnerabilityAnimationRemainingTicks--;
      }

      if (this.attackAnimationRemainingTicks > 0) {
         this.attackAnimationRemainingTicks--;
      }

      if (!this.level().isClientSide()) {
         boolean couldMove = (Boolean)this.entityData.get(CAN_MOVE);
         boolean canMoveNow = this.checkCanMove();
         if (canMoveNow != couldMove) {
            this.gameEvent(GameEvent.ENTITY_ROAR);
            if (canMoveNow) {
               this.playSound((SoundEvent)ModSounds.CREAKING_UNFREEZE.get(), this.getSoundVolume(), this.getVoicePitch());
            } else {
               this.getNavigation().stop();
               this.setXxa(0.0F);
               this.setZza(0.0F);
               this.setSpeed(0.0F);
               this.playSound((SoundEvent)ModSounds.CREAKING_FREEZE.get(), this.getSoundVolume(), this.getVoicePitch());
            }
         }

         this.entityData.set(CAN_MOVE, canMoveNow);
      }

      super.aiStep();
   }

   public void tick() {
      if (!this.level().isClientSide()) {
         BlockPos home = this.getHomePos();
         if (home != null && !(this.level().getBlockEntity(home) instanceof CreakingHeartBlockEntity heart && heart.isProtector(this))) {
            this.setHealth(0.0F);
         }
      }

      super.tick();
      if (this.level().isClientSide()) {
         this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
         this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
         this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
         this.checkEyeBlink();
      }
   }

   protected void tickDeath() {
      if (this.isHeartBound() && this.isTearingDown()) {
         this.deathTime++;
         if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved()) {
            this.tearDown();
         }
      } else {
         super.tickDeath();
      }
   }

   protected void updateWalkAnimation(float distance) {
      this.walkAnimation.update(Math.min(distance * 25.0F, 3.0F), 0.4F);
   }

   public void tearDown() {
      if (this.level() instanceof ServerLevel serverLevel) {
         AABB box = this.getBoundingBox();
         Vec3 center = box.getCenter();
         double xs = box.getXsize() * 0.3;
         double ys = box.getYsize() * 0.3;
         double zs = box.getZsize() * 0.3;
         serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, ((RotatedPillarBlock)ModBlocks.PALE_OAK_WOOD.get()).defaultBlockState()),
            center.x,
            center.y,
            center.z,
            100,
            xs,
            ys,
            zs,
            0.0
         );
         serverLevel.sendParticles(
            new BlockParticleOption(
               ParticleTypes.BLOCK,
               (BlockState)((CreakingHeartBlock)ModBlocks.CREAKING_HEART.get())
                  .defaultBlockState()
                  .setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE)
            ),
            center.x,
            center.y,
            center.z,
            10,
            xs,
            ys,
            zs,
            0.0
         );
      }

      this.playSound(this.getDeathSound(), this.getSoundVolume(), this.getVoicePitch());
      this.remove(RemovalReason.DISCARDED);
   }

   public void creakingDeathEffects(DamageSource source) {
      this.blameSourceForDamage(source);
      this.die(source);
      this.playSound((SoundEvent)ModSounds.CREAKING_TWITCH.get(), this.getSoundVolume(), this.getVoicePitch());
   }

   public void handleEntityEvent(byte id) {
      if (id == 66) {
         this.invulnerabilityAnimationRemainingTicks = 8;
         this.playHurtSound(this.damageSources().generic());
      } else if (id == 4) {
         this.attackAnimationRemainingTicks = 15;
         this.playSound((SoundEvent)ModSounds.CREAKING_ATTACK.get(), this.getSoundVolume(), this.getVoicePitch());
      } else {
         super.handleEntityEvent(id);
      }
   }

   public boolean fireImmune() {
      return this.isHeartBound() || super.fireImmune();
   }

   public boolean canChangeDimensions() {
      return !this.isHeartBound() && super.canChangeDimensions();
   }

   protected PathNavigation createNavigation(Level level) {
      return new Creaking.CreakingPathNavigation(this, level);
   }

   public boolean playerIsStuckInYou() {
      List<Player> players = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
      if (players.isEmpty()) {
         this.playerStuckCounter = 0;
         return false;
      } else {
         AABB box = this.getBoundingBox();

         for (Player player : players) {
            if (box.contains(player.getEyePosition())) {
               this.playerStuckCounter++;
               return this.playerStuckCounter > 4;
            }
         }

         this.playerStuckCounter = 0;
         return false;
      }
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains("home_pos", 10)) {
         this.setTransient(NbtUtils.readBlockPos(tag.getCompound("home_pos")));
      }
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      BlockPos home = this.getHomePos();
      if (home != null) {
         tag.put("home_pos", NbtUtils.writeBlockPos(home));
      }
   }

   public void setHomePos(BlockPos pos) {
      this.entityData.set(HOME_POS, Optional.of(pos));
   }

   @Nullable
   public BlockPos getHomePos() {
      return (BlockPos)((Optional)this.entityData.get(HOME_POS)).orElse(null);
   }

   public void setTearingDown() {
      this.entityData.set(IS_TEARING_DOWN, true);
   }

   public boolean isTearingDown() {
      return (Boolean)this.entityData.get(IS_TEARING_DOWN);
   }

   public boolean hasGlowingEyes() {
      return this.eyesGlowing;
   }

   private void checkEyeBlink() {
      if (this.deathTime > this.nextFlickerTime) {
         this.nextFlickerTime = this.deathTime
            + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
         this.eyesGlowing = !this.eyesGlowing;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isActive() ? null : (SoundEvent)ModSounds.CREAKING_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isHeartBound() ? (SoundEvent)ModSounds.CREAKING_SWAY.get() : super.getHurtSound(source);
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.CREAKING_DEATH.get();
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound((SoundEvent)ModSounds.CREAKING_STEP.get(), 0.15F, 1.0F);
   }

   @Nullable
   public LivingEntity getTarget() {
      return (LivingEntity)this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
   }

   public void knockback(double strength, double x, double z) {
      if (this.canMove()) {
         super.knockback(strength, x, z);
      }
   }

   private boolean checkCanMove() {
      List<Player> players = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
      boolean active = this.isActive();
      if (players.isEmpty()) {
         if (active) {
            this.deactivate();
         }

         return true;
      } else {
         boolean hasPotentialTarget = false;

         for (Player player : players) {
            if (this.canAttack(player) && !this.isAlliedTo(player)) {
               hasPotentialTarget = true;
               boolean disguised = player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN);
               if ((!active || !disguised)
                  && this.isLookingAtMe(player, 0.5, this.getEyeY(), this.getY() + 0.5 * this.getScale(), (this.getEyeY() + this.getY()) / 2.0)) {
                  if (active) {
                     return false;
                  }

                  if (player.distanceToSqr(this) < 144.0) {
                     this.activate(player);
                     return false;
                  }
               }
            }
         }

         if (!hasPotentialTarget && active) {
            this.deactivate();
         }

         return true;
      }
   }

   private boolean isLookingAtMe(Player player, double coneSize, double... gazeHeights) {
      Vec3 look = player.getViewVector(1.0F).normalize();

      for (double gazeHeight : gazeHeights) {
         Vec3 dir = new Vec3(this.getX() - player.getX(), gazeHeight - player.getEyeY(), this.getZ() - player.getZ()).normalize();
         if (look.dot(dir) > 1.0 - coneSize) {
            Vec3 from = player.getEyePosition();
            Vec3 to = new Vec3(this.getX(), gazeHeight, this.getZ());
            if (!(to.distanceTo(from) > 128.0) && this.level().clip(new ClipContext(from, to, Block.VISUAL, Fluid.NONE, player)).getType() == Type.MISS) {
               return true;
            }
         }
      }

      return false;
   }

   public void activate(Player player) {
      this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
      this.gameEvent(GameEvent.ENTITY_ROAR);
      this.playSound((SoundEvent)ModSounds.CREAKING_ACTIVATE.get(), this.getSoundVolume(), this.getVoicePitch());
      this.entityData.set(IS_ACTIVE, true);
   }

   public void deactivate() {
      this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      this.gameEvent(GameEvent.ENTITY_ROAR);
      this.playSound((SoundEvent)ModSounds.CREAKING_DEACTIVATE.get(), this.getSoundVolume(), this.getVoicePitch());
      this.entityData.set(IS_ACTIVE, false);
   }

   public boolean isActive() {
      return (Boolean)this.entityData.get(IS_ACTIVE);
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return 0.0F;
   }

   private class CreakingJumpControl extends JumpControl {
      CreakingJumpControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         } else {
            Creaking.this.setJumping(false);
         }
      }
   }

   private class CreakingLookControl extends LookControl {
      CreakingLookControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }
   }

   private class CreakingMoveControl extends MoveControl {
      CreakingMoveControl(Creaking creaking) {
         super(creaking);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }
   }

   private class CreakingPathNavigation extends GroundPathNavigation {
      CreakingPathNavigation(Creaking mob, Level level) {
         super(mob, level);
      }

      public void tick() {
         if (Creaking.this.canMove()) {
            super.tick();
         }
      }

      protected PathFinder createPathFinder(int maxVisitedNodes) {
         this.nodeEvaluator = Creaking.this.new HomeNodeEvaluator();
         this.nodeEvaluator.setCanPassDoors(true);
         return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
      }
   }

   private class HomeNodeEvaluator extends WalkNodeEvaluator {
      public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
         BlockPos home = Creaking.this.getHomePos();
         if (home == null) {
            return super.getBlockPathType(level, x, y, z);
         } else {
            double homeDistance = home.distSqr(new Vec3i(x, y, z));
            return homeDistance > 1024.0 && homeDistance >= home.distSqr(this.mob.blockPosition())
               ? BlockPathTypes.BLOCKED
               : super.getBlockPathType(level, x, y, z);
         }
      }
   }
}
