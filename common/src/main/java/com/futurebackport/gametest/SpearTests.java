package com.futurebackport.gametest;

import com.futurebackport.item.SpearItem;
import com.futurebackport.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public class SpearTests {
   private static Player spearman(GameTestHelper helper) {
      Player player = helper.makeMockPlayer(GameType.SURVIVAL);
      Vec3 pos = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 2)));
      player.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)ModItems.IRON_SPEAR.get()));
      return player;
   }

   private static Mob dummy(GameTestHelper helper, int z) {
      return dummy(helper, EntityType.ZOMBIE, z);
   }

   private static Mob dummy(GameTestHelper helper, EntityType<? extends Mob> type, int z) {
      Mob mob = helper.spawnWithNoFreeWill(type, new BlockPos(2, 1, 2 + z));
      mob.setNoGravity(true);
      return mob;
   }

   @GameTest(
      template = "arena"
   )
   public static void jabHitsEverythingBetweenTwoAndFourAndAHalfBlocks(GameTestHelper helper) {
      Player player = spearman(helper);
      Mob tooClose = dummy(helper, 1);
      Mob first = dummy(helper, 3);
      Mob second = dummy(helper, 4);
      Mob tooFar = dummy(helper, 7);
      ((SpearItem)ModItems.IRON_SPEAR.get()).jab(helper.getLevel(), player);
      helper.assertTrue(first.getHealth() < first.getMaxHealth() && second.getHealth() < second.getMaxHealth(), "jab missed a zombie in reach");
      helper.assertTrue(tooClose.getHealth() == tooClose.getMaxHealth(), "jab hit a zombie closer than 2 blocks");
      helper.assertTrue(tooFar.getHealth() == tooFar.getMaxHealth(), "jab hit a zombie beyond reach");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void chargeDamageGrowsWithSpeed(GameTestHelper helper) {
      Player player = spearman(helper);
      Mob target = dummy(helper, EntityType.COW, 3);
      player.setXRot(20.0F);
      SpearItem spear = (SpearItem)ModItems.IRON_SPEAR.get();
      ItemStack stack = player.getMainHandItem();
      int justAfterDelay = spear.getUseDuration(stack, player) - 12 - 1;
      player.zo = player.getZ() - 0.4;
      spear.onUseTick(helper.getLevel(), player, stack, justAfterDelay);
      float dealt = target.getMaxHealth() - target.getHealth();
      helper.assertTrue(Math.abs(dealt - 8.0F) < 0.01F, "charge dealt " + dealt + ", expected 8");
      target.discard();
      Mob other = dummy(helper, EntityType.COW, 3);
      Player still = spearman(helper);
      still.setXRot(20.0F);
      still.xo = still.getX();
      still.yo = still.getY();
      still.zo = still.getZ();
      spear.onUseTick(helper.getLevel(), still, still.getMainHandItem(), justAfterDelay);
      helper.assertTrue(other.getHealth() == other.getMaxHealth(), "a standing charge hurt the target");
      helper.succeed();
   }
}
