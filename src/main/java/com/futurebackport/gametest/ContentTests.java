package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("futurebackport")
@PrefixGameTestTemplate(false)
public class ContentTests {
   @GameTest(
      template = "arena"
   )
   public static void everyBlockHasLootTable(GameTestHelper helper) {
      for (Block block : BuiltInRegistries.BLOCK) {
         if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("futurebackport")) {
            ResourceKey<LootTable> key = block.getLootTable();
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            helper.assertTrue(table != LootTable.EMPTY, "Missing loot table " + key.location() + " for " + block);
         }
      }

      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void everyPlaceableBlockHasItem(GameTestHelper helper) {
      for (Block block : BuiltInRegistries.BLOCK) {
         if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("futurebackport")
            && !(block instanceof WallSignBlock)
            && !(block instanceof WallHangingSignBlock)
            && !(block instanceof FlowerPotBlock)) {
            helper.assertTrue(block.asItem() != Items.AIR, "No item for " + block);
         }
      }

      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void everyItemHasRecipeOrIsNatural(GameTestHelper helper) {
      Collection<RecipeHolder<?>> recipes = helper.getLevel().getRecipeManager().getRecipes();
      List<String> missing = new ArrayList<>();

      for (Item item : BuiltInRegistries.ITEM) {
         ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
         if (id.getNamespace().equals("futurebackport")
            && !item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, FutureBackport.id("uncraftable")))
            && !(item instanceof SpawnEggItem)) {
            boolean craftable = recipes.stream().anyMatch(r -> r.value().getResultItem(helper.getLevel().registryAccess()).is(item));
            if (!craftable) {
               missing.add(id.getPath());
            }
         }
      }

      helper.assertTrue(missing.isEmpty(), "No recipe produces " + missing);
      helper.succeed();
   }
}
