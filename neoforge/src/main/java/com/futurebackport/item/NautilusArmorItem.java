package com.futurebackport.item;

import com.futurebackport.FutureBackport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Builder;

public class NautilusArmorItem extends Item {
   private final ResourceLocation texture;

   public NautilusArmorItem(String material, int defense, float toughness, float knockbackResistance, Properties properties) {
      super(properties.stacksTo(1).attributes(attributes(defense, toughness, knockbackResistance)));
      this.texture = FutureBackport.id("textures/entity/equipment/nautilus_body/" + material + ".png");
   }

   private static ItemAttributeModifiers attributes(int defense, float toughness, float knockbackResistance) {
      ResourceLocation id = FutureBackport.id("armor.body");
      Builder builder = ItemAttributeModifiers.builder()
         .add(Attributes.ARMOR, new AttributeModifier(id, defense, Operation.ADD_VALUE), EquipmentSlotGroup.BODY)
         .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, Operation.ADD_VALUE), EquipmentSlotGroup.BODY);
      if (knockbackResistance > 0.0F) {
         builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, knockbackResistance, Operation.ADD_VALUE), EquipmentSlotGroup.BODY);
      }

      return builder.build();
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }
}
