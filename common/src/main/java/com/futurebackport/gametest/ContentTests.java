package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModItems;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.storage.loot.LootTable;

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

   /** Catches ingredients naming a tag that doesn't exist on this version (they load fine but match nothing). */
   @GameTest(
      template = "arena"
   )
   public static void everyRecipeIngredientMatchesAnItem(GameTestHelper helper) {
      List<String> broken = new ArrayList<>();

      for (RecipeHolder<?> recipe : helper.getLevel().getRecipeManager().getRecipes()) {
         if (recipe.id().getNamespace().equals("futurebackport")) {
            for (Ingredient ingredient : recipe.value().getIngredients()) {
               // An empty ingredient is a blank slot in a shaped recipe. NeoForge shows a barrier for an empty tag, which the tag itself doesn't match.
               if (!ingredient.isEmpty() && Arrays.stream(ingredient.getItems()).noneMatch(ingredient)) {
                  broken.add(recipe.id().getPath());
                  break;
               }
            }
         }
      }

      helper.assertTrue(broken.isEmpty(), "Recipes with an ingredient nothing matches: " + broken);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void copperPickaxeAndPaleOakPlanksCraft(GameTestHelper helper) {
      ItemStack copper = new ItemStack(Items.COPPER_INGOT);
      ItemStack stick = new ItemStack(Items.STICK);
      ItemStack empty = ItemStack.EMPTY;
      assertCrafts(helper, CraftingInput.of(3, 3, List.of(copper, copper, copper, empty, stick, empty, empty, stick, empty)),
         ModItems.COPPER_PICKAXE.get(), 1);
      assertCrafts(helper, CraftingInput.of(1, 1, List.of(new ItemStack(ModBlocks.PALE_OAK_LOG.get()))), ModBlocks.PALE_OAK_PLANKS.get().asItem(), 4);
      helper.succeed();
   }

   private static void assertCrafts(GameTestHelper helper, CraftingInput input, Item expected, int count) {
      ItemStack result = helper.getLevel()
         .getRecipeManager()
         .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
         .map(recipe -> recipe.value().assemble(input, helper.getLevel().registryAccess()))
         .orElse(ItemStack.EMPTY);
      helper.assertTrue(result.is(expected) && result.getCount() == count, "expected " + count + " " + expected + ", crafted " + result);
   }
}
