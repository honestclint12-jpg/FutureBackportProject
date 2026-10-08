package com.futurebackport.entity;

import com.futurebackport.FutureBackport;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

public final class SoundVariants {
   public static final String CLASSIC = "classic";
   public static final Supplier<AttachmentType<String>> ATTACHMENT = FarmAnimalVariant.ATTACHMENTS
      .register("sound_variant", () -> AttachmentType.builder(() -> "classic").serialize(Codec.STRING).build());
   private static final Map<EntityType<?>, SoundVariants.Mob> MOBS = Map.of(
      EntityType.COW,
      new SoundVariants.Mob("cow", List.of("classic", "moody"), List.of("ambient", "death", "hurt", "step")),
      EntityType.PIG,
      new SoundVariants.Mob("pig", List.of("classic", "mini", "big"), List.of("ambient", "death", "eat", "hurt")),
      EntityType.CHICKEN,
      new SoundVariants.Mob("chicken", List.of("classic", "picky"), List.of("ambient", "death", "hurt")),
      EntityType.WOLF,
      new SoundVariants.Mob(
         "wolf", List.of("classic", "angry", "big", "cute", "grumpy", "puglin", "sad"), List.of("ambient", "death", "growl", "hurt", "pant", "whine")
      ),
      EntityType.CAT,
      new SoundVariants.Mob(
         "cat", List.of("classic", "royal"), List.of("ambient", "beg_for_food", "death", "eat", "hiss", "hurt", "purr", "purreow", "stray_ambient")
      )
   );
   private static final Map<EntityType<?>, SoundVariants.Mob> BABIES = Map.of(
      EntityType.CAT,
      new SoundVariants.Mob("cat", List.of(), List.of("ambient", "beg_for_food", "death", "eat", "hiss", "hurt", "purr", "purreow", "stray_ambient")),
      EntityType.CHICKEN,
      new SoundVariants.Mob("chicken", List.of(), List.of("ambient", "death", "hurt", "step")),
      EntityType.HORSE,
      new SoundVariants.Mob("horse", List.of(), List.of("ambient", "angry", "breathe", "death", "eat", "hurt", "land", "step")),
      EntityType.PIG,
      new SoundVariants.Mob("pig", List.of(), List.of("ambient", "death", "eat", "hurt", "step")),
      EntityType.WOLF,
      new SoundVariants.Mob("wolf", List.of(), List.of("ambient", "death", "growl", "hurt", "pant", "step", "whine"))
   );

   private SoundVariants() {
   }

   public static List<String> variants(EntityType<?> type) {
      SoundVariants.Mob mob = MOBS.get(type);
      return mob == null ? List.of() : mob.variants();
   }

   public static void registerSounds(Consumer<String> register) {
      BABIES.values().forEach(mob -> mob.sounds().forEach(sound -> register.accept("entity.baby_" + mob.prefix() + "." + sound)));
      MOBS.values()
         .forEach(
            mob -> mob.variants()
               .stream()
               .skip(1L)
               .forEach(variant -> mob.sounds().forEach(sound -> register.accept("entity." + mob.prefix() + "_" + variant + "." + sound)))
         );
   }

   public static String get(Entity entity) {
      return (String)entity.getData(ATTACHMENT);
   }

   public static void set(Entity entity, String variant) {
      entity.setData(ATTACHMENT, variant);
   }

   public static SoundEvent swap(Entity entity, SoundEvent sound) {
      SoundVariants.Mob baby = entity instanceof LivingEntity living && living.isBaby() ? BABIES.get(entity.getType()) : null;
      if (baby != null) {
         SoundEvent babySound = replace(sound, baby.prefix(), "baby_" + baby.prefix(), baby.sounds());
         if (babySound != null) {
            return babySound;
         }
      }

      SoundVariants.Mob mob = MOBS.get(entity.getType());
      if (mob == null) {
         return sound;
      } else {
         String variant = get(entity);
         if (variant.equals("classic")) {
            return sound;
         } else {
            ResourceLocation id = sound.getLocation();
            String from = "entity." + mob.prefix() + ".";
            if (id.getNamespace().equals("minecraft") && id.getPath().startsWith(from)) {
               String name = id.getPath().substring(from.length());
               if (!mob.sounds().contains(name)) {
                  return sound;
               } else {
                  SoundEvent swapped = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(FutureBackport.id("entity." + mob.prefix() + "_" + variant + "." + name));
                  return swapped != null ? swapped : sound;
               }
            } else {
               return sound;
            }
         }
      }
   }

   private static SoundEvent replace(SoundEvent sound, String from, String to, List<String> sounds) {
      ResourceLocation id = sound.getLocation();
      String prefix = "entity." + from + ".";
      if (id.getNamespace().equals("minecraft") && id.getPath().startsWith(prefix)) {
         String name = id.getPath().substring(prefix.length());
         return sounds.contains(name) ? (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(FutureBackport.id("entity." + to + "." + name)) : null;
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
      SoundVariants.Mob mob = MOBS.get(event.getEntity().getType());
      if (mob != null) {
         set(event.getEntity(), mob.variants().get(event.getLevel().getRandom().nextInt(mob.variants().size())));
      }
   }

   private record Mob(String prefix, List<String> variants, List<String> sounds) {
   }
}
