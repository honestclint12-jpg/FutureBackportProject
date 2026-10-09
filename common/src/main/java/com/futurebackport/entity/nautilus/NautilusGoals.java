package com.futurebackport.entity.nautilus;

import com.futurebackport.registry.ModTags;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class NautilusGoals {
   private NautilusGoals() {
   }

   static TargetingConditions attackTargeting(Mob mob) {
      return TargetingConditions.forCombat()
         .selector(
            target -> (mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || !(target instanceof ArmorStand))
               && mob.level().getWorldBorder().isWithinBounds(target.getBoundingBox())
         );
   }

   public static class Charge extends Goal {
      private static final int TIME_BETWEEN_ATTACKS = 80;
      private static final double MAX_CHARGE_DISTANCE = 12.0;
      private static final double MAX_TARGET_DETECTION_DISTANCE = 11.0;
      private static final float KNOCKBACK_FORCE = 2.0F;
      private final AbstractNautilus nautilus;
      private final float speed;
      private final Supplier<SoundEvent> sound;
      private Vec3 velocity = Vec3.ZERO;
      private Vec3 start = Vec3.ZERO;

      public Charge(AbstractNautilus nautilus, float speed, Supplier<SoundEvent> sound) {
         this.nautilus = nautilus;
         this.speed = speed;
         this.sound = sound;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean canUse() {
         LivingEntity target = this.nautilus.getTarget();
         return target != null
            && target.isAlive()
            && this.nautilus.chargeCooldown <= 0
            && !this.nautilus.isTame()
            && !this.nautilus.isInLove()
            && this.nautilus.distanceToSqr(target) < 121.0
            && this.nautilus.hasLineOfSight(target);
      }

      public boolean canContinueToUse() {
         LivingEntity target = this.nautilus.getTarget();
         return target != null
            && target.isAlive()
            && !this.nautilus.isTame()
            && this.nautilus.position().distanceToSqr(this.start) < 144.0
            && this.nautilus.distanceToSqr(target) < 121.0
            && this.nautilus.hasLineOfSight(target);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      public void start() {
         this.start = this.nautilus.position();
         this.velocity = this.nautilus.getTarget().position().subtract(this.nautilus.position()).normalize().scale(this.speed);
         this.nautilus.playSound(this.sound.get());
      }

      public void tick() {
         LivingEntity target = this.nautilus.getTarget();
         if (target != null) {
            this.nautilus.lookAt(target, 360.0F, 360.0F);
            this.nautilus.setDeltaMovement(this.velocity);
            if (this.nautilus.level() instanceof ServerLevel level) {
               TargetingConditions var14 = NautilusGoals.attackTargeting(this.nautilus);
               List<LivingEntity> hit = level.getEntitiesOfClass(
                  LivingEntity.class, this.nautilus.getBoundingBox(), e -> e != this.nautilus && var14.test(this.nautilus, e)
               );
               if (!hit.isEmpty() && !this.nautilus.hasPassenger((Entity)hit.get(0))) {
                  LivingEntity victim = hit.get(0);
                  DamageSource source = this.nautilus.damageSources().mobAttack(this.nautilus);
                  float damage = (float)this.nautilus.getAttributeValue(Attributes.ATTACK_DAMAGE);
                  if (victim.hurt(source, damage)) {
                     EnchantmentHelper.doPostHurtEffects(victim, this.nautilus);
                     EnchantmentHelper.doPostDamageEffects(this.nautilus, victim);
                  }

                  int speedLevel = this.nautilus.hasEffect(MobEffects.MOVEMENT_SPEED)
                     ? this.nautilus.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() + 1
                     : 0;
                  int slowLevel = this.nautilus.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                     ? this.nautilus.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() + 1
                     : 0;
                  float speedFactor = Mth.clamp(this.speed * (float)this.nautilus.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.2F, 2.0F)
                     + 0.25F * (speedLevel - slowLevel);
                  double strength = speedFactor * 2.0F * (1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                  Vec3 push = this.velocity.normalize();
                  victim.push(push.x * strength * 0.5, 0.1 * strength, push.z * strength * 0.5);
                  victim.hurtMarked = true;
                  this.stop();
               }
            }
         }
      }

      public void stop() {
         this.nautilus.chargeCooldown = 80;
         this.nautilus.setTarget(null);
      }
   }

   public static class HuntHostiles extends Goal {
      private final AbstractNautilus nautilus;

      public HuntHostiles(AbstractNautilus nautilus) {
         this.nautilus = nautilus;
         this.setFlags(EnumSet.of(Flag.TARGET));
      }

      public boolean canUse() {
         if (this.nautilus.isTame() || this.nautilus.isBaby() || this.nautilus.isInLove() || !this.nautilus.isInWater() || this.nautilus.getTarget() != null) {
            return false;
         } else if (this.nautilus.attackTargetCooldown > 0) {
            return false;
         } else {
            this.nautilus.attackTargetCooldown = 2400 + this.nautilus.getRandom().nextInt(1201);
            if (this.nautilus.getRandom().nextFloat() < 0.5F) {
               return false;
            } else {
               LivingEntity hostile = this.findHostile();
               if (hostile == null) {
                  return false;
               } else {
                  this.nautilus.setTarget(hostile);
                  return false;
               }
            }
         }
      }

      @Nullable
      private LivingEntity findHostile() {
         return this.nautilus
            .level()
            .getNearestEntity(
               this.nautilus
                  .level()
                  .getEntitiesOfClass(
                     LivingEntity.class,
                     this.nautilus.getBoundingBox().inflate(16.0),
                     e -> e.isInWater() && e.getType().is(ModTags.NAUTILUS_HOSTILES) && this.nautilus.hasLineOfSight(e)
                  ),
               TargetingConditions.forCombat(),
               this.nautilus,
               this.nautilus.getX(),
               this.nautilus.getY(),
               this.nautilus.getZ()
            );
      }
   }
}
