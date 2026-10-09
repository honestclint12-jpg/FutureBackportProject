package com.futurebackport.gametest;

import com.futurebackport.block.CopperChestBlock;
import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.block.entity.CopperGolemStatueBlockEntity;
import com.futurebackport.entity.CopperGolem;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CopperGolemTests {
   @GameTest(
      template = "arena"
   )
   public static void pumpkinOnCopperBuildsGolemAndChest(GameTestHelper helper) {
      BlockPos copper = new BlockPos(5, 1, 5);
      helper.setBlock(copper, Blocks.WEATHERED_COPPER);
      helper.setBlock(copper.above(), (BlockState)Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.EAST));
      // Only this test's arena: tests run side by side and others spawn golems too.
      List<CopperGolem> golems = TestPlayers.entities(helper, ModEntities.COPPER_GOLEM.get());
      helper.assertTrue(golems.size() == 1, "expected one copper golem, found " + golems.size());
      helper.assertTrue(golems.get(0).getWeatherState() == WeatherState.WEATHERED, "golem should match the copper's age");
      BlockState chest = helper.getBlockState(copper);
      helper.assertTrue(
         chest.getBlock() instanceof CopperChestBlock c && c.getWeatherState() == WeatherState.WEATHERED, "copper block should become a weathered copper chest"
      );
      helper.assertTrue(chest.getValue(ChestBlock.FACING) == Direction.EAST, "chest should face like the pumpkin");
      helper.assertBlockNotPresent(Blocks.CARVED_PUMPKIN, copper.above());
      helper.succeed();
   }

   @GameTest(
      template = "arena",
      timeoutTicks = 800
   )
   public static void golemCarriesItemsFromCopperChestToMatchingChest(GameTestHelper helper) {
      BlockPos source = new BlockPos(2, 1, 2);
      BlockPos empty = new BlockPos(9, 1, 2);
      BlockPos matching = new BlockPos(9, 1, 9);
      helper.setBlock(source, (Block)ModBlocks.COPPER_CHEST.weathering().get(WeatherState.UNAFFECTED).get());
      helper.setBlock(empty, Blocks.CHEST);
      helper.setBlock(matching, Blocks.CHEST);
      ((ChestBlockEntity)helper.getBlockEntity(source)).setItem(0, new ItemStack(Items.APPLE, 20));
      ((ChestBlockEntity)helper.getBlockEntity(empty)).setItem(0, new ItemStack(Items.STONE, 1));
      ((ChestBlockEntity)helper.getBlockEntity(matching)).setItem(5, new ItemStack(Items.APPLE, 1));
      helper.spawn((EntityType)ModEntities.COPPER_GOLEM.get(), new BlockPos(5, 1, 5));
      helper.succeedWhen(() -> {
         ChestBlockEntity dest = (ChestBlockEntity)helper.getBlockEntity(matching);
         int apples = 0;

         for (int i = 0; i < dest.getContainerSize(); i++) {
            if (dest.getItem(i).is(Items.APPLE)) {
               apples += dest.getItem(i).getCount();
            }
         }

         helper.assertTrue(apples == 17, "matching chest has " + apples + " apples");
         ChestBlockEntity skipped = (ChestBlockEntity)helper.getBlockEntity(empty);
         helper.assertTrue(skipped.getItem(1).isEmpty(), "golem left apples in a chest without apples");
      });
   }

   @GameTest(
      template = "arena"
   )
   public static void golemBecomesStatueAndAxeRevivesIt(GameTestHelper helper) {
      CopperGolem golem = (CopperGolem)helper.spawn((EntityType)ModEntities.COPPER_GOLEM.get(), new BlockPos(5, 1, 5));
      golem.setCustomName(Component.literal("Rusty"));
      golem.turnToStatue(helper.getLevel());
      BlockPos pos = new BlockPos(5, 1, 5);
      BlockState state = helper.getBlockState(pos);
      helper.assertTrue(
         state.getBlock() instanceof CopperGolemStatueBlock s && s.getWeatherState() == WeatherState.OXIDIZED, "golem did not become an oxidized statue"
      );
      helper.assertTrue(!golem.isAlive(), "golem should be gone");
      Player player = TestPlayers.mock(helper, GameType.SURVIVAL);
      Block statueBlock = (Block)ModBlocks.COPPER_GOLEM_STATUE.weathering().get(WeatherState.UNAFFECTED).get();
      helper.setBlock(pos, statueBlock);
      CopperGolemStatueBlockEntity be = (CopperGolemStatueBlockEntity)helper.getBlockEntity(pos);
      CopperGolem stand = (CopperGolem)((EntityType)ModEntities.COPPER_GOLEM.get()).create(helper.getLevel());
      stand.setCustomName(Component.literal("Rusty"));
      be.createStatue(stand);
      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.UP, helper.absolutePos(pos), false);
      int before = helper.getBlockState(pos).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(pos));
      player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
      int after = helper.getBlockState(pos).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(pos));
      helper.assertTrue(before == 1 && after == 2, "pose comparator output went " + before + " -> " + after);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
      helper.getBlockState(pos).use(helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
      helper.assertBlockNotPresent(statueBlock, pos);
      List<CopperGolem> revived = helper.getLevel().getEntitiesOfClass(CopperGolem.class, new AABB(helper.absolutePos(pos)).inflate(2.0));
      helper.assertTrue(revived.size() == 1 && "Rusty".equals(revived.get(0).getCustomName().getString()), "statue did not turn back into Rusty");
      helper.succeed();
   }
}
