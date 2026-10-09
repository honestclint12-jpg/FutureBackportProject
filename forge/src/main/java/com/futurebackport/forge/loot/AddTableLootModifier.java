package com.futurebackport.forge.loot;

import com.futurebackport.forge.ForgeLootModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/** Rolls another loot table into the generated loot (NeoForge's add_table, which Forge 1.20.1 doesn't have). */
public class AddTableLootModifier extends LootModifier {
   public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(
      i -> codecStart(i).and(ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table)).apply(i, AddTableLootModifier::new)
   );
   private final ResourceLocation table;

   public AddTableLootModifier(LootItemCondition[] conditions, ResourceLocation table) {
      super(conditions);
      this.table = table;
   }

   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
      LootTable extra = context.getResolver().getLootTable(this.table);
      extra.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
      return generatedLoot;
   }

   @Override
   public Codec<? extends IGlobalLootModifier> codec() {
      return ForgeLootModifiers.ADD_TABLE.get();
   }
}
