package com.futurebackport.gametest;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.CamelHusk;
import com.futurebackport.entity.CamelHuskSpawning;
import com.futurebackport.entity.Parched;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("futurebackport")
@PrefixGameTestTemplate(false)
public class WorldgenTests {
   private static void grassFloor(GameTestHelper helper) {
      for (int x = 0; x < 12; x++) {
         for (int z = 0; z < 12; z++) {
            helper.setBlock(new BlockPos(x, 0, z), Blocks.GRASS_BLOCK);
         }
      }
   }

   private static boolean placeConfigured(GameTestHelper helper, ResourceLocation id, BlockPos pos) {
      ServerLevel level = helper.getLevel();
      ConfiguredFeature<?, ?> feature = (ConfiguredFeature<?, ?>)level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(id);
      helper.assertTrue(feature != null, "missing configured feature " + id);
      return feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(pos));
   }

   private static int count(GameTestHelper helper, Block block) {
      int n = 0;

      for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(11, 15, 11))) {
         if (helper.getBlockState(pos).is(block)) {
            n++;
         }
      }

      return n;
   }

   @GameTest(
      template = "arena"
   )
   public static void fallenTreeLiesDown(GameTestHelper helper) {
      grassFloor(helper);

      for (int attempt = 0; attempt < 8 && count(helper, Blocks.OAK_LOG) < 2; attempt++) {
         for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(11, 4, 11))) {
            helper.setBlock(pos.immutable(), Blocks.AIR);
         }

         placeConfigured(helper, FutureBackport.id("fallen_oak_tree"), new BlockPos(6, 1, 6));
      }

      helper.assertTrue(count(helper, Blocks.OAK_LOG) >= 2, "fallen oak tree placed " + count(helper, Blocks.OAK_LOG) + " logs");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void oakTreeDropsLeafLitter(GameTestHelper helper) {
      grassFloor(helper);

      for (int x = -1; x <= 12; x++) {
         for (int z = -1; z <= 12; z++) {
            for (int y = 16; y <= 18; y++) {
               helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
         }
      }

      helper.assertTrue(placeConfigured(helper, FutureBackport.id("oak_leaf_litter"), new BlockPos(6, 1, 6)), "oak tree did not grow");
      helper.assertTrue(count(helper, (Block)ModBlocks.LEAF_LITTER.get()) > 0, "no leaf litter around the tree");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void bushPatchGenerates(GameTestHelper helper) {
      grassFloor(helper);
      ServerLevel level = helper.getLevel();
      PlacedFeature patch = (PlacedFeature)level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).get(FutureBackport.id("patch_bush"));
      helper.assertTrue(patch != null, "missing placed feature patch_bush");
      ((ConfiguredFeature)patch.feature().value())
         .place(level, level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(new BlockPos(6, 1, 6)));
      helper.assertTrue(count(helper, (Block)ModBlocks.BUSH.get()) > 0, "bush patch placed nothing");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void parchedSpawnsInDesertsAndResistsWeakness(GameTestHelper helper) {
      Biome desert = (Biome)helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.DESERT);
      List<SpawnerData> monsters = desert.getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
      int parched = 0;
      int skeleton = 0;

      for (SpawnerData spawn : monsters) {
         if (spawn.type == ModEntities.PARCHED.get()) {
            parched += spawn.getWeight().asInt();
         }

         if (spawn.type == EntityType.SKELETON) {
            skeleton += spawn.getWeight().asInt();
         }
      }

      helper.assertTrue(parched == 50 && skeleton == 50, "desert spawn weights: parched " + parched + ", skeleton " + skeleton);
      Parched mob = (Parched)helper.spawn((EntityType)ModEntities.PARCHED.get(), new BlockPos(3, 1, 3));
      helper.assertTrue(!mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100)), "parched accepted weakness");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void huskRidesInOnCamelHusk(GameTestHelper helper) {
      Husk husk = (Husk)helper.spawn(EntityType.HUSK, new BlockPos(5, 1, 5));
      ServerLevel level = helper.getLevel();
      CamelHusk camel = CamelHuskSpawning.mount(husk, level, level.getCurrentDifficultyAt(husk.blockPosition()), MobSpawnType.NATURAL);
      helper.assertTrue(camel != null && husk.getVehicle() == camel, "husk is not riding a camel husk");
      helper.assertTrue(camel.getPassengers().size() == 2 && camel.getPassengers().get(1) instanceof Parched, "no parched behind the husk");
      helper.assertTrue(camel.getControllingPassenger() == husk, "husk does not steer the camel husk");
      helper.assertTrue(camel.isFood(new ItemStack(Items.RABBIT_FOOT)) && !camel.isFood(new ItemStack(Items.CACTUS)), "wrong food");
      helper.assertTrue(!camel.canFallInLove(), "camel husk can breed");
      helper.assertTrue(camel.getType().is(EntityTypeTags.UNDEAD) && ((EntityType)ModEntities.PARCHED.get()).is(EntityTypeTags.UNDEAD), "not undead");
      helper.succeed();
   }

   @GameTest(
      template = "arena"
   )
   public static void biomeModifiersApply(GameTestHelper helper) {
      Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
      Registry<PlacedFeature> placed = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
      PlacedFeature bush = (PlacedFeature)placed.get(FutureBackport.id("patch_bush"));
      PlacedFeature wildflowers = (PlacedFeature)placed.get(FutureBackport.id("wildflowers_meadow"));
      helper.assertTrue(
         ((Biome)biomes.getOrThrow(Biomes.PLAINS)).getGenerationSettings().features().stream().anyMatch(set -> set.stream().anyMatch(h -> h.value() == bush)),
         "plains has no bushes"
      );
      helper.assertTrue(
         ((Biome)biomes.getOrThrow(Biomes.MEADOW))
            .getGenerationSettings()
            .features()
            .stream()
            .anyMatch(set -> set.stream().anyMatch(h -> h.value() == wildflowers)),
         "meadow has no wildflowers"
      );
      Reference<PlacedFeature> oldGrass = placed.getHolderOrThrow(
         ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.withDefaultNamespace("patch_grass_plain"))
      );
      helper.assertTrue(
         ((Biome)biomes.getOrThrow(Biomes.MEADOW)).getGenerationSettings().features().stream().noneMatch(set -> set.contains(oldGrass)),
         "meadow still has the old grass patch"
      );
      helper.succeed();
   }
}
