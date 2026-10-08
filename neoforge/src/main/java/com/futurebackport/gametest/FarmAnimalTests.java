package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.AgeLock;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.entity.SoundVariants;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("futurebackport")
@PrefixGameTestTemplate(false)
public class FarmAnimalTests {
   private static void floor(GameTestHelper helper) {
      for (int x = 0; x < 12; x++) {
         for (int z = 0; z < 12; z++) {
            helper.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
         }
      }
   }

   @GameTest(
      template = "arena"
   )
   public static void spawnedCowGetsBiomeVariant(GameTestHelper helper) {
      floor(helper);
      Cow cow = (Cow)EntityType.COW.spawn(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)), MobSpawnType.SPAWN_EGG);
      helper.assertTrue(cow != null && FarmAnimalVariant.get(cow) == FarmAnimalVariant.TEMPERATE, "spawned cow is not temperate");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void calfInheritsParentVariant(GameTestHelper helper) {
      floor(helper);
      Cow a = (Cow)helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
      Cow b = (Cow)helper.spawn(EntityType.COW, new BlockPos(4, 1, 3));
      FarmAnimalVariant.set(a, FarmAnimalVariant.COLD);
      FarmAnimalVariant.set(b, FarmAnimalVariant.COLD);
      a.spawnChildFromBreeding(helper.getLevel(), b);
      List<Cow> calves = helper.getLevel().getEntitiesOfClass(Cow.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0), AgeableMob::isBaby);
      helper.assertTrue(!calves.isEmpty(), "no calf was born");
      helper.assertTrue(FarmAnimalVariant.get((Entity)calves.get(0)) == FarmAnimalVariant.COLD, "calf did not inherit the cold variant");
      helper.succeed();
   }

   @GameTest(
      template = "arena",
      timeoutTicks = 120
   )
   public static void goldenDandelionKeepsBabiesYoung(GameTestHelper helper) {
      floor(helper);
      Cow calf = (Cow)helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
      calf.setAge(-24000);
      ServerPlayer player = helper.makeMockServerPlayerInLevel();
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)ModBlocks.GOLDEN_DANDELION.get(), 2));
      player.interactOn(calf, InteractionHand.MAIN_HAND);
      helper.assertTrue(AgeLock.LOCKED.get(calf), "calf was not age locked");
      calf.ageUp(30000);
      helper.runAfterDelay(5L, () -> {
         helper.assertTrue(calf.isBaby(), "age-locked calf grew up");
         helper.runAfterDelay(45L, () -> {
            player.interactOn(calf, InteractionHand.MAIN_HAND);
            helper.assertTrue(!AgeLock.LOCKED.get(calf), "second dandelion did not unlock");
            helper.succeed();
         });
      });
   }

   @GameTest(
      template = "arena"
   )
   public static void coldChickenLaysBlueEgg(GameTestHelper helper) {
      floor(helper);
      Chicken chicken = (Chicken)helper.spawn(EntityType.CHICKEN, new BlockPos(5, 1, 5));
      FarmAnimalVariant.set(chicken, FarmAnimalVariant.COLD);
      chicken.eggTime = 1;
      helper.succeedWhen(() -> {
         List<ItemEntity> eggs = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0));
         helper.assertTrue(eggs.stream().anyMatch(e -> e.getItem().is((Item)ModItems.BLUE_EGG.get())), "no blue egg laid");
      });
   }

   @GameTest(
      template = "arena"
   )
   public static void soundVariantsSwapSounds(GameTestHelper helper) {
      Cow cow = (Cow)helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
      Chicken chicken = (Chicken)helper.spawn(EntityType.CHICKEN, new BlockPos(5, 1, 3));
      MushroomCow mooshroom = (MushroomCow)helper.spawn(EntityType.MOOSHROOM, new BlockPos(7, 1, 3));
      Wolf wolf = (Wolf)helper.spawn(EntityType.WOLF, new BlockPos(9, 1, 3));
      helper.assertTrue(SoundVariants.swap(cow, SoundEvents.COW_AMBIENT) == SoundEvents.COW_AMBIENT, "classic cow changed");
      SoundVariants.set(cow, "moody");
      SoundVariants.set(chicken, "picky");
      SoundVariants.set(mooshroom, "moody");
      SoundVariants.set(wolf, "puglin");
      helper.assertTrue(SoundVariants.swap(cow, SoundEvents.COW_AMBIENT).getLocation().equals(FutureBackport.id("entity.cow_moody.ambient")), "moody cow");
      helper.assertTrue(
         SoundVariants.swap(chicken, SoundEvents.CHICKEN_HURT).getLocation().equals(FutureBackport.id("entity.chicken_picky.hurt")), "picky chicken"
      );
      helper.assertTrue(SoundVariants.swap(chicken, SoundEvents.CHICKEN_STEP) == SoundEvents.CHICKEN_STEP, "picky chicken kept the classic step in 26.x");
      helper.assertTrue(SoundVariants.swap(mooshroom, SoundEvents.COW_AMBIENT) == SoundEvents.COW_AMBIENT, "mooshrooms have no sound variants");
      helper.assertTrue(SoundVariants.swap(wolf, SoundEvents.WOLF_WHINE).getLocation().equals(FutureBackport.id("entity.wolf_puglin.whine")), "puglin wolf");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void babiesUseBabySounds(GameTestHelper helper) {
      Pig piglet = (Pig)helper.spawn(EntityType.PIG, new BlockPos(3, 1, 3));
      piglet.setBaby(true);
      SoundVariants.set(piglet, "mini");
      Horse foal = (Horse)helper.spawn(EntityType.HORSE, new BlockPos(6, 1, 3));
      foal.setBaby(true);
      Pig adultPig = (Pig)helper.spawn(EntityType.PIG, new BlockPos(9, 1, 3));
      helper.assertTrue(
         SoundVariants.swap(piglet, SoundEvents.PIG_AMBIENT).getLocation().equals(FutureBackport.id("entity.baby_pig.ambient")),
         "piglets use the baby pig sounds, even mini ones"
      );
      helper.assertTrue(
         SoundVariants.swap(foal, SoundEvents.HORSE_BREATHE).getLocation().equals(FutureBackport.id("entity.baby_horse.breathe")), "foals breathe like foals"
      );
      helper.assertTrue(SoundVariants.swap(adultPig, SoundEvents.PIG_AMBIENT) == SoundEvents.PIG_AMBIENT, "adult classic pigs unchanged");
      helper.succeed();
   }
}
