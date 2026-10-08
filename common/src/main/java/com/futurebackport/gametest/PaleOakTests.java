package com.futurebackport.gametest;

import com.futurebackport.block.ParticleLeavesBlock;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModBoats;
import com.futurebackport.registry.ModItems;
import com.futurebackport.registry.ModTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PaleOakTests {
   @GameTest(
      template = "arena"
   )
   public static void paleOakTags(GameTestHelper helper) {
      helper.assertTrue(((RotatedPillarBlock)ModBlocks.PALE_OAK_LOG.get()).defaultBlockState().is(BlockTags.LOGS_THAT_BURN), "log not in logs_that_burn");
      helper.assertTrue(((RotatedPillarBlock)ModBlocks.PALE_OAK_LOG.get()).defaultBlockState().is(BlockTags.MINEABLE_WITH_AXE), "log not axe-mineable");
      helper.assertTrue(((ParticleLeavesBlock)ModBlocks.PALE_OAK_LEAVES.get()).defaultBlockState().is(BlockTags.LEAVES), "leaves not in leaves tag");
      helper.assertTrue(((StandingSignBlock)ModBlocks.PALE_OAK_SIGN.get()).defaultBlockState().is(BlockTags.STANDING_SIGNS), "sign not in standing_signs");
      helper.assertTrue(((DoorBlock)ModBlocks.PALE_OAK_DOOR.get()).defaultBlockState().is(BlockTags.WOODEN_DOORS), "door not in wooden_doors");
      helper.assertTrue(((Block)ModBlocks.PALE_OAK_PLANKS.get()).asItem().builtInRegistryHolder().is(ItemTags.PLANKS), "planks not in planks item tag");
      helper.assertTrue(((BoatItem)ModItems.PALE_OAK_BOAT.get()).builtInRegistryHolder().is(ItemTags.BOATS), "boat not in boats tag");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleOakStripsAndBurns(GameTestHelper helper) {
      // Strip with a real axe: NeoForge reads its strippables data map, Fabric its StrippableBlockRegistry.
      BlockPos log = new BlockPos(1, 1, 1);
      helper.setBlock(log, (Block)ModBlocks.PALE_OAK_LOG.get());
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(log)), Direction.UP, helper.absolutePos(log), false);
      Items.IRON_AXE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
      helper.assertBlockPresent((Block)ModBlocks.STRIPPED_PALE_OAK_LOG.get(), log);
      // Fire placed beside the planks clings to them only if they can burn.
      BlockPos planks = new BlockPos(1, 2, 3);
      helper.setBlock(planks, (Block)ModBlocks.PALE_OAK_PLANKS.get());
      BlockState fire = BaseFireBlock.getState(helper.getLevel(), helper.absolutePos(planks.east()));
      helper.assertTrue(fire.is(Blocks.FIRE) && fire.getValue(FireBlock.WEST), "planks not flammable");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleOakSignsHaveBlockEntities(GameTestHelper helper) {
      helper.setBlock(new BlockPos(1, 1, 1), (Block)ModBlocks.PALE_OAK_SIGN.get());
      helper.setBlock(new BlockPos(3, 3, 3), Blocks.OAK_PLANKS);
      helper.setBlock(new BlockPos(3, 2, 3), (Block)ModBlocks.PALE_OAK_HANGING_SIGN.get());
      helper.assertTrue(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof SignBlockEntity, "sign has no block entity");
      helper.assertTrue(helper.getBlockEntity(new BlockPos(3, 2, 3)) instanceof HangingSignBlockEntity, "hanging sign has no block entity");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleOakBoatRoundTrips(GameTestHelper helper) {
      Type type = ModBoats.paleOak();
      Boat boat = (Boat)helper.spawn(EntityType.BOAT, new BlockPos(2, 2, 2));
      helper.assertTrue(type != Type.OAK, "no pale oak boat type on this loader");
      helper.assertTrue(type.getPlanks() == ModBlocks.PALE_OAK_PLANKS.get(), "boat type has planks " + type.getPlanks());
      boat.setVariant(type);
      helper.assertTrue(boat.getDropItem() == ModItems.PALE_OAK_BOAT.get(), "boat drops " + boat.getDropItem());
      ChestBoat chestBoat = (ChestBoat)helper.spawn(EntityType.CHEST_BOAT, new BlockPos(2, 2, 4));
      chestBoat.setVariant(type);
      helper.assertTrue(chestBoat.getDropItem() == ModItems.PALE_OAK_CHEST_BOAT.get(), "chest boat drops " + chestBoat.getDropItem());
      helper.assertTrue(Type.byName(type.getName()) == type, "boat type does not serialize");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleOakGrowsFrom2x2(GameTestHelper helper) {
      ServerLevel level = helper.getLevel();

      for (int x = 4; x <= 5; x++) {
         for (int z = 4; z <= 5; z++) {
            helper.setBlock(new BlockPos(x, 0, z), Blocks.DIRT);
            helper.setBlock(new BlockPos(x, 1, z), (Block)ModBlocks.PALE_OAK_SAPLING.get());
         }
      }

      BlockPos corner = helper.absolutePos(new BlockPos(4, 1, 4));
      boolean grown = ModTreeGrowers.PALE_OAK.growTree(level, level.getChunkSource().getGenerator(), corner, level.getBlockState(corner), level.getRandom());
      helper.assertTrue(grown, "tree failed to grow");
      helper.assertBlockPresent((Block)ModBlocks.PALE_OAK_LOG.get(), new BlockPos(4, 2, 4));
      helper.succeed();
   }
}
