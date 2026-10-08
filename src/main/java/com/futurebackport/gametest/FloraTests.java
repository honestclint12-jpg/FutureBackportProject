package com.futurebackport.gametest;

import com.futurebackport.block.HangingMossBlock;
import com.futurebackport.block.LeafLitterBlock;
import com.futurebackport.block.MossyCarpetBlock;
import com.futurebackport.block.ResinClumpBlock;
import com.futurebackport.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("futurebackport")
@PrefixGameTestTemplate(false)
public class FloraTests {
   private static void bonemeal(GameTestHelper helper, BlockPos pos) {
      BlockPos abs = helper.absolutePos(pos);
      BlockState state = helper.getLevel().getBlockState(abs);
      BonemealableBlock block = (BonemealableBlock)state.getBlock();
      helper.assertTrue(block.isValidBonemealTarget(helper.getLevel(), abs, state), state + " is not a valid bonemeal target");
      block.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(), abs, state);
   }

   @GameTest(
      template = "arena"
   )
   public static void eyeblossomFollowsDayCycle(GameTestHelper helper) {
      helper.getLevel().setDayTime(18000L);
      helper.getLevel().updateSkyBrightness();
      helper.setBlock(new BlockPos(2, 0, 2), (Block)ModBlocks.PALE_MOSS_BLOCK.get());
      helper.setBlock(new BlockPos(2, 1, 2), (Block)ModBlocks.CLOSED_EYEBLOSSOM.get());
      BlockPos abs = helper.absolutePos(new BlockPos(2, 1, 2));
      helper.getBlockState(new BlockPos(2, 1, 2)).randomTick(helper.getLevel(), abs, helper.getLevel().getRandom());
      helper.assertBlockPresent((Block)ModBlocks.OPEN_EYEBLOSSOM.get(), new BlockPos(2, 1, 2));
      helper.getLevel().setDayTime(6000L);
      helper.getLevel().updateSkyBrightness();
      helper.getBlockState(new BlockPos(2, 1, 2)).randomTick(helper.getLevel(), abs, helper.getLevel().getRandom());
      helper.assertBlockPresent((Block)ModBlocks.CLOSED_EYEBLOSSOM.get(), new BlockPos(2, 1, 2));
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void hangingMossGrowsDown(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 5, 2), Blocks.STONE);
      helper.setBlock(new BlockPos(2, 4, 2), (Block)ModBlocks.PALE_HANGING_MOSS.get());
      bonemeal(helper, new BlockPos(2, 4, 2));
      helper.assertBlockPresent((Block)ModBlocks.PALE_HANGING_MOSS.get(), new BlockPos(2, 3, 2));
      helper.assertBlockProperty(new BlockPos(2, 4, 2), HangingMossBlock.TIP, false);
      helper.assertBlockProperty(new BlockPos(2, 3, 2), HangingMossBlock.TIP, true);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleMossCarpetClimbsWalls(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
      helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
      helper.setBlock(new BlockPos(2, 2, 1), Blocks.STONE);
      BlockPos carpet = new BlockPos(2, 1, 2);
      BlockState placed = ((MossyCarpetBlock)ModBlocks.PALE_MOSS_CARPET.get()).defaultBlockState();
      helper.setBlock(carpet, placed);
      helper.getLevel().neighborChanged(helper.absolutePos(carpet), Blocks.STONE, helper.absolutePos(new BlockPos(2, 1, 1)));
      helper.setBlock(
         carpet,
         placed.updateShape(
            Direction.NORTH, Blocks.STONE.defaultBlockState(), helper.getLevel(), helper.absolutePos(carpet), helper.absolutePos(new BlockPos(2, 1, 1))
         )
      );
      bonemeal(helper, carpet);
      helper.assertBlockPresent((Block)ModBlocks.PALE_MOSS_CARPET.get(), new BlockPos(2, 2, 2));
      helper.assertBlockProperty(new BlockPos(2, 2, 2), MossyCarpetBlock.BASE, false);
      helper.assertBlockProperty(new BlockPos(2, 2, 2), MossyCarpetBlock.NORTH, WallSide.LOW);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void leafLitterStacksToFour(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE);
      helper.setBlock(
         new BlockPos(2, 1, 2), (BlockState)((LeafLitterBlock)ModBlocks.LEAF_LITTER.get()).defaultBlockState().setValue(LeafLitterBlock.SEGMENT_AMOUNT, 4)
      );
      helper.assertTrue(
         !helper.getBlockState(new BlockPos(2, 1, 2)).getShape(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 2))).isEmpty(),
         "leaf litter has no shape"
      );
      helper.setBlock(new BlockPos(2, 0, 2), Blocks.AIR);
      helper.assertBlockNotPresent((Block)ModBlocks.LEAF_LITTER.get(), new BlockPos(2, 1, 2));
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void bonemealSpreadsAndGrows(GameTestHelper helper) {
      for (int x = 1; x <= 3; x++) {
         for (int z = 1; z <= 7; z++) {
            helper.setBlock(new BlockPos(x, 0, z), z >= 5 ? Blocks.SAND : Blocks.GRASS_BLOCK);
         }
      }

      helper.setBlock(new BlockPos(2, 1, 2), (Block)ModBlocks.BUSH.get());
      bonemeal(helper, new BlockPos(2, 1, 2));
      int bushes = 0;

      for (Direction d : Plane.HORIZONTAL) {
         if (helper.getBlockState(new BlockPos(2, 1, 2).relative(d)).is(ModBlocks.BUSH.get())) {
            bushes++;
         }
      }

      helper.assertTrue(bushes == 1, "bush spread to " + bushes + " neighbours");
      helper.setBlock(new BlockPos(2, 1, 6), (Block)ModBlocks.SHORT_DRY_GRASS.get());
      bonemeal(helper, new BlockPos(2, 1, 6));
      helper.assertBlockPresent((Block)ModBlocks.TALL_DRY_GRASS.get(), new BlockPos(2, 1, 6));
      helper.setBlock(new BlockPos(2, 0, 4), (Block)ModBlocks.PALE_MOSS_BLOCK.get());
      bonemeal(helper, new BlockPos(2, 0, 4));
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void cactusGrowsFlowers(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 0, 2), Blocks.SAND);
      helper.setBlock(new BlockPos(2, 1, 2), Blocks.CACTUS);
      helper.setBlock(new BlockPos(2, 2, 2), Blocks.CACTUS);
      helper.setBlock(new BlockPos(2, 3, 2), (BlockState)Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 8));
      BlockPos top = helper.absolutePos(new BlockPos(2, 3, 2));

      for (int i = 0; i < 200 && !helper.getBlockState(new BlockPos(2, 4, 2)).is(ModBlocks.CACTUS_FLOWER.get()); i++) {
         helper.getLevel().setBlock(top, (BlockState)Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 8), 260);
         helper.getLevel().getBlockState(top).randomTick(helper.getLevel(), top, helper.getLevel().getRandom());
      }

      helper.assertBlockPresent((Block)ModBlocks.CACTUS_FLOWER.get(), new BlockPos(2, 4, 2));
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void resinClumpAttachesToWalls(GameTestHelper helper) {
      helper.setBlock(new BlockPos(2, 1, 1), Blocks.STONE);
      BlockState clump = (BlockState)((ResinClumpBlock)ModBlocks.RESIN_CLUMP.get())
         .defaultBlockState()
         .setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true);
      helper.setBlock(new BlockPos(2, 1, 2), clump);
      helper.assertBlockPresent((Block)ModBlocks.RESIN_CLUMP.get(), new BlockPos(2, 1, 2));
      helper.succeed();
   }
}
