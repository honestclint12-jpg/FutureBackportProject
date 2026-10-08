package com.futurebackport.gametest;

import com.futurebackport.block.entity.CopperChestBlockEntity;
import com.futurebackport.registry.CopperFamily;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.HoneycombItem;

public class CopperTests {
   @GameTest(
      template = "arena"
   )
   public static void copperOxidizesAndWaxes(GameTestHelper helper) {
      CopperFamily bars = ModBlocks.COPPER_BARS;
      helper.assertTrue(
         WeatheringCopper.getNext((Block)bars.weathering().get(WeatherState.UNAFFECTED).get()).orElse(null)
            == bars.weathering().get(WeatherState.EXPOSED).get(),
         "bars do not oxidize"
      );
      helper.assertTrue(
         WeatheringCopper.getNext(Blocks.LIGHTNING_ROD).orElse(null) == ModBlocks.LIGHTNING_ROD.weathering().get(WeatherState.EXPOSED).get(),
         "vanilla lightning rod has no next stage"
      );
      helper.assertTrue(
         HoneycombItem.getWaxed(Blocks.LIGHTNING_ROD.defaultBlockState()).map(BlockState::getBlock).orElse(null)
            == ModBlocks.LIGHTNING_ROD.waxed().get(WeatherState.UNAFFECTED).get(),
         "vanilla lightning rod cannot be waxed"
      );
      helper.assertTrue(
         WeatheringCopper.getPrevious((Block)ModBlocks.COPPER_LANTERN.weathering().get(WeatherState.OXIDIZED).get()).isPresent(),
         "oxidized lantern cannot be scraped"
      );
      helper.assertTrue(((Block)bars.weathering().get(WeatherState.UNAFFECTED).get()).defaultBlockState().isRandomlyTicking(), "copper bars do not tick");
      helper.assertTrue(!((Block)bars.weathering().get(WeatherState.OXIDIZED).get()).defaultBlockState().isRandomlyTicking(), "oxidized bars still tick");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void vanillaLightningRodOxidizes(GameTestHelper helper) {
      BlockPos pos = new BlockPos(2, 1, 2);
      helper.setBlock(pos, Blocks.LIGHTNING_ROD);
      helper.assertTrue(helper.getBlockState(pos).isRandomlyTicking(), "vanilla lightning rod does not tick");
      BlockState waxed = ((Block)ModBlocks.LIGHTNING_ROD.waxed().get(WeatherState.UNAFFECTED).get()).defaultBlockState();
      helper.assertTrue(!waxed.isRandomlyTicking(), "waxed lightning rod ticks");

      for (int i = 0; i < 2000 && helper.getBlockState(pos).is(Blocks.LIGHTNING_ROD); i++) {
         helper.getBlockState(pos).randomTick(helper.getLevel(), helper.absolutePos(pos), helper.getLevel().getRandom());
      }

      helper.assertBlockPresent((Block)ModBlocks.LIGHTNING_ROD.weathering().get(WeatherState.EXPOSED).get(), pos);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void doubleCopperChestOxidizesTogether(GameTestHelper helper) {
      BlockState chest = (BlockState)((Block)ModBlocks.COPPER_CHEST.weathering().get(WeatherState.UNAFFECTED).get())
         .defaultBlockState()
         .setValue(ChestBlock.FACING, Direction.NORTH);
      BlockPos left = new BlockPos(3, 1, 3);
      BlockPos right = new BlockPos(4, 1, 3);
      helper.setBlock(left, (BlockState)chest.setValue(ChestBlock.TYPE, ChestType.LEFT));
      helper.setBlock(right, (BlockState)chest.setValue(ChestBlock.TYPE, ChestType.RIGHT));
      Block exposed = (Block)ModBlocks.COPPER_CHEST.weathering().get(WeatherState.EXPOSED).get();
      helper.setBlock(left, exposed.withPropertiesOf(helper.getBlockState(left)));
      helper.assertBlockPresent(exposed, right);
      helper.assertBlockProperty(right, ChestBlock.TYPE, ChestType.RIGHT);
      helper.assertTrue(helper.getBlockEntity(left) instanceof CopperChestBlockEntity, "copper chest has the wrong block entity");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void copperToolsMineIronButNotDiamond(GameTestHelper helper) {
      ItemStack pickaxe = new ItemStack((ItemLike)ModItems.COPPER_PICKAXE.get());
      helper.assertTrue(pickaxe.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()), "copper pickaxe cannot mine iron ore");
      helper.assertTrue(!pickaxe.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "copper pickaxe can mine diamond ore");
      helper.assertTrue(pickaxe.getMaxDamage() == 190, "copper pickaxe durability " + pickaxe.getMaxDamage());
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void lootInjectionAddsCopperHorseArmor(GameTestHelper helper) {
      MinecraftServer server = helper.getLevel().getServer();
      LootTable table = server.reloadableRegistries()
         .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/desert_pyramid")));
      BlockPos origin = helper.absolutePos(BlockPos.ZERO);
      LootParams params = new Builder(helper.getLevel()).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin)).create(LootContextParamSets.CHEST);
      boolean found = false;

      for (int i = 0; i < 400 && !found; i++) {
         found = table.getRandomItems(params).stream().anyMatch(s -> s.is((Item)ModItems.COPPER_HORSE_ARMOR.get()));
      }

      helper.assertTrue(found, "copper horse armor never rolled from desert pyramid loot");
      helper.succeed();
   }
}
