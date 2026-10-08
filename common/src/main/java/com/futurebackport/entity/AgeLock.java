package com.futurebackport.entity;

import com.futurebackport.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.futurebackport.platform.attachment.DataAttachment;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.FlowerBlock;

public final class AgeLock {
   private AgeLock() {
   }

   private static final TagKey<EntityType<?>> CANNOT_BE_AGE_LOCKED = TagKey.create(Registries.ENTITY_TYPE, FutureBackport.id("cannot_be_age_locked"));
   private static final int PARTICLE_TICKS = 40;
   public static final DataAttachment<Boolean> LOCKED = Services.ATTACHMENTS.register("futurebackport", "age_locked", () -> false, Codec.BOOL, false);
   /** Not saved: only drives the particle burst after toggling. */
   public static final DataAttachment<Integer> PARTICLE_TIMER = Services.ATTACHMENTS.register("futurebackport", "age_lock_particles", () -> 0, null, false);

   /**
    * A player right-clicked an entity. Returns the interaction result if the golden dandelion toggled the age lock,
    * or {@code null} to let the interaction continue normally.
    */
   @Nullable
   public static InteractionResult onInteract(Player player, Level level, Entity target, ItemStack stack) {
      if (target instanceof AgeableMob mob && stack.is(((FlowerBlock)ModBlocks.GOLDEN_DANDELION.get()).asItem())) {
         if (mob.isBaby() && PARTICLE_TIMER.get(mob) <= 0 && !mob.getType().is(CANNOT_BE_AGE_LOCKED)) {
            if (!level.isClientSide()) {
               boolean locked = !LOCKED.get(mob);
               LOCKED.set(mob, locked);
               mob.setAge(-24000);
               PARTICLE_TIMER.set(mob, 40);
               stack.consume(1, player);
               if (locked) {
                  mob.setPersistenceRequired();
               }

               mob.level()
                  .playSound(
                     null,
                     mob.blockPosition(),
                     locked ? (SoundEvent)ModSounds.GOLDEN_DANDELION_USE.get() : (SoundEvent)ModSounds.GOLDEN_DANDELION_UNUSE.get(),
                     SoundSource.PLAYERS,
                     1.0F,
                     1.0F
                  );
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
         }
      }

      return null;
   }

   /** Every entity tick: keep locked babies young and run the toggle particles. */
   public static void onTick(Entity entity) {
      if (entity instanceof AgeableMob mob && mob.level() instanceof ServerLevel level) {
         boolean locked = LOCKED.get(mob);
         if (locked && mob.getAge() > -24000) {
            mob.setAge(-24000);
         }

         int timer = PARTICLE_TIMER.get(mob);
         if (timer > 0) {
            if (timer % 2 == 0) {
               double y = mob.getY() + mob.getRandom().nextDouble() * 0.2 + mob.getBbHeight() + (locked ? 0.2 : 0.0);
               level.sendParticles(
                  locked ? (SimpleParticleType)ModParticles.PAUSE_MOB_GROWTH.get() : (SimpleParticleType)ModParticles.RESET_MOB_GROWTH.get(),
                  mob.getRandomX(1.0),
                  y,
                  mob.getRandomZ(1.0),
                  1,
                  0.0,
                  0.0,
                  0.0,
                  0.0
               );
            }

            PARTICLE_TIMER.set(mob, timer - 1);
         }
      }
   }
}
