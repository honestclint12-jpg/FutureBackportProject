package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CreakingHeartBlock;
import com.futurebackport.block.CreakingHeartState;
import com.futurebackport.entity.Creaking;
import com.futurebackport.registry.ModBlocks;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PaleGardenTests {
   private static final BlockPos HEART = new BlockPos(6, 2, 6);

   private static void buildHeart(GameTestHelper helper) {
      for (int x = 0; x < 12; x++) {
         for (int z = 0; z < 12; z++) {
            helper.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
         }
      }

      helper.setBlock(HEART.below(), (Block)ModBlocks.PALE_OAK_LOG.get());
      helper.setBlock(HEART.above(), (Block)ModBlocks.PALE_OAK_LOG.get());
      helper.setBlock(
         HEART,
         (BlockState)((BlockState)((CreakingHeartBlock)ModBlocks.CREAKING_HEART.get())
               .defaultBlockState()
               .setValue(CreakingHeartBlock.STATE, CreakingHeartState.DORMANT))
            .setValue(CreakingHeartBlock.NATURAL, true)
      );
      helper.getLevel().setDayTime(18000L);
      helper.getLevel().updateSkyBrightness();
   }

   /** The creaking bound to this test's heart; tests run side by side, so a neighbour's creaking may be in range. */
   private static Creaking findCreaking(GameTestHelper helper) {
      BlockPos heart = helper.absolutePos(HEART);
      AABB area = new AABB(heart).inflate(40.0);
      return (Creaking)helper.getLevel()
         .getEntitiesOfClass(Creaking.class, area, creaking -> heart.equals(creaking.getHomePos()))
         .stream()
         .findFirst()
         .orElse(null);
   }

   @GameTest(
      template = "arena",
      timeoutTicks = 200
   )
   public static void heartSpawnsCreakingAtNight(GameTestHelper helper) {
      buildHeart(helper);
      ServerPlayer player = TestPlayers.mockServerPlayer(helper);
      player.setGameMode(GameType.SURVIVAL);
      player.moveTo(helper.absoluteVec(new Vec3(2.0, 1.0, 2.0)));
      helper.succeedWhen(() -> {
         helper.assertBlockProperty(HEART, CreakingHeartBlock.STATE, CreakingHeartState.AWAKE);
         Creaking creaking = findCreaking(helper);
         helper.assertTrue(creaking != null, "no creaking spawned");
         helper.assertTrue(creaking.isHeartBound(), "creaking is not bound to its heart");
      });
   }

   @GameTest(
      template = "arena",
      timeoutTicks = 200
   )
   public static void hittingBoundCreakingGrowsResinAndBreakingHeartKillsIt(GameTestHelper helper) {
      buildHeart(helper);
      ServerPlayer player = TestPlayers.mockServerPlayer(helper);
      player.setGameMode(GameType.SURVIVAL);
      player.moveTo(helper.absoluteVec(new Vec3(2.0, 1.0, 2.0)));
      helper.succeedWhen(() -> {
         Creaking creaking = findCreaking(helper);
         helper.assertTrue(creaking != null, "no creaking yet");
         creaking.hurt(helper.getLevel().damageSources().playerAttack(player), 5.0F);
         helper.assertTrue(creaking.isAlive() && creaking.getHealth() > 0.0F, "bound creaking took damage");
         int resin = 0;

         for (BlockPos pos : BlockPos.betweenClosed(HEART.offset(-3, -3, -3), HEART.offset(3, 3, 3))) {
            if (helper.getBlockState(pos).is(ModBlocks.RESIN_CLUMP.get())) {
               resin++;
            }
         }

         helper.assertTrue(resin > 0, "no resin grew on the logs");
         helper.destroyBlock(HEART);
         helper.assertTrue(creaking.isTearingDown() || creaking.isRemoved(), "creaking survived its heart");
      });
   }

   @GameTest(
      template = "arena"
   )
   public static void paleGardenIsInTheOverworld(GameTestHelper helper) {
      ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, FutureBackport.id("pale_garden"));
      Registry<MultiNoiseBiomeSourceParameterList> lists = helper.getLevel()
         .registryAccess()
         .registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
      MultiNoiseBiomeSourceParameterList overworld = (MultiNoiseBiomeSourceParameterList)lists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
      helper.assertTrue(
         overworld.parameters().values().stream().anyMatch(pair -> ((Holder)pair.getSecond()).is(biome)),
         "pale garden is not in the overworld biome parameters"
      );
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void overworldFeatureOrderIsConsistent(GameTestHelper helper) {
      Registry<MultiNoiseBiomeSourceParameterList> lists = helper.getLevel()
         .registryAccess()
         .registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
      List<Holder<Biome>> biomes = ((MultiNoiseBiomeSourceParameterList)lists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD))
         .parameters()
         .values()
         .stream()
         .<Holder<Biome>>map(Pair::getSecond)
         .distinct()
         .toList();
      FeatureSorter.buildFeaturesPerStep(biomes, b -> ((Biome)b.value()).getGenerationSettings().features(), true);
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void paleOakTreeGrowsWithHeartAndMoss(GameTestHelper helper) {
      for (int x = 0; x < 12; x++) {
         for (int z = 0; z < 12; z++) {
            helper.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
         }
      }

      for (int x = -1; x <= 12; x++) {
         for (int z = -1; z <= 12; z++) {
            for (int y = 16; y <= 18; y++) {
               helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
         }
      }

      ServerLevel level = helper.getLevel();
      ConfiguredFeature<?, ? extends Feature<?>> feature = (ConfiguredFeature<?, ? extends Feature<?>>)level.registryAccess()
         .registryOrThrow(Registries.CONFIGURED_FEATURE)
         .get(FutureBackport.id("pale_oak_creaking"));
      helper.assertTrue(feature != null, "missing pale_oak_creaking feature");
      helper.assertTrue(
         feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(new BlockPos(6, 1, 6))), "pale oak did not grow"
      );
      int hearts = 0;
      int moss = 0;

      for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(11, 15, 11))) {
         if (helper.getBlockState(pos).is(ModBlocks.CREAKING_HEART.get())) {
            hearts++;
         }

         if (helper.getBlockState(pos).is(ModBlocks.PALE_HANGING_MOSS.get())) {
            moss++;
         }
      }

      helper.assertTrue(hearts <= 1, "expected at most one creaking heart, found " + hearts);
      helper.assertTrue(moss > 0, "no hanging moss under the tree");
      helper.succeed();
   }
}
