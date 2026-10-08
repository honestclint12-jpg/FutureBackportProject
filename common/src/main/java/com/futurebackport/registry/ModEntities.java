package com.futurebackport.registry;

import com.futurebackport.platform.entity.SpawnPlacementRegistrar;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;

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

public class ModEntities {
   public static final RegistrationProvider<EntityType<?>> ENTITIES = RegistrationProvider.create(Registries.ENTITY_TYPE, "futurebackport");
   public static final RegistryEntry<EntityType<?>, EntityType<Creaking>> CREAKING = ENTITIES.register(
      "creaking", () -> Builder.of(Creaking::new, MobCategory.MONSTER).sized(0.9F, 2.7F).eyeHeight(2.3F).clientTrackingRange(8).build("creaking")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<HappyGhast>> HAPPY_GHAST = ENTITIES.register(
      "happy_ghast",
      () -> Builder.of(HappyGhast::new, MobCategory.CREATURE)
         .sized(4.0F, 4.0F)
         .eyeHeight(2.6F)
         .passengerAttachments(new Vec3[]{new Vec3(0.0, 4.0, 1.7), new Vec3(-1.7, 4.0, 0.0), new Vec3(0.0, 4.0, -1.7), new Vec3(1.7, 4.0, 0.0)})
         .ridingOffset(0.5F)
         .clientTrackingRange(10)
         .build("happy_ghast")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<Parched>> PARCHED = ENTITIES.register(
      "parched",
      () -> Builder.of(Parched::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8).build("parched")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<CamelHusk>> CAMEL_HUSK = ENTITIES.register(
      "camel_husk", () -> Builder.of(CamelHusk::new, MobCategory.MONSTER).sized(1.7F, 2.375F).eyeHeight(2.275F).clientTrackingRange(10).build("camel_husk")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<CopperGolem>> COPPER_GOLEM = ENTITIES.register(
      "copper_golem", () -> Builder.of(CopperGolem::new, MobCategory.MISC).sized(0.49F, 0.98F).eyeHeight(0.8125F).clientTrackingRange(10).build("copper_golem")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<Nautilus>> NAUTILUS = ENTITIES.register(
      "nautilus",
      () -> Builder.of(Nautilus::new, MobCategory.WATER_CREATURE)
         .sized(0.875F, 0.95F)
         .passengerAttachments(new float[]{1.1375F})
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
         .build("nautilus")
   );
   public static final RegistryEntry<EntityType<?>, EntityType<ZombieNautilus>> ZOMBIE_NAUTILUS = ENTITIES.register(
      "zombie_nautilus",
      () -> Builder.of(ZombieNautilus::new, MobCategory.MONSTER)
         .sized(0.875F, 0.95F)
         .passengerAttachments(new float[]{1.1375F})
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
         .build("zombie_nautilus")
   );

   public static void registerSpawnPlacements(SpawnPlacementRegistrar registrar) {
      registrar.register(NAUTILUS.get(), SpawnPlacementTypes.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES, AbstractNautilus::checkNautilusSpawnRules);
      registrar.register(ZOMBIE_NAUTILUS.get(), SpawnPlacementTypes.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES, AbstractNautilus::checkNautilusSpawnRules);
      registrar.register(PARCHED.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Parched::checkSpawnRules);
      registrar.register(CAMEL_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, CamelHusk::checkSpawnRules);
   }

   public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> registrar) {
      registrar.accept(PARCHED.get(), Parched.createAttributes().build());
      registrar.accept(CREAKING.get(), Creaking.createAttributes().build());
      registrar.accept(CAMEL_HUSK.get(), Camel.createAttributes().build());
      registrar.accept(COPPER_GOLEM.get(), CopperGolem.createAttributes().build());
      registrar.accept(NAUTILUS.get(), AbstractNautilus.createAttributes().build());
      registrar.accept(ZOMBIE_NAUTILUS.get(), ZombieNautilus.createAttributes().build());
      registrar.accept(HAPPY_GHAST.get(), HappyGhast.createAttributes().build());
   }
}
