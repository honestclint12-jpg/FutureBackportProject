package com.futurebackport.gametest;

import com.futurebackport.block.ShelfBlock;
import com.futurebackport.block.SideChainPart;
import com.futurebackport.block.entity.ShelfBlockEntity;
import com.futurebackport.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShelfTests {
   @GameTest(
      template = "arena"
   )
   public static void poweredShelvesChainUpToThree(GameTestHelper helper) {
      BlockState shelf = (BlockState)((ShelfBlock)ModBlocks.SHELVES.get("oak").get()).defaultBlockState().setValue(ShelfBlock.FACING, Direction.NORTH);

      for (int x = 1; x <= 4; x++) {
         helper.setBlock(new BlockPos(x, 1, 3), Blocks.REDSTONE_BLOCK);
         helper.setBlock(new BlockPos(x, 1, 2), (BlockState)shelf.setValue(ShelfBlock.POWERED, true));
      }

      int connected = 0;

      for (int x = 1; x <= 4; x++) {
         if (helper.getBlockState(new BlockPos(x, 1, 2)).getValue(ShelfBlock.SIDE_CHAIN_PART) != SideChainPart.UNCONNECTED) {
            connected++;
         }
      }

      helper.assertTrue(connected == 3, "expected three chained shelves, found " + connected);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void shelfComparatorReadsSlots(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 1, 2), (Block)ModBlocks.SHELVES.get("pale_oak").get());
      ShelfBlockEntity shelf = (ShelfBlockEntity)helper.getBlockEntity(new BlockPos(2, 1, 2));
      shelf.swapItemNoUpdate(0, new ItemStack(Items.DIAMOND));
      shelf.swapItemNoUpdate(2, new ItemStack(Items.EMERALD));
      int signal = helper.getBlockState(new BlockPos(2, 1, 2)).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 2)));
      helper.assertTrue(signal == 5, "expected comparator signal 5 (slots 1 and 3), got " + signal);
      helper.succeed();
   }
}
