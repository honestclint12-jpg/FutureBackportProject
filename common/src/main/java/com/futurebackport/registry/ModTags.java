package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

public class ModTags {
   public static final TagKey<Item> NAUTILUS_FOOD = TagKey.create(Registries.ITEM, FutureBackport.id("nautilus_food"));
   public static final TagKey<Item> NAUTILUS_TAMING_ITEMS = TagKey.create(Registries.ITEM, FutureBackport.id("nautilus_taming_items"));
   public static final TagKey<Item> NAUTILUS_BUCKET_FOOD = TagKey.create(Registries.ITEM, FutureBackport.id("nautilus_bucket_food"));
   public static final TagKey<EntityType<?>> NAUTILUS_HOSTILES = TagKey.create(Registries.ENTITY_TYPE, FutureBackport.id("nautilus_hostiles"));
   public static final TagKey<Biome> SPAWNS_CORAL_VARIANT_ZOMBIE_NAUTILUS = TagKey.create(
      Registries.BIOME, FutureBackport.id("spawns_coral_variant_zombie_nautilus")
   );
}
