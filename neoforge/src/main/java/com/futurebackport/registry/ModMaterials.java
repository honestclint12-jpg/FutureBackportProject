package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;
import com.futurebackport.platform.util.SimpleToolTier;

import com.futurebackport.FutureBackport;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public class ModMaterials {
   public static final RegistrationProvider<ArmorMaterial> ARMOR_MATERIALS = RegistrationProvider.create(Registries.ARMOR_MATERIAL, "futurebackport");
   private static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = TagKey.create(Registries.BLOCK, FutureBackport.id("incorrect_for_copper_tool"));
   private static final TagKey<Item> COPPER_TOOL_MATERIALS = TagKey.create(Registries.ITEM, FutureBackport.id("copper_tool_materials"));
   private static final TagKey<Item> REPAIRS_COPPER_ARMOR = TagKey.create(Registries.ITEM, FutureBackport.id("repairs_copper_armor"));
   public static final Tier COPPER_TIER = new SimpleToolTier(INCORRECT_FOR_COPPER_TOOL, 190, 5.0F, 1.0F, 13, () -> Ingredient.of(COPPER_TOOL_MATERIALS));
   public static final int COPPER_ARMOR_DURABILITY = 11;
   public static final RegistryEntry<ArmorMaterial, ArmorMaterial> COPPER_ARMOR = ARMOR_MATERIALS.register(
      "copper", () -> new ArmorMaterial((Map)Util.make(new EnumMap(Type.class), map -> {
         map.put(Type.BOOTS, 1);
         map.put(Type.LEGGINGS, 3);
         map.put(Type.CHESTPLATE, 4);
         map.put(Type.HELMET, 2);
         map.put(Type.BODY, 4);
      }), 8, ModSounds.ARMOR_EQUIP_COPPER.holder(), () -> Ingredient.of(REPAIRS_COPPER_ARMOR), List.of(new Layer(FutureBackport.id("copper"))), 0.0F, 0.0F)
   );
}
