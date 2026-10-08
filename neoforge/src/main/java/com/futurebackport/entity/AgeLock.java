package com.futurebackport.entity;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;

public final class AgeLock {
   private static final TagKey<EntityType<?>> CANNOT_BE_AGE_LOCKED = TagKey.create(Registries.ENTITY_TYPE, FutureBackport.id("cannot_be_age_locked"));
   private static final int PARTICLE_TICKS = 40;
   public static final Supplier<AttachmentType<Boolean>> LOCKED = FarmAnimalVariant.ATTACHMENTS
      .register("age_locked", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
   public static final Supplier<AttachmentType<Integer>> PARTICLE_TIMER = FarmAnimalVariant.ATTACHMENTS
      .register("age_lock_particles", () -> AttachmentType.builder(() -> 0).build());

   @SubscribeEvent
   public void onInteract(EntityInteract event) {
      if (event.getTarget() instanceof AgeableMob mob && event.getItemStack().is(((FlowerBlock)ModBlocks.GOLDEN_DANDELION.get()).asItem())) {
         if (mob.isBaby() && (Integer)mob.getData(PARTICLE_TIMER) <= 0 && !mob.getType().is(CANNOT_BE_AGE_LOCKED)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            if (!event.getLevel().isClientSide()) {
               boolean locked = !(Boolean)mob.getData(LOCKED);
               mob.setData(LOCKED, locked);
               mob.setAge(-24000);
               mob.setData(PARTICLE_TIMER, 40);
               event.getItemStack().consume(1, event.getEntity());
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
         }
      }
   }

   @SubscribeEvent
   public void onTick(Post event) {
      if (event.getEntity() instanceof AgeableMob mob && mob.level() instanceof ServerLevel level) {
         boolean locked = (Boolean)mob.getData(LOCKED);
         if (locked && mob.getAge() > -24000) {
            mob.setAge(-24000);
         }

         int timer = (Integer)mob.getData(PARTICLE_TIMER);
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

            mob.setData(PARTICLE_TIMER, timer - 1);
         }
      }
   }
}
