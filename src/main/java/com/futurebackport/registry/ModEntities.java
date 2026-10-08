package com.futurebackport.registry;

import com.futurebackport.entity.CamelHusk;
import com.futurebackport.entity.CopperGolem;
import com.futurebackport.entity.Creaking;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.entity.Parched;
import com.futurebackport.entity.nautilus.AbstractNautilus;
import com.futurebackport.entity.nautilus.Nautilus;
import com.futurebackport.entity.nautilus.ZombieNautilus;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "futurebackport");
   public static final DeferredHolder<EntityType<?>, EntityType<Creaking>> CREAKING = ENTITIES.register(
      "creaking", () -> Builder.of(Creaking::new, MobCategory.MONSTER).sized(0.9F, 2.7F).eyeHeight(2.3F).clientTrackingRange(8).build("creaking")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<HappyGhast>> HAPPY_GHAST = ENTITIES.register(
      "happy_ghast",
      () -> Builder.of(HappyGhast::new, MobCategory.CREATURE)
         .sized(4.0F, 4.0F)
         .eyeHeight(2.6F)
         .passengerAttachments(new Vec3[]{new Vec3(0.0, 4.0, 1.7), new Vec3(-1.7, 4.0, 0.0), new Vec3(0.0, 4.0, -1.7), new Vec3(1.7, 4.0, 0.0)})
         .ridingOffset(0.5F)
         .clientTrackingRange(10)
         .build("happy_ghast")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<Parched>> PARCHED = ENTITIES.register(
      "parched",
      () -> Builder.of(Parched::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8).build("parched")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<CamelHusk>> CAMEL_HUSK = ENTITIES.register(
      "camel_husk", () -> Builder.of(CamelHusk::new, MobCategory.MONSTER).sized(1.7F, 2.375F).eyeHeight(2.275F).clientTrackingRange(10).build("camel_husk")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<CopperGolem>> COPPER_GOLEM = ENTITIES.register(
      "copper_golem", () -> Builder.of(CopperGolem::new, MobCategory.MISC).sized(0.49F, 0.98F).eyeHeight(0.8125F).clientTrackingRange(10).build("copper_golem")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<Nautilus>> NAUTILUS = ENTITIES.register(
      "nautilus",
      () -> Builder.of(Nautilus::new, MobCategory.WATER_CREATURE)
         .sized(0.875F, 0.95F)
         .passengerAttachments(new float[]{1.1375F})
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
         .build("nautilus")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<ZombieNautilus>> ZOMBIE_NAUTILUS = ENTITIES.register(
      "zombie_nautilus",
      () -> Builder.of(ZombieNautilus::new, MobCategory.MONSTER)
         .sized(0.875F, 0.95F)
         .passengerAttachments(new float[]{1.1375F})
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
         .build("zombie_nautilus")
   );

   public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
      event.register(
         (EntityType)NAUTILUS.get(),
         SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         AbstractNautilus::checkNautilusSpawnRules,
         Operation.REPLACE
      );
      event.register(
         (EntityType)ZOMBIE_NAUTILUS.get(),
         SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         AbstractNautilus::checkNautilusSpawnRules,
         Operation.REPLACE
      );
      event.register(PARCHED.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Parched::checkSpawnRules, Operation.REPLACE);
      event.register(
         CAMEL_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, CamelHusk::checkSpawnRules, Operation.REPLACE
      );
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)PARCHED.get(), Parched.createAttributes().build());
      event.put((EntityType)CREAKING.get(), Creaking.createAttributes().build());
      event.put((EntityType)CAMEL_HUSK.get(), Camel.createAttributes().build());
      event.put((EntityType)COPPER_GOLEM.get(), CopperGolem.createAttributes().build());
      event.put((EntityType)NAUTILUS.get(), AbstractNautilus.createAttributes().build());
      event.put((EntityType)ZOMBIE_NAUTILUS.get(), ZombieNautilus.createAttributes().build());
      event.put((EntityType)HAPPY_GHAST.get(), HappyGhast.createAttributes().build());
   }
}
