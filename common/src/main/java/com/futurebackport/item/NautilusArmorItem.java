package com.futurebackport.item;

import com.futurebackport.FutureBackport;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

/** Nautilus body armor. The nautilus applies {@link #applyModifiers} itself (1.20.1 has no BODY slot modifiers). */
public class NautilusArmorItem extends Item {
   private final ResourceLocation texture;
   private final int defense;
   private final float toughness;
   private final float knockbackResistance;

   public NautilusArmorItem(String material, int defense, float toughness, float knockbackResistance, Properties properties) {
      super(properties.stacksTo(1));
      this.texture = FutureBackport.id("textures/entity/equipment/nautilus_body/" + material + ".png");
      this.defense = defense;
      this.toughness = toughness;
      this.knockbackResistance = knockbackResistance;
   }

   public void applyModifiers(LivingEntity entity, UUID id) {
      add(entity, Attributes.ARMOR, id, this.defense);
      add(entity, Attributes.ARMOR_TOUGHNESS, id, this.toughness);
      if (this.knockbackResistance > 0.0F) {
         add(entity, Attributes.KNOCKBACK_RESISTANCE, id, this.knockbackResistance);
      }
   }

   private static void add(LivingEntity entity, Attribute attribute, UUID id, double amount) {
      AttributeInstance instance = entity.getAttribute(attribute);
      if (instance != null && amount != 0.0) {
         instance.addTransientModifier(new AttributeModifier(id, "Nautilus armor", amount, Operation.ADDITION));
      }
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }
}
