package com.futurebackport.item;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModSounds;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class SpearItem extends Item {
   public static final ResourceKey<DamageType> SPEAR_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, FutureBackport.id("spear"));
   static final float MIN_REACH = 2.0F;
   static final float MAX_REACH = 4.5F;
   static final float MIN_CREATIVE_REACH = 2.0F;
   static final float MAX_CREATIVE_REACH = 6.5F;
   static final float HITBOX_MARGIN = 0.125F;
   static final float MOB_FACTOR = 0.5F;
   static final int CONTACT_COOLDOWN_TICKS = 10;
   private final boolean wooden;
   private final int delayTicks;
   private final float damageMultiplier;
   private final SpearItem.Condition dismount;
   private final SpearItem.Condition knockback;
   private final SpearItem.Condition damage;
   private final Tier tier;
   private static final Map<LivingEntity, SpearItem.Charge> CHARGES = new WeakHashMap<>();

   public SpearItem(
      Tier tier,
      float attackDuration,
      float damageMultiplier,
      float delay,
      float dismountTime,
      float dismountThreshold,
      float knockbackTime,
      float knockbackThreshold,
      float damageTime,
      float damageThreshold,
      Properties properties
   ) {
      super(
         properties.durability(tier.getUses())
            .attributes(
               ItemAttributeModifiers.builder()
                  .add(
                     Attributes.ATTACK_DAMAGE,
                     new AttributeModifier(BASE_ATTACK_DAMAGE_ID, tier.getAttackDamageBonus(), Operation.ADD_VALUE),
                     EquipmentSlotGroup.MAINHAND
                  )
                  .add(
                     Attributes.ATTACK_SPEED,
                     new AttributeModifier(BASE_ATTACK_SPEED_ID, 1.0F / attackDuration - 4.0, Operation.ADD_VALUE),
                     EquipmentSlotGroup.MAINHAND
                  )
                  .build()
            )
      );
      this.tier = tier;
      this.wooden = tier == Tiers.WOOD;
      this.damageMultiplier = damageMultiplier;
      this.delayTicks = (int)(delay * 20.0F);
      this.dismount = new SpearItem.Condition((int)(dismountTime * 20.0F), dismountThreshold, 0.0F);
      this.knockback = new SpearItem.Condition((int)(knockbackTime * 20.0F), knockbackThreshold, 0.0F);
      this.damage = new SpearItem.Condition((int)(damageTime * 20.0F), 0.0F, damageThreshold);
   }

   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
      return this.tier.getRepairIngredient().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      return true;
   }

   public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
   }

   public SoundEvent attackSound() {
      return (SoundEvent)(this.wooden ? ModSounds.SPEAR_WOOD_ATTACK : ModSounds.SPEAR_ATTACK).get();
   }

   public SoundEvent hitSound() {
      return (SoundEvent)(this.wooden ? ModSounds.SPEAR_WOOD_HIT : ModSounds.SPEAR_HIT).get();
   }

   public SoundEvent useSound() {
      return (SoundEvent)(this.wooden ? ModSounds.SPEAR_WOOD_USE : ModSounds.SPEAR_USE).get();
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.NONE;
   }

   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      level.playSound(player, player.getX(), player.getY(), player.getZ(), this.useSound(), player.getSoundSource(), 1.0F, 1.0F);
      if (!level.isClientSide) {
         CHARGES.put(player, new SpearItem.Charge());
      }

      return InteractionResultHolder.consume(player.getItemInHand(hand));
   }

   static Vec3 motion(Entity entity) {
      if (!(entity instanceof Player) && entity.isPassenger()) {
         entity = entity.getRootVehicle();
      }

      if (entity instanceof Player && entity.isPassenger()) {
         entity = entity.getRootVehicle();
      }

      return new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo).scale(20.0);
   }

   public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
      if (level instanceof ServerLevel serverLevel) {
         SpearItem.Charge charge = CHARGES.computeIfAbsent(user, u -> new SpearItem.Charge());
         int ticksUsed = this.getUseDuration(stack, user) - ticksRemaining - this.delayTicks;
         if (ticksUsed >= 0) {
            Vec3 look = user.getLookAngle();
            double attackerSpeed = look.dot(motion(user));
            float actionFactor = user instanceof Player ? 1.0F : 0.2F;
            double baseDamage = user.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
            EquipmentSlot slot = user.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            boolean affected = false;
            long now = level.getGameTime();

            for (Entity target : hitEntitiesAlong(user)) {
               if (target instanceof EnderDragonPart part) {
                  target = part.parentMob;
               }

               if (!charge.stabbed.containsKey(target) || now - charge.stabbed.getLong(target) >= 10L) {
                  charge.stabbed.put(target, now);
                  double relativeSpeed = Math.max(0.0, attackerSpeed - look.dot(motion(target)));
                  boolean dealsDismount = this.dismount.test(ticksUsed, attackerSpeed, relativeSpeed, actionFactor);
                  boolean dealsKnockback = this.knockback.test(ticksUsed, attackerSpeed, relativeSpeed, actionFactor);
                  boolean dealsDamage = this.damage.test(ticksUsed, attackerSpeed, relativeSpeed, actionFactor);
                  if (dealsDismount || dealsKnockback || dealsDamage) {
                     float damageDealt = (float)baseDamage + (float)Math.floor(relativeSpeed * this.damageMultiplier);
                     affected |= stabAttack(serverLevel, user, slot, target, damageDealt, dealsDamage, dealsKnockback, dealsDismount, true);
                  }
               }
            }

            if (affected) {
               level.playSound(null, user.getX(), user.getY(), user.getZ(), this.hitSound(), user.getSoundSource(), 1.0F, 1.0F);
            }
         }
      }
   }

   public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
      CHARGES.remove(user);
   }

   public void jab(ServerLevel level, Player player) {
      float attackDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      boolean hitSomething = false;

      for (Entity target : hitEntitiesAlong(player)) {
         hitSomething |= stabAttack(level, player, EquipmentSlot.MAINHAND, target, attackDamage, true, true, false, false);
      }

      player.resetAttackStrengthTicker();
      Lunge.afterJab(level, player);
      if (hitSomething) {
         level.playSound(null, player.getX(), player.getY(), player.getZ(), this.hitSound(), player.getSoundSource(), 1.0F, 1.0F);
      }

      level.playSound(player, player.getX(), player.getY(), player.getZ(), this.attackSound(), player.getSoundSource(), 1.0F, 1.0F);
      player.swing(InteractionHand.MAIN_HAND, true);
   }

   static List<Entity> hitEntitiesAlong(LivingEntity attacker) {
      boolean creative = attacker instanceof Player p && p.isCreative();
      float minRange = attacker instanceof Player ? (creative ? 2.0F : 2.0F) : 1.0F;
      float maxRange = attacker instanceof Player ? (creative ? 6.5F : 4.5F) : 2.25F;
      Vec3 look = attacker.getLookAngle();
      Vec3 eye = attacker.getEyePosition();
      Vec3 from = eye.add(look.scale(minRange));
      Vec3 to = eye.add(look.scale(maxRange + Math.max(0.0, motion(attacker).scale(0.05).dot(look))));
      Level level = attacker.level();
      HitResult block = level.clip(new ClipContext(eye, to, Block.COLLIDER, Fluid.NONE, attacker));
      if (block.getType() != Type.MISS) {
         to = block.getLocation();
         if (eye.distanceToSqr(to) < eye.distanceToSqr(from)) {
            return List.of();
         }
      }

      AABB search = AABB.ofSize(from, 0.125, 0.125, 0.125).expandTowards(to.subtract(from)).inflate(1.0);
      List<Entity> hits = new ArrayList<>();

      for (Entity entity : level.getEntities(attacker, search, e -> canHitEntity(attacker, e))) {
         AABB box = entity.getBoundingBox().inflate(0.125);
         if (box.contains(from) || box.clip(from, to).isPresent()) {
            hits.add(entity);
         }
      }

      return hits;
   }

   static boolean canHitEntity(Entity attacker, Entity target) {
      if (!target.isAlive() || target.isSpectator()) {
         return false;
      } else if (target instanceof Interaction) {
         return true;
      } else if (!target.canBeHitByProjectile()) {
         return false;
      } else {
         return target instanceof Player targetPlayer && attacker instanceof Player player && !player.canHarmPlayer(targetPlayer)
            ? false
            : !attacker.isPassengerOfSameVehicle(target);
      }
   }

   static boolean stabAttack(
      ServerLevel level,
      LivingEntity attacker,
      EquipmentSlot slot,
      Entity target,
      float baseDamage,
      boolean dealsDamage,
      boolean dealsKnockback,
      boolean dismounts,
      boolean charging
   ) {
      ItemStack weapon = attacker.getItemBySlot(slot);
      DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SPEAR_DAMAGE), attacker);
      float total = EnchantmentHelper.modifyDamage(level, weapon, target, source, baseDamage);
      if (attacker instanceof Player player) {
         if (player.isSpectator() || !target.isAttackable() || target.skipAttackInteraction(player)) {
            return false;
         }

         float magicBoost = total - baseDamage;
         if (!charging) {
            float scale = player.getAttackStrengthScale(0.5F);
            magicBoost *= scale;
            baseDamage *= 0.2F + scale * scale * 0.8F;
         }

         total = baseDamage + magicBoost;
      }

      float oldHealth = target instanceof LivingEntity living ? living.getHealth() : 0.0F;
      boolean hurt = dealsDamage && target.hurt(source, total);
      if (dealsKnockback) {
         Vec3 old = target.getDeltaMovement();
         float strength = 0.4F + (float)attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
         if (target instanceof LivingEntity livingx) {
            livingx.knockback(strength, Math.sin(attacker.getYRot() * (Math.PI / 180.0)), -Math.cos(attacker.getYRot() * (Math.PI / 180.0)));
         } else {
            target.push(
               -Math.sin(attacker.getYRot() * (Math.PI / 180.0)) * strength * 0.5, 0.1, Math.cos(attacker.getYRot() * (Math.PI / 180.0)) * strength * 0.5
            );
         }

         if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            target.hurtMarked = false;
            target.setDeltaMovement(old);
         }
      }

      boolean dismounted = false;
      if (dismounts && target.isPassenger()) {
         dismounted = true;
         target.stopRiding();
      }

      if (!hurt && !dealsKnockback && !dismounted) {
         return false;
      } else {
         attacker.setLastHurtMob(target);
         if (target instanceof LivingEntity livingx) {
            if (weapon.getItem().hurtEnemy(weapon, livingx, attacker)) {
               weapon.getItem().postHurtEnemy(weapon, livingx, attacker);
            }

            if (attacker instanceof Player player && hurt) {
               player.awardStat(Stats.DAMAGE_DEALT, Math.round((oldHealth - livingx.getHealth()) * 10.0F));
            }
         }

         if (hurt) {
            EnchantmentHelper.doPostAttackEffects(level, target, source);
         }

         if (attacker instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
         }

         return true;
      }
   }

   private static final class Charge {
      final Object2LongOpenHashMap<Entity> stabbed = new Object2LongOpenHashMap();
   }

   public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
      boolean test(int ticksUsed, double attackerSpeed, double relativeSpeed, double entityFactor) {
         return ticksUsed <= this.maxDurationTicks && attackerSpeed >= this.minSpeed * entityFactor && relativeSpeed >= this.minRelativeSpeed * entityFactor;
      }
   }
}
