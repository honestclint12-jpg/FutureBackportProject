package com.futurebackport.entity;

import com.futurebackport.FutureBackport;
import com.futurebackport.item.HarnessItem;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModSounds;
import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class HappyGhast extends Animal {
   public static final float BABY_SCALE = 0.2375F;
   private static final TagKey<Item> FOOD = TagKey.create(Registries.ITEM, FutureBackport.id("happy_ghast_food"));
   private static final TagKey<Item> TEMPT_ITEMS = TagKey.create(Registries.ITEM, FutureBackport.id("happy_ghast_tempt_items"));
   private static final EntityDataAccessor<Boolean> STAYS_STILL = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
   private int serverStillTimeout;
   private static final double TEMPT_RANGE = 16.0;

   public HappyGhast(EntityType<? extends HappyGhast> type, Level level) {
      super(type, level);
      this.moveControl = new HappyGhast.GhastMoveControl(this);
      this.lookControl = new HappyGhast.HappyGhastLookControl();
      this.setNoGravity(true);
   }

   public static Builder createAttributes() {
      return Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(Attributes.FLYING_SPEED, 0.05)
         .add(Attributes.MOVEMENT_SPEED, 0.05)
         .add(Attributes.FOLLOW_RANGE, 16.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(3, new FloatGoal(this) {
         public boolean canUse() {
            return !HappyGhast.this.isOnStillTimeout() && super.canUse();
         }
      });
      this.goalSelector
         .addGoal(
            4, new HappyGhast.FlyTowardsPlayerGoal(stack -> !this.isWearingHarness() && !this.isBaby() ? stack.is(TEMPT_ITEMS) : stack.is(FOOD), 7.0, false)
         );
      this.goalSelector.addGoal(5, new HappyGhast.FlyTowardsPlayerGoal(stack -> true, 3.0, true));
      this.goalSelector.addGoal(6, new HappyGhast.RandomFloatAroundGoal());
   }

   protected PathNavigation createNavigation(Level level) {
      FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
      navigation.setCanOpenDoors(false);
      navigation.setCanFloat(true);
      return navigation;
   }

   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      this.moveControl = (MoveControl)(this.isBaby() ? new FlyingMoveControl(this, 180, true) : new HappyGhast.GhastMoveControl(this));
      if (this.isBaby()) {
         this.setServerStillTimeout(0);
      }
   }

   public void setBaby(boolean baby) {
      super.setBaby(baby);
      this.moveControl = (MoveControl)(baby ? new FlyingMoveControl(this, 180, true) : new HappyGhast.GhastMoveControl(this));
   }

   public float getAgeScale() {
      return this.isBaby() ? 0.2375F : 1.0F;
   }

   public boolean isNoGravity() {
      return true;
   }

   protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
   }

   public boolean onClimbable() {
      return false;
   }

   public void travel(Vec3 input) {
      if (this.isControlledByLocalInstance()) {
         float speed = (float)this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0F / 3.0F;
         this.moveRelative(speed, input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
      }

      this.calculateEntityAnimation(false);
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      if (!level.isEmptyBlock(pos)) {
         return 0.0F;
      } else {
         return level.isEmptyBlock(pos.below()) && !level.isEmptyBlock(pos.below(2)) ? 10.0F : 5.0F;
      }
   }

   public boolean canDrownInFluidType(FluidType type) {
      return !this.isBaby() && super.canDrownInFluidType(type);
   }

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   public float getVoicePitch() {
      return 1.0F;
   }

   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   public int getAmbientSoundInterval() {
      return this.isVehicle() ? super.getAmbientSoundInterval() * 6 : super.getAmbientSoundInterval();
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? (SoundEvent)ModSounds.GHASTLING_AMBIENT.get() : (SoundEvent)ModSounds.HAPPY_GHAST_AMBIENT.get();
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isBaby() ? (SoundEvent)ModSounds.GHASTLING_HURT.get() : (SoundEvent)ModSounds.HAPPY_GHAST_HURT.get();
   }

   protected SoundEvent getDeathSound() {
      return this.isBaby() ? (SoundEvent)ModSounds.GHASTLING_DEATH.get() : (SoundEvent)ModSounds.HAPPY_GHAST_DEATH.get();
   }

   protected float getSoundVolume() {
      return this.isBaby() ? 1.0F : 4.0F;
   }

   public int getMaxSpawnClusterSize() {
      return 1;
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
      return (AgeableMob)((EntityType)ModEntities.HAPPY_GHAST.get()).create(level);
   }

   public boolean canFallInLove() {
      return false;
   }

   public boolean isFood(ItemStack stack) {
      return stack.is(FOOD);
   }

   public boolean isWearingHarness() {
      return this.getItemBySlot(EquipmentSlot.BODY).getItem() instanceof HarnessItem;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.isBaby()) {
         return super.mobInteract(player, hand);
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (stack.getItem() instanceof HarnessItem && !this.isWearingHarness()) {
            if (!this.level().isClientSide()) {
               this.setItemSlot(EquipmentSlot.BODY, stack.copyWithCount(1));
               stack.consume(1, player);
               this.level().playSound(null, this, (SoundEvent)ModSounds.HAPPY_GHAST_EQUIP.get(), this.getSoundSource(), 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
         } else if (stack.is(Items.SHEARS) && this.isWearingHarness() && !this.isVehicle()) {
            if (!this.level().isClientSide()) {
               this.spawnAtLocation(this.getItemBySlot(EquipmentSlot.BODY));
               this.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
               this.level().playSound(null, this, (SoundEvent)ModSounds.HAPPY_GHAST_UNEQUIP.get(), this.getSoundSource(), 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
         } else if (this.isWearingHarness() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
               player.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
         } else {
            return super.mobInteract(player, hand);
         }
      }
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.isWearingHarness()) {
         this.spawnAtLocation(this.getItemBySlot(EquipmentSlot.BODY));
      }
   }

   protected void addPassenger(Entity passenger) {
      if (!this.isVehicle()) {
         this.level()
            .playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.HARNESS_GOGGLES_DOWN.get(), this.getSoundSource(), 1.0F, 1.0F);
      }

      super.addPassenger(passenger);
      if (!this.level().isClientSide()) {
         if (!this.scanPlayerAboveGhast()) {
            this.setServerStillTimeout(0);
         } else if (this.serverStillTimeout > 10) {
            this.setServerStillTimeout(10);
         }
      }
   }

   protected void removePassenger(Entity passenger) {
      super.removePassenger(passenger);
      if (!this.level().isClientSide()) {
         this.setServerStillTimeout(10);
      }

      if (!this.isVehicle()) {
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)ModSounds.HARNESS_GOGGLES_UP.get(), this.getSoundSource(), 1.0F, 1.0F);
      }
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().size() < 4;
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return (LivingEntity)(this.isWearingHarness() && !this.isOnStillTimeout() && this.getFirstPassenger() instanceof Player player
         ? player
         : super.getControllingPassenger());
   }

   protected Vec3 getRiddenInput(Player rider, Vec3 selfInput) {
      float forward = 0.0F;
      float up = 0.0F;
      if (rider.zza != 0.0F) {
         float forwardLook = Mth.cos(rider.getXRot() * (float) (Math.PI / 180.0));
         float upLook = -Mth.sin(rider.getXRot() * (float) (Math.PI / 180.0));
         if (rider.zza < 0.0F) {
            forwardLook *= -0.5F;
            upLook *= -0.5F;
         }

         up = upLook;
         forward = forwardLook;
      }

      if (rider.jumping) {
         up += 0.5F;
      }

      return new Vec3(rider.xxa, up, forward).scale(3.9F * this.getAttributeValue(Attributes.FLYING_SPEED));
   }

   protected void tickRidden(Player rider, Vec3 input) {
      super.tickRidden(rider, input);
      float yRot = this.getYRot() + Mth.wrapDegrees(rider.getYRot() - this.getYRot()) * 0.08F;
      this.setRot(yRot, rider.getXRot() * 0.5F);
      this.yRotO = this.yBodyRot = this.yHeadRot = yRot;
   }

   protected float getRiddenSpeed(Player rider) {
      return (float)this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0F / 3.0F;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.serverStillTimeout > 0) {
            if (this.tickCount > 60) {
               this.serverStillTimeout--;
            }

            this.setServerStillTimeout(this.serverStillTimeout);
         }

         if (this.scanPlayerAboveGhast()) {
            this.setServerStillTimeout(10);
         }
      }
   }

   public void aiStep() {
      super.aiStep();
      if (this.level() instanceof ServerLevel serverLevel && this.isAlive() && this.deathTime == 0 && this.getHealth() < this.getMaxHealth()) {
         boolean fast = this.getY() >= 192.0 && this.getY() <= 196.0 || serverLevel.isRainingAt(this.blockPosition());
         if (this.tickCount % (fast ? 20 : 600) == 0) {
            this.heal(1.0F);
         }
      }
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(STAYS_STILL, false);
   }

   private void setServerStillTimeout(int timeout) {
      this.serverStillTimeout = timeout;
      this.entityData.set(STAYS_STILL, timeout > 0);
   }

   public boolean isOnStillTimeout() {
      return (Boolean)this.entityData.get(STAYS_STILL) || this.serverStillTimeout > 0;
   }

   private boolean scanPlayerAboveGhast() {
      AABB box = this.getBoundingBox();
      AABB above = new AABB(box.minX - 1.0, box.maxY - 1.0E-5F, box.minZ - 1.0, box.maxX + 1.0, box.maxY + box.getYsize() / 2.0, box.maxZ + 1.0);

      for (Player player : this.level().players()) {
         if (!player.isSpectator()) {
            Entity root = player.getRootVehicle();
            if (!(root instanceof HappyGhast) && above.contains(root.position())) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean canBeCollidedWith() {
      return !this.isBaby() && this.isAlive() && this.isOnStillTimeout();
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("still_timeout", this.serverStillTimeout);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setServerStillTimeout(tag.getInt("still_timeout"));
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this) {
         public void clientTick() {
            if (HappyGhast.this.isVehicle()) {
               HappyGhast.this.yHeadRot = HappyGhast.this.getYRot();
               HappyGhast.this.yBodyRot = HappyGhast.this.yHeadRot;
            }

            super.clientTick();
         }
      };
   }

   private class FlyTowardsPlayerGoal extends Goal {
      private final Predicate<ItemStack> wants;
      private final double stopDistance;
      private final boolean babiesOnly;
      @Nullable
      private Player target;

      FlyTowardsPlayerGoal(Predicate<ItemStack> wants, double stopDistance, boolean babiesOnly) {
         this.wants = wants;
         this.stopDistance = stopDistance;
         this.babiesOnly = babiesOnly;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         if (!HappyGhast.this.isOnStillTimeout() && !HappyGhast.this.isVehicle() && (!this.babiesOnly || HappyGhast.this.isBaby())) {
            double range = 16.0;
            this.target = HappyGhast.this.level()
               .getNearestPlayer(
                  HappyGhast.this.getX(),
                  HappyGhast.this.getY(),
                  HappyGhast.this.getZ(),
                  range,
                  e -> e instanceof Player p && !p.isSpectator() && (this.wants.test(p.getMainHandItem()) || this.wants.test(p.getOffhandItem()))
               );
            return this.target != null && HappyGhast.this.distanceTo(this.target) > this.stopDistance;
         } else {
            return false;
         }
      }

      public boolean canContinueToUse() {
         return this.target != null && this.target.isAlive() && HappyGhast.this.distanceTo(this.target) > this.stopDistance && this.canUse();
      }

      public void tick() {
         if (this.target != null) {
            HappyGhast.this.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            HappyGhast.this.getMoveControl()
               .setWantedPosition(this.target.getX(), this.target.getEyeY(), this.target.getZ(), HappyGhast.this.isBaby() ? 1.25 : 1.0);
         }
      }
   }

   private class GhastMoveControl extends MoveControl {
      private int floatDuration;

      GhastMoveControl(HappyGhast ghast) {
         super(ghast);
      }

      public void tick() {
         if (HappyGhast.this.isOnStillTimeout()) {
            this.operation = MoveControl.Operation.WAIT;
            HappyGhast.this.setDeltaMovement(HappyGhast.this.getDeltaMovement().multiply(0.0, 0.0, 0.0));
         }

         if (this.operation == MoveControl.Operation.MOVE_TO && this.floatDuration-- <= 0) {
            this.floatDuration = this.floatDuration + HappyGhast.this.getRandom().nextInt(5) + 2;
            Vec3 travel = new Vec3(this.wantedX - HappyGhast.this.getX(), this.wantedY - HappyGhast.this.getY(), this.wantedZ - HappyGhast.this.getZ());
            double length = travel.length();
            AABB box = HappyGhast.this.getBoundingBox();
            boolean clear = true;

            for (int i = 1; i < Mth.ceil(length) && clear; i++) {
               clear = HappyGhast.this.level().noCollision(HappyGhast.this, box.move(travel.scale(i / length)));
            }

            if (clear) {
               HappyGhast.this.setDeltaMovement(
                  HappyGhast.this.getDeltaMovement().add(travel.normalize().scale(HappyGhast.this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0 / 3.0))
               );
            } else {
               this.operation = MoveControl.Operation.WAIT;
            }
         }
      }
   }

   private class HappyGhastLookControl extends LookControl {
      HappyGhastLookControl() {
         super(HappyGhast.this);
      }

      public void tick() {
         if (HappyGhast.this.isOnStillTimeout()) {
            float snap = Mth.wrapDegrees(HappyGhast.this.getYRot());
            snap -= Math.round(snap / 90.0F) * 90.0F;
            HappyGhast.this.setYRot(HappyGhast.this.getYRot() - snap);
            HappyGhast.this.setYHeadRot(HappyGhast.this.getYRot());
         } else if (this.lookAtCooldown > 0) {
            this.lookAtCooldown--;
            HappyGhast.this.setYRot(
               -((float)Mth.atan2(this.wantedX - HappyGhast.this.getX(), this.wantedZ - HappyGhast.this.getZ())) * (180.0F / (float)Math.PI)
            );
            HappyGhast.this.yBodyRot = HappyGhast.this.getYRot();
            HappyGhast.this.yHeadRot = HappyGhast.this.yBodyRot;
         } else if (HappyGhast.this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {
            Vec3 movement = HappyGhast.this.getDeltaMovement();
            HappyGhast.this.setYRot(-((float)Mth.atan2(movement.x, movement.z)) * (180.0F / (float)Math.PI));
            HappyGhast.this.yBodyRot = HappyGhast.this.getYRot();
         }
      }
   }

   private class RandomFloatAroundGoal extends Goal {
      RandomFloatAroundGoal() {
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         if (!HappyGhast.this.isOnStillTimeout() && !HappyGhast.this.isVehicle()) {
            MoveControl control = HappyGhast.this.getMoveControl();
            if (!control.hasWanted()) {
               return true;
            } else {
               double dx = control.getWantedX() - HappyGhast.this.getX();
               double dy = control.getWantedY() - HappyGhast.this.getY();
               double dz = control.getWantedZ() - HappyGhast.this.getZ();
               double distance = dx * dx + dy * dy + dz * dz;
               return distance < 1.0 || distance > 3600.0;
            }
         } else {
            return false;
         }
      }

      public boolean canContinueToUse() {
         return false;
      }

      public void start() {
         RandomSource random = HappyGhast.this.getRandom();
         double x = HappyGhast.this.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
         double z = HappyGhast.this.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
         int ground = HappyGhast.this.level().getHeight(Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
         double y = Mth.clamp(HappyGhast.this.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F, ground + 1, ground + 16);
         HappyGhast.this.getMoveControl().setWantedPosition(x, y, z, 1.0);
      }
   }
}
