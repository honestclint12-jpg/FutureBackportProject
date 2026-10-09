package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import com.futurebackport.platform.util.SimpleToolTier;
import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class ModMaterials {
   private static final TagKey<Item> COPPER_TOOL_MATERIALS = TagKey.create(Registries.ITEM, FutureBackport.id("copper_tool_materials"));
   private static final TagKey<Item> REPAIRS_COPPER_ARMOR = TagKey.create(Registries.ITEM, FutureBackport.id("repairs_copper_armor"));
   /** Mining level 1 (stone): 1.20.1 has no incorrect_for_*_tool tags. */
   public static final Tier COPPER_TIER = new SimpleToolTier(1, 190, 5.0F, 1.0F, 13, () -> Ingredient.of(COPPER_TOOL_MATERIALS));
   public static final int COPPER_ARMOR_DURABILITY = 11;
   public static final ArmorMaterial COPPER_ARMOR = new ArmorMaterial() {
      private static final EnumMap<Type, Integer> DURABILITY = Util.make(new EnumMap<>(Type.class), map -> {
         map.put(Type.BOOTS, 13);
         map.put(Type.LEGGINGS, 15);
         map.put(Type.CHESTPLATE, 16);
         map.put(Type.HELMET, 11);
      });
      private static final EnumMap<Type, Integer> DEFENSE = Util.make(new EnumMap<>(Type.class), map -> {
         map.put(Type.BOOTS, 1);
         map.put(Type.LEGGINGS, 3);
         map.put(Type.CHESTPLATE, 4);
         map.put(Type.HELMET, 2);
      });

      @Override
      public int getDurabilityForType(Type type) {
         return DURABILITY.get(type) * COPPER_ARMOR_DURABILITY;
      }

      @Override
      public int getDefenseForType(Type type) {
         return DEFENSE.get(type);
      }

      @Override
      public int getEnchantmentValue() {
         return 8;
      }

      @Override
      public SoundEvent getEquipSound() {
         return ModSounds.ARMOR_EQUIP_COPPER.get();
      }

      @Override
      public Ingredient getRepairIngredient() {
         return Ingredient.of(REPAIRS_COPPER_ARMOR);
      }

      /** Namespaced so the armor layer texture resolves to futurebackport:textures/models/armor/copper_layer_N.png. */
      @Override
      public String getName() {
         return FutureBackport.id("copper").toString();
      }

      @Override
      public float getToughness() {
         return 0.0F;
      }

      @Override
      public float getKnockbackResistance() {
         return 0.0F;
      }
   };
}
