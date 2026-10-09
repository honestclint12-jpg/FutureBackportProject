package com.futurebackport.gametest;

import net.minecraft.world.entity.MobType;
import com.futurebackport.entity.nautilus.AbstractNautilus;
import com.futurebackport.entity.nautilus.ZombieNautilus;
import com.futurebackport.registry.ModEffects;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class NautilusTests {
   private static void flood(GameTestHelper helper) {
      for (int x = 1; x < 11; x++) {
         for (int z = 1; z < 11; z++) {
            for (int y = 1; y < 5; y++) {
               helper.setBlock(x, y, z, Blocks.WATER);
            }
         }
      }
   }

   @GameTest(
      template = "arena"
   )
   public static void pufferfishTamesThenSaddleAndArmorEquip(GameTestHelper helper) {
      flood(helper);
      AbstractNautilus nautilus = (AbstractNautilus)helper.spawn((EntityType)ModEntities.NAUTILUS.get(), new BlockPos(5, 2, 5));
      Player player = TestPlayers.mock(helper, GameType.SURVIVAL);
      player.moveTo(helper.absoluteVec(new Vec3(5.5, 2.0, 4.0)));
      helper.assertTrue(
         !nautilus.isFood(new ItemStack(Items.COD)) && nautilus.isFood(new ItemStack(Items.PUFFERFISH)), "untamed nautiluses only take pufferfish"
      );

      for (int i = 0; i < 200 && !nautilus.isTame(); i++) {
         player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUFFERFISH));
         nautilus.mobInteract(player, InteractionHand.MAIN_HAND);
      }

      helper.assertTrue(nautilus.isTame() && player.getUUID().equals(nautilus.getOwnerUUID()), "pufferfish never tamed the nautilus");
      helper.assertTrue(nautilus.isFood(new ItemStack(Items.COD)), "tamed nautiluses eat any fish");
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
      nautilus.mobInteract(player, InteractionHand.MAIN_HAND);
      helper.assertTrue(nautilus.isSaddled(), "saddle did not go on");
      double armorBefore = nautilus.getAttributeValue(Attributes.ARMOR);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)ModItems.DIAMOND_NAUTILUS_ARMOR.get()));
      nautilus.mobInteract(player, InteractionHand.MAIN_HAND);
      helper.assertTrue(nautilus.getBodyArmorItem().is((Item)ModItems.DIAMOND_NAUTILUS_ARMOR.get()), "armor not in the body slot");
      player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      nautilus.mobInteract(player, InteractionHand.MAIN_HAND);
      helper.assertTrue(player.getVehicle() == nautilus && nautilus.getControllingPassenger() == player, "empty hand should mount a saddled tame nautilus");
      helper.runAfterDelay(
         2L,
         () -> {
            helper.assertTrue(player.hasEffect(ModEffects.BREATH_OF_THE_NAUTILUS.get()), "rider has no Breath of the Nautilus");
            helper.assertTrue(
               nautilus.getAttributeValue(Attributes.ARMOR) - armorBefore == 11.0,
               "diamond nautilus armor should give 11 armor, gave " + (nautilus.getAttributeValue(Attributes.ARMOR) - armorBefore)
            );
            helper.succeed();
         }
      );
   }

   @GameTest(
      template = "arena"
   )
   public static void tridentDrownedRideZombieNautiluses(GameTestHelper helper) {
      flood(helper);
      ServerLevel level = helper.getLevel();
      int jockeys = 0;

      for (int i = 0; i < 40; i++) {
         Drowned drowned = (Drowned)EntityType.DROWNED.create(level);
         drowned.moveTo(helper.absoluteVec(new Vec3(5.5, 2.0, 5.5)));
         drowned.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TRIDENT));
         drowned.finalizeSpawn(level, level.getCurrentDifficultyAt(drowned.blockPosition()), MobSpawnType.NATURAL, null, null);
         if (drowned.getMainHandItem().is(Items.TRIDENT) && drowned.getVehicle() instanceof ZombieNautilus z) {
            jockeys++;
            z.discard();
         }
      }

      helper.assertTrue(jockeys > 5 && jockeys < 35, "about half of trident drowned should ride a zombie nautilus, got " + jockeys + "/40");
      ZombieNautilus zombie = (ZombieNautilus)helper.spawn((EntityType)ModEntities.ZOMBIE_NAUTILUS.get(), new BlockPos(5, 2, 5));
      helper.assertTrue(zombie.getMobType() == MobType.UNDEAD, "zombie nautilus should be undead");
      helper.succeed();
   }
}
