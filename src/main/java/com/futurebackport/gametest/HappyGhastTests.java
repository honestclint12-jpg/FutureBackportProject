package com.futurebackport.gametest;

import com.futurebackport.block.DriedGhastBlock;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("futurebackport")
@PrefixGameTestTemplate(false)
public class HappyGhastTests {
   @GameTest(
      template = "arena"
   )
   public static void waterloggedDriedGhastHatchesGhastling(GameTestHelper helper) {
      BlockPos pos = new BlockPos(5, 1, 5);
      helper.setBlock(pos.below(), Blocks.STONE);
      helper.setBlock(pos, (BlockState)((DriedGhastBlock)ModBlocks.DRIED_GHAST.get()).defaultBlockState().setValue(DriedGhastBlock.WATERLOGGED, true));

      for (int i = 0; i < 4; i++) {
         BlockState state = helper.getBlockState(pos);
         if (state.is(ModBlocks.DRIED_GHAST)) {
            state.tick(helper.getLevel(), helper.absolutePos(pos), helper.getLevel().getRandom());
         }
      }

      List<HappyGhast> ghasts = helper.getLevel().getEntitiesOfClass(HappyGhast.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0));
      helper.assertTrue(ghasts.size() == 1 && ghasts.get(0).isBaby(), "expected one ghastling, found " + ghasts.size());
      helper.assertBlockNotPresent((Block)ModBlocks.DRIED_GHAST.get(), pos);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void harnessedHappyGhastCanBeRidden(GameTestHelper helper) {
      HappyGhast ghast = (HappyGhast)helper.spawn((EntityType)ModEntities.HAPPY_GHAST.get(), new BlockPos(5, 2, 5));
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      player.setGameMode(GameType.SURVIVAL);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)ModItems.HARNESSES.get(DyeColor.RED).get()));
      ghast.mobInteract(player, InteractionHand.MAIN_HAND);
      helper.assertTrue(ghast.isWearingHarness(), "harness was not equipped");
      helper.assertTrue(player.getMainHandItem().isEmpty(), "harness was not consumed");
      ghast.mobInteract(player, InteractionHand.MAIN_HAND);
      helper.assertTrue(player.getVehicle() == ghast, "player did not mount the happy ghast");
      helper.assertTrue(ghast.getControllingPassenger() == player || ghast.isOnStillTimeout(), "rider does not control the happy ghast");
      helper.assertTrue(ghast.getItemBySlot(EquipmentSlot.BODY).is((Item)ModItems.HARNESSES.get(DyeColor.RED).get()), "wrong body item");
      helper.succeed();
   }
}
