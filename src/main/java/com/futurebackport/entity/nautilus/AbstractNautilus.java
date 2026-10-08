package com.futurebackport.entity.nautilus;

import com.futurebackport.item.NautilusArmorItem;
import com.futurebackport.menu.NautilusMenu;
import com.futurebackport.registry.ModEffects;
import com.futurebackport.registry.ModSounds;
import com.futurebackport.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNautilus extends TamableAnimal implements PlayerRideableJumping, HasCustomInventoryScreen, Saddleable, ContainerListener {
   public static final int SADDLE_SLOT = 0;
   public static final int ARMOR_SLOT = 1;
   private static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(AbstractNautilus.class, EntityDataSerializers.BOOLEAN);
   private static final int DASH_COOLDOWN_TICKS = 40;
   private int dashCooldown = 0;
   protected float playerJumpPendingScale;
   protected SimpleContainer inventory;
   int attackTargetCooldown;
   int chargeCooldown;

   protected AbstractNautilus(EntityType<? extends AbstractNautilus> type, Level level) {
      super(type, level);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.011F, 0.0F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
      this.setPathfindingMalus(PathType.WATER, 0.0F);
      this.inventory = new SimpleContainer(2);
      this.inventory.addListener(this);
   }

   public static Builder createAttributes() {
      return Animal.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 15.0)
         .add(Attributes.MOVEMENT_SPEED, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.3F)
         .add(Attributes.FOLLOW_RANGE, 16.0);
   }

   public static boolean checkNautilusSpawnRules(
      EntityType<? extends AbstractNautilus> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      int seaLevel = level.getSeaLevel();
      return pos.getY() >= seaLevel - 25
         && pos.getY() <= seaLevel - 5
         && level.getFluidState(pos.below()).is(FluidTags.WATER)
         && level.getBlockState(pos.above()).is(Blocks.WATER);
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DASH, false);
      builder.define(SADDLED, false);
   }

   public boolean isFood(ItemStack stack) {
      return !this.isTame() && !this.isBaby() ? stack.is(ModTags.NAUTILUS_TAMING_ITEMS) : stack.is(ModTags.NAUTILUS_FOOD);
   }

   protected void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
      if (stack.is(ModTags.NAUTILUS_BUCKET_FOOD)) {
         player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET)));
      } else {
         super.usePlayerItem(player, hand, stack);
      }
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      this.setPersistenceRequired();
      ItemStack stack = player.getItemInHand(hand);
      if (this.isBaby()) {
         return super.mobInteract(player, hand);
      } else if (this.isTame() && player.isSecondaryUseActive()) {
         this.openCustomInventoryScreen(player);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         if (!stack.isEmpty()) {
            if (!this.isTame() && this.isFood(stack)) {
               if (!this.level().isClientSide) {
                  this.usePlayerItem(player, hand, stack);
                  this.tryToTame(player);
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
               if (!this.level().isClientSide) {
                  this.usePlayerItem(player, hand, stack);
                  this.heal(2.0F);
                  this.playEatingSound();
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.isTame() && stack.getItem() instanceof NautilusArmorItem && this.inventory.getItem(1).isEmpty()) {
               if (!this.level().isClientSide) {
                  this.inventory.setItem(1, stack.consumeAndReturn(1, player));
                  this.playSound((SoundEvent)ModSounds.ARMOR_EQUIP_NAUTILUS.get(), 1.0F, 1.0F);
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.isTame() && stack.is(Items.SHEARS) && !this.inventory.getItem(1).isEmpty()) {
               if (!this.level().isClientSide) {
                  this.spawnAtLocation(this.inventory.removeItemNoUpdate(1));
                  this.containerChanged(this.inventory);
                  this.playSound((SoundEvent)ModSounds.ARMOR_UNEQUIP_NAUTILUS.get(), 1.0F, 1.0F);
                  stack.hurtAndBreak(1, player, getSlotForHand(hand));
               }

               return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            InteractionResult result = stack.interactLivingEntity(player, this, hand);
            if (result.consumesAction()) {
               return result;
            }
         }

         if (this.isTame() && !this.isFood(stack)) {
            if (!this.level().isClientSide) {
               player.startRiding(this);
               if (!this.isVehicle()) {
                  this.clearRestriction();
               }
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else {
            return super.mobInteract(player, hand);
         }
      }
   }

   private void tryToTame(Player player) {
      if (this.random.nextInt(3) == 0) {
         this.tame(player);
         this.navigation.stop();
         this.setTarget(null);
         this.level().broadcastEntityEvent(this, (byte)7);
      } else {
         this.level().broadcastEntityEvent(this, (byte)6);
      }

      this.playEatingSound();
   }

   protected abstract void playEatingSound();

   public boolean canMate(Animal other) {
      return other != this && other.getClass() == this.getClass() && this.isInLove() && other.isInLove();
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby() && this.isTame();
   }

   public void equipSaddle(ItemStack stack, @Nullable SoundSource source) {
      this.inventory.setItem(0, stack);
   }

   public boolean isSaddled() {
      return (Boolean)this.entityData.get(SADDLED);
   }

   public void containerChanged(Container container) {
      boolean wasSaddled = this.isSaddled();
      if (!this.level().isClientSide) {
         this.entityData.set(SADDLED, !this.inventory.getItem(0).isEmpty());
         this.setBodyArmorItem(this.inventory.getItem(1));
      }

      if (this.tickCount > 20 && !wasSaddled && this.isSaddled()) {
         this.playSound(
            this.isUnderWater() ? (SoundEvent)ModSounds.NAUTILUS_SADDLE_UNDERWATER_EQUIP.get() : (SoundEvent)ModSounds.NAUTILUS_SADDLE_EQUIP.get(), 0.5F, 1.0F
         );
      }
   }

   public SimpleContainer getInventory() {
      return this.inventory;
   }

   public void openCustomInventoryScreen(Player player) {
      if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(player)) && this.isTame() && player instanceof ServerPlayer serverPlayer) {
         NautilusMenu.open(serverPlayer, this);
      }
   }

   protected void dropEquipment() {
      super.dropEquipment();
      ItemStack saddle = this.inventory.getItem(0);
      if (!saddle.isEmpty()) {
         this.spawnAtLocation(saddle);
      }

      this.inventory.setItem(0, ItemStack.EMPTY);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      ItemStack saddle = this.inventory.getItem(0);
      if (!saddle.isEmpty()) {
         tag.put("SaddleItem", saddle.save(this.registryAccess()));
      }
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.inventory.setItem(1, this.getBodyArmorItem().copy());
      if (tag.contains("SaddleItem")) {
         this.inventory.setItem(0, ItemStack.parse(this.registryAccess(), tag.getCompound("SaddleItem")).orElse(ItemStack.EMPTY));
      }
   }

   public boolean isPushedByFluid() {
      return false;
   }

   protected PathNavigation createNavigation(Level level) {
      return new WaterBoundPathNavigation(this, level);
   }

   public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return 0.0F;
   }

   public boolean checkSpawnObstruction(LevelReader level) {
      return level.isUnobstructed(this);
   }

   protected boolean canAddPassenger(Entity passenger) {
      return !this.isVehicle();
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      return this.isSaddled() && this.getFirstPassenger() instanceof Player player ? player : null;
   }

   protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
      float strafe = controller.xxa;
      float forward = 0.0F;
      float up = 0.0F;
      if (controller.zza != 0.0F) {
         float forwardLook = Mth.cos(controller.getXRot() * (float) (Math.PI / 180.0));
         float upLook = -Mth.sin(controller.getXRot() * (float) (Math.PI / 180.0));
         if (controller.zza < 0.0F) {
            forwardLook *= -0.5F;
            upLook *= -0.5F;
         }

         up = upLook;
         forward = forwardLook;
      }

      return new Vec3(strafe, up, forward);
   }

   protected void tickRidden(Player controller, Vec3 riddenInput) {
      super.tickRidden(controller, riddenInput);
      Vec2 rotation = new Vec2(controller.getXRot() * 0.5F, controller.getYRot());
      float yRot = this.getYRot();
      yRot += Mth.wrapDegrees(rotation.y - yRot) * 0.5F;
      this.setRot(yRot, rotation.x);
      this.yRotO = this.yBodyRot = this.yHeadRot = yRot;
      if (this.isControlledByLocalInstance()) {
         if (this.playerJumpPendingScale > 0.0F && !this.isDashing()) {
            this.executeRidersJump(this.playerJumpPendingScale, controller);
         }

         this.playerJumpPendingScale = 0.0F;
      }
   }

   public void travel(Vec3 input) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(this.getSpeed(), input);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
      } else {
         super.travel(input);
      }
   }

   protected float getRiddenSpeed(Player controller) {
      return (this.isInWater() ? 0.0325F : 0.02F) * (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void onPlayerJump(int jumpAmount) {
      if (this.isSaddled() && this.dashCooldown <= 0) {
         this.playerJumpPendingScale = jumpAmount >= 90 ? 1.0F : 0.4F + 0.4F * jumpAmount / 90.0F;
      }
   }

   protected void executeRidersJump(float amount, Player controller) {
      this.addDeltaMovement(controller.getLookAngle().scale((this.isInWater() ? 1.2F : 0.5F) * amount * this.getAttributeValue(Attributes.MOVEMENT_SPEED)));
      this.dashCooldown = 40;
      this.setDashing(true);
      this.hasImpulse = true;
   }

   public void handleStartJump(int jumpPower) {
      SoundEvent sound = this.getDashSound();
      if (sound != null) {
         this.makeSound(sound);
      }

      this.setDashing(true);
   }

   public void handleStopJump() {
   }

   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   public boolean isDashing() {
      return (Boolean)this.entityData.get(DASH);
   }

   public void setDashing(boolean dashing) {
      this.entityData.set(DASH, dashing);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      if (!this.firstTick && DASH.equals(key)) {
         this.dashCooldown = this.dashCooldown == 0 ? 40 : this.dashCooldown;
      }

      super.onSyncedDataUpdated(key);
   }

   @Nullable
   protected abstract SoundEvent getDashSound();

   @Nullable
   protected abstract SoundEvent getDashReadySound();

   protected void playStepSound(BlockPos pos, BlockState state) {
   }

   protected void checkRestriction() {
      if (!this.isLeashed() && !this.isVehicle() && this.isTame()) {
         int radius = !this.isBaby() && !this.isSaddled() ? 32 : 16;
         if (!this.hasRestriction() || !this.getRestrictCenter().closerThan(this.blockPosition(), radius + 8) || radius != (int)this.getRestrictRadius()) {
            this.restrictTo(this.blockPosition(), radius);
         }
      }
   }

   protected void customServerAiStep() {
      this.checkRestriction();
      if (this.attackTargetCooldown > 0) {
         this.attackTargetCooldown--;
      }

      if (this.chargeCooldown > 0) {
         this.chargeCooldown--;
      }

      super.customServerAiStep();
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.getFirstPassenger() instanceof Player player) {
         boolean has = player.hasEffect(ModEffects.BREATH_OF_THE_NAUTILUS.holder());
         if (!has || this.level().getGameTime() % 40L == 0L) {
            player.addEffect(new MobEffectInstance(ModEffects.BREATH_OF_THE_NAUTILUS.holder(), 60, 0, true, true, true));
         }
      }

      if (this.isDashing() && this.dashCooldown < 35) {
         this.setDashing(false);
      }

      if (this.dashCooldown > 0) {
         this.dashCooldown--;
         if (this.dashCooldown == 0) {
            SoundEvent ready = this.getDashReadySound();
            if (ready != null) {
               this.makeSound(ready);
            }
         }
      }

      if (this.isInWater() && this.level().isClientSide) {
         this.spawnBubbles();
      }
   }

   private void spawnBubbles() {
      double speed = this.getDeltaMovement().length();
      if (this.random.nextFloat() < Mth.clamp(speed * 2.0, 0.15F, 1.0)) {
         Vec3 mouth = this.calculateViewVector(Mth.clamp(this.getXRot(), -10.0F, 10.0F), this.getYRot());
         double spread = this.random.nextDouble() * 0.8 * (1.0 + speed);
         this.level()
            .addParticle(
               ParticleTypes.BUBBLE,
               this.getX() - mouth.x * 1.1,
               this.getY() - mouth.y + 0.25,
               this.getZ() - mouth.z * 1.1,
               (this.random.nextFloat() - 0.5) * spread,
               (this.random.nextFloat() - 0.5) * spread,
               (this.random.nextFloat() - 0.5) * spread
            );
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      boolean hurt = super.hurt(source, amount);
      if (hurt
         && source.getEntity() instanceof LivingEntity attacker
         && !this.isTame()
         && !this.isBaby()
         && this.level() instanceof ServerLevel
         && attacker.isInWater()
         && !(attacker instanceof Player p && (p.isCreative() || p.isSpectator()))) {
         this.setTarget(attacker);
      }

      return hurt;
   }

   public boolean canBeAffected(MobEffectInstance effect) {
      return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
   }

   public boolean removeWhenFarAway(double distance) {
      return !this.isTame() && !this.hasCustomName() && !this.isLeashed();
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
      this.attackTargetCooldown = 2400 + this.random.nextInt(1201);
      return super.finalizeSpawn(level, difficulty, spawnType, groupData);
   }

   protected boolean isMobControlled() {
      return this.getFirstPassenger() instanceof Mob;
   }

   protected boolean isAggravated() {
      return this.getTarget() != null;
   }
}
