package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CactusFlowerBlock;
import com.futurebackport.block.CopperChestBlock;
import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.block.CopperTorchBlock;
import com.futurebackport.block.CreakingHeartBlock;
import com.futurebackport.block.DriedGhastBlock;
import com.futurebackport.block.DryVegetationBlock;
import com.futurebackport.block.EyeblossomBlock;
import com.futurebackport.block.FeaturePlacerBlock;
import com.futurebackport.block.FireflyBushBlock;
import com.futurebackport.block.HangingMossBlock;
import com.futurebackport.block.LeafLitterBlock;
import com.futurebackport.block.MossyCarpetBlock;
import com.futurebackport.block.ParticleLeavesBlock;
import com.futurebackport.block.PottedEyeblossomBlock;
import com.futurebackport.block.ResinClumpBlock;
import com.futurebackport.block.ShelfBlock;
import com.futurebackport.block.SpreadingBushBlock;
import com.futurebackport.block.WeatheringCopperBarsBlock;
import com.futurebackport.block.WeatheringCopperChainBlock;
import com.futurebackport.block.WeatheringCopperLanternBlock;
import com.futurebackport.block.WeatheringLightningRodBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("futurebackport");
   private static final MapColor PALE_OAK_PLANKS_COLOR = MapColor.QUARTZ;
   private static final MapColor PALE_OAK_BARK_COLOR = MapColor.STONE;
   public static final DeferredBlock<Block> PALE_OAK_PLANKS = BLOCKS.registerSimpleBlock(
      "pale_oak_planks", Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_PLANKS).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<RotatedPillarBlock> PALE_OAK_LOG = BLOCKS.registerBlock(
      "pale_oak_log",
      RotatedPillarBlock::new,
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LOG)
         .mapColor(s -> s.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? PALE_OAK_PLANKS_COLOR : PALE_OAK_BARK_COLOR)
   );
   public static final DeferredBlock<RotatedPillarBlock> PALE_OAK_WOOD = BLOCKS.registerBlock(
      "pale_oak_wood", RotatedPillarBlock::new, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_WOOD).mapColor(PALE_OAK_BARK_COLOR)
   );
   public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PALE_OAK_LOG = BLOCKS.registerBlock(
      "stripped_pale_oak_log",
      RotatedPillarBlock::new,
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STRIPPED_OAK_LOG).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<RotatedPillarBlock> STRIPPED_PALE_OAK_WOOD = BLOCKS.registerBlock(
      "stripped_pale_oak_wood",
      RotatedPillarBlock::new,
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STRIPPED_OAK_WOOD).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<StairBlock> PALE_OAK_STAIRS = BLOCKS.register(
      "pale_oak_stairs", () -> new StairBlock(((Block)PALE_OAK_PLANKS.get()).defaultBlockState(), Properties.ofFullCopy((BlockBehaviour)PALE_OAK_PLANKS.get()))
   );
   public static final DeferredBlock<SlabBlock> PALE_OAK_SLAB = BLOCKS.registerBlock(
      "pale_oak_slab", SlabBlock::new, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_SLAB).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<FenceBlock> PALE_OAK_FENCE = BLOCKS.registerBlock(
      "pale_oak_fence", FenceBlock::new, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_FENCE).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<FenceGateBlock> PALE_OAK_FENCE_GATE = BLOCKS.registerBlock(
      "pale_oak_fence_gate",
      p -> new FenceGateBlock(ModWoodTypes.PALE_OAK, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_FENCE_GATE).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<DoorBlock> PALE_OAK_DOOR = BLOCKS.registerBlock(
      "pale_oak_door",
      p -> new DoorBlock(ModWoodTypes.PALE_OAK_SET, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_DOOR).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<TrapDoorBlock> PALE_OAK_TRAPDOOR = BLOCKS.registerBlock(
      "pale_oak_trapdoor",
      p -> new TrapDoorBlock(ModWoodTypes.PALE_OAK_SET, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_TRAPDOOR).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<ButtonBlock> PALE_OAK_BUTTON = BLOCKS.registerBlock(
      "pale_oak_button", p -> new ButtonBlock(ModWoodTypes.PALE_OAK_SET, 30, p), Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_BUTTON)
   );
   public static final DeferredBlock<PressurePlateBlock> PALE_OAK_PRESSURE_PLATE = BLOCKS.registerBlock(
      "pale_oak_pressure_plate",
      p -> new PressurePlateBlock(ModWoodTypes.PALE_OAK_SET, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_PRESSURE_PLATE).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<StandingSignBlock> PALE_OAK_SIGN = BLOCKS.registerBlock(
      "pale_oak_sign",
      p -> new StandingSignBlock(ModWoodTypes.PALE_OAK, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_SIGN).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<WallSignBlock> PALE_OAK_WALL_SIGN = BLOCKS.register(
      "pale_oak_wall_sign",
      () -> new WallSignBlock(
         ModWoodTypes.PALE_OAK,
         Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_WALL_SIGN).mapColor(PALE_OAK_PLANKS_COLOR).dropsLike((Block)PALE_OAK_SIGN.get())
      )
   );
   public static final DeferredBlock<CeilingHangingSignBlock> PALE_OAK_HANGING_SIGN = BLOCKS.registerBlock(
      "pale_oak_hanging_sign",
      p -> new CeilingHangingSignBlock(ModWoodTypes.PALE_OAK, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_HANGING_SIGN).mapColor(PALE_OAK_PLANKS_COLOR)
   );
   public static final DeferredBlock<WallHangingSignBlock> PALE_OAK_WALL_HANGING_SIGN = BLOCKS.register(
      "pale_oak_wall_hanging_sign",
      () -> new WallHangingSignBlock(
         ModWoodTypes.PALE_OAK,
         Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_WALL_HANGING_SIGN)
            .mapColor(PALE_OAK_PLANKS_COLOR)
            .dropsLike((Block)PALE_OAK_HANGING_SIGN.get())
      )
   );
   public static final DeferredBlock<ParticleLeavesBlock> PALE_OAK_LEAVES = BLOCKS.registerBlock(
      "pale_oak_leaves",
      p -> new ParticleLeavesBlock(0.02F, ModParticles.PALE_OAK_LEAVES, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DARK_OAK_LEAVES).mapColor(MapColor.METAL)
   );
   public static final DeferredBlock<SaplingBlock> PALE_OAK_SAPLING = BLOCKS.registerBlock(
      "pale_oak_sapling",
      p -> new SaplingBlock(ModTreeGrowers.PALE_OAK, p),
      Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DARK_OAK_SAPLING).mapColor(MapColor.METAL)
   );
   public static final DeferredBlock<FlowerPotBlock> POTTED_PALE_OAK_SAPLING = BLOCKS.register(
      "potted_pale_oak_sapling",
      () -> new FlowerPotBlock(
         () -> (FlowerPotBlock)net.minecraft.world.level.block.Blocks.FLOWER_POT,
         PALE_OAK_SAPLING,
         Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POTTED_DARK_OAK_SAPLING)
      )
   );
   public static final DeferredBlock<FeaturePlacerBlock> PALE_MOSS_BLOCK = BLOCKS.registerBlock(
      "pale_moss_block",
      p -> new FeaturePlacerBlock(ResourceKey.create(Registries.CONFIGURED_FEATURE, FutureBackport.id("pale_moss_patch_bonemeal")), p),
      Properties.of().ignitedByLava().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundType.MOSS).pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<MossyCarpetBlock> PALE_MOSS_CARPET = BLOCKS.registerBlock(
      "pale_moss_carpet",
      MossyCarpetBlock::new,
      Properties.of()
         .ignitedByLava()
         .mapColor(MapColor.COLOR_LIGHT_GRAY)
         .strength(0.1F)
         .sound(SoundType.MOSS_CARPET)
         .pushReaction(PushReaction.DESTROY)
         .noOcclusion()
   );
   public static final DeferredBlock<HangingMossBlock> PALE_HANGING_MOSS = BLOCKS.registerBlock(
      "pale_hanging_moss",
      HangingMossBlock::new,
      Properties.of().ignitedByLava().mapColor(MapColor.COLOR_LIGHT_GRAY).noCollission().sound(SoundType.MOSS_CARPET).pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<EyeblossomBlock> OPEN_EYEBLOSSOM = BLOCKS.registerBlock(
      "open_eyeblossom", p -> new EyeblossomBlock(EyeblossomBlock.Type.OPEN, p), eyeblossomProperties(MapColor.COLOR_ORANGE)
   );
   public static final DeferredBlock<EyeblossomBlock> CLOSED_EYEBLOSSOM = BLOCKS.registerBlock(
      "closed_eyeblossom", p -> new EyeblossomBlock(EyeblossomBlock.Type.CLOSED, p), eyeblossomProperties(MapColor.METAL)
   );
   public static final DeferredBlock<PottedEyeblossomBlock> POTTED_OPEN_EYEBLOSSOM = BLOCKS.register(
      "potted_open_eyeblossom",
      () -> new PottedEyeblossomBlock(
         true, OPEN_EYEBLOSSOM, ModBlocks.POTTED_CLOSED_EYEBLOSSOM, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POTTED_POPPY).randomTicks()
      )
   );
   public static final DeferredBlock<PottedEyeblossomBlock> POTTED_CLOSED_EYEBLOSSOM = BLOCKS.register(
      "potted_closed_eyeblossom",
      () -> new PottedEyeblossomBlock(
         false, CLOSED_EYEBLOSSOM, POTTED_OPEN_EYEBLOSSOM, Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POTTED_POPPY).randomTicks()
      )
   );
   public static final DeferredBlock<CreakingHeartBlock> CREAKING_HEART = BLOCKS.registerBlock(
      "creaking_heart",
      CreakingHeartBlock::new,
      Properties.of().mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(10.0F).sound(ModSounds.CREAKING_HEART)
   );
   public static final DeferredBlock<ResinClumpBlock> RESIN_CLUMP = BLOCKS.registerBlock(
      "resin_clump",
      ResinClumpBlock::new,
      Properties.of()
         .mapColor(MapColor.TERRACOTTA_ORANGE)
         .replaceable()
         .noCollission()
         .sound(ModSounds.RESIN)
         .ignitedByLava()
         .pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<Block> RESIN_BLOCK = BLOCKS.registerSimpleBlock(
      "resin_block", Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).sound(ModSounds.RESIN)
   );
   public static final DeferredBlock<Block> RESIN_BRICKS = BLOCKS.registerSimpleBlock("resin_bricks", resinBrickProperties());
   public static final DeferredBlock<StairBlock> RESIN_BRICK_STAIRS = BLOCKS.register(
      "resin_brick_stairs", () -> new StairBlock(((Block)RESIN_BRICKS.get()).defaultBlockState(), resinBrickProperties())
   );
   public static final DeferredBlock<SlabBlock> RESIN_BRICK_SLAB = BLOCKS.registerBlock("resin_brick_slab", SlabBlock::new, resinBrickProperties());
   public static final DeferredBlock<WallBlock> RESIN_BRICK_WALL = BLOCKS.registerBlock(
      "resin_brick_wall", WallBlock::new, resinBrickProperties().forceSolidOn()
   );
   public static final DeferredBlock<Block> CHISELED_RESIN_BRICKS = BLOCKS.registerSimpleBlock("chiseled_resin_bricks", resinBrickProperties());
   public static final DeferredBlock<LeafLitterBlock> LEAF_LITTER = BLOCKS.registerBlock(
      "leaf_litter",
      LeafLitterBlock::new,
      Properties.of().mapColor(MapColor.COLOR_BROWN).replaceable().noCollission().sound(ModSounds.LEAF_LITTER).pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<PinkPetalsBlock> WILDFLOWERS = BLOCKS.registerBlock(
      "wildflowers",
      PinkPetalsBlock::new,
      Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<SpreadingBushBlock> BUSH = BLOCKS.registerBlock(
      "bush", SpreadingBushBlock::new, plantProperties(MapColor.PLANT).replaceable()
   );
   public static final DeferredBlock<FireflyBushBlock> FIREFLY_BUSH = BLOCKS.registerBlock(
      "firefly_bush", FireflyBushBlock::new, plantProperties(MapColor.PLANT).lightLevel(s -> 2).sound(SoundType.SWEET_BERRY_BUSH)
   );
   public static final DeferredBlock<CactusFlowerBlock> CACTUS_FLOWER = BLOCKS.registerBlock(
      "cactus_flower", CactusFlowerBlock::new, plantProperties(MapColor.COLOR_PINK).sound(ModSounds.CACTUS_FLOWER)
   );
   public static final DeferredBlock<DryVegetationBlock> SHORT_DRY_GRASS = BLOCKS.register(
      "short_dry_grass",
      () -> new DryVegetationBlock(false, ModBlocks.TALL_DRY_GRASS, plantProperties(MapColor.COLOR_YELLOW).replaceable().offsetType(OffsetType.XYZ))
   );
   public static final DeferredBlock<DryVegetationBlock> TALL_DRY_GRASS = BLOCKS.register(
      "tall_dry_grass", () -> new DryVegetationBlock(true, SHORT_DRY_GRASS, plantProperties(MapColor.COLOR_YELLOW).replaceable().offsetType(OffsetType.XYZ))
   );
   public static final DeferredBlock<FlowerBlock> GOLDEN_DANDELION = BLOCKS.registerBlock(
      "golden_dandelion", p -> new FlowerBlock(MobEffects.SATURATION, 0.35F, p), Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.DANDELION)
   );
   public static final DeferredBlock<FlowerPotBlock> POTTED_GOLDEN_DANDELION = BLOCKS.register(
      "potted_golden_dandelion",
      () -> new FlowerPotBlock(
         () -> (FlowerPotBlock)net.minecraft.world.level.block.Blocks.FLOWER_POT,
         GOLDEN_DANDELION,
         Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.POTTED_DANDELION)
      )
   );
   public static final DeferredBlock<CopperTorchBlock> COPPER_TORCH = BLOCKS.registerBlock(
      "copper_torch",
      CopperTorchBlock::new,
      Properties.of().noCollission().instabreak().lightLevel(s -> 14).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY)
   );
   public static final DeferredBlock<CopperTorchBlock.Wall> COPPER_WALL_TORCH = BLOCKS.register(
      "copper_wall_torch",
      () -> new CopperTorchBlock.Wall(
         Properties.of()
            .noCollission()
            .instabreak()
            .lightLevel(s -> 14)
            .sound(SoundType.WOOD)
            .pushReaction(PushReaction.DESTROY)
            .dropsLike((Block)COPPER_TORCH.get())
      )
   );
   public static final CopperFamily COPPER_BARS = CopperFamily.register(
      "copper_bars",
      WeatheringCopperBarsBlock::new,
      (s, p) -> new IronBarsBlock(p),
      s -> Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.COPPER).noOcclusion()
   );
   public static final CopperFamily COPPER_CHAIN = CopperFamily.register(
      "copper_chain",
      WeatheringCopperChainBlock::new,
      (s, p) -> new ChainBlock(p),
      s -> Properties.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion()
   );
   public static final CopperFamily COPPER_LANTERN = CopperFamily.register(
      "copper_lantern",
      WeatheringCopperLanternBlock::new,
      (s, p) -> new LanternBlock(p),
      s -> Properties.of()
         .mapColor(MapColor.METAL)
         .forceSolidOn()
         .strength(3.5F)
         .sound(SoundType.LANTERN)
         .lightLevel(l -> 15)
         .noOcclusion()
         .pushReaction(PushReaction.DESTROY)
   );
   public static final CopperFamily LIGHTNING_ROD = CopperFamily.register(
      "lightning_rod",
      WeatheringLightningRodBlock::new,
      (s, p) -> new LightningRodBlock(p),
      s -> Properties.of()
         .mapColor(CopperFamily.copperColor(s))
         .forceSolidOn()
         .requiresCorrectToolForDrops()
         .strength(3.0F, 6.0F)
         .sound(SoundType.COPPER)
         .noOcclusion()
   );
   public static final DeferredBlock<DriedGhastBlock> DRIED_GHAST = BLOCKS.registerBlock(
      "dried_ghast",
      DriedGhastBlock::new,
      Properties.of().mapColor(MapColor.COLOR_GRAY).forceSolidOn().instabreak().sound(ModSounds.DRIED_GHAST).noOcclusion().randomTicks()
   );
   public static final Map<String, DeferredBlock<ShelfBlock>> SHELVES = new LinkedHashMap<>();
   public static final StoneFamily SULFUR = StoneFamily.register("sulfur", "sulfur", () -> stoneProperties(MapColor.COLOR_YELLOW, ModSounds.SULFUR));
   public static final StoneFamily POLISHED_SULFUR = StoneFamily.register(
      "polished_sulfur", "polished_sulfur", () -> Properties.ofLegacyCopy((BlockBehaviour)SULFUR.base().get())
   );
   public static final StoneFamily SULFUR_BRICKS = StoneFamily.register(
      "sulfur_bricks", "sulfur_brick", () -> Properties.ofLegacyCopy((BlockBehaviour)SULFUR.base().get())
   );
   public static final DeferredBlock<Block> CHISELED_SULFUR = BLOCKS.register(
      "chiseled_sulfur", () -> new Block(Properties.ofLegacyCopy((BlockBehaviour)SULFUR.base().get()))
   );
   public static final StoneFamily CINNABAR = StoneFamily.register("cinnabar", "cinnabar", () -> stoneProperties(MapColor.COLOR_RED, ModSounds.CINNABAR));
   public static final StoneFamily POLISHED_CINNABAR = StoneFamily.register(
      "polished_cinnabar", "polished_cinnabar", () -> Properties.ofLegacyCopy((BlockBehaviour)CINNABAR.base().get())
   );
   public static final StoneFamily CINNABAR_BRICKS = StoneFamily.register(
      "cinnabar_bricks", "cinnabar_brick", () -> Properties.ofLegacyCopy((BlockBehaviour)CINNABAR.base().get())
   );
   public static final DeferredBlock<Block> CHISELED_CINNABAR = BLOCKS.register(
      "chiseled_cinnabar", () -> new Block(Properties.ofLegacyCopy((BlockBehaviour)CINNABAR.base().get()))
   );
   public static final CopperFamily COPPER_CHEST = CopperFamily.register(
      "copper_chest",
      CopperChestBlock.Weathering::new,
      CopperChestBlock::new,
      s -> Properties.of().mapColor(CopperFamily.copperColor(s)).strength(3.0F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops()
   );
   public static final CopperFamily COPPER_GOLEM_STATUE = CopperFamily.register(
      "copper_golem_statue",
      CopperGolemStatueBlock.Weathering::new,
      CopperGolemStatueBlock::new,
      s -> Properties.of()
         .mapColor(CopperFamily.copperColor(s))
         .strength(3.0F, 6.0F)
         .sound(ModSounds.COPPER_GOLEM_STATUE)
         .pushReaction(PushReaction.DESTROY)
         .noOcclusion()
   );

   private static void shelf(String wood, Supplier<MapColor> color) {
      boolean nether = wood.equals("crimson") || wood.equals("warped");
      SHELVES.put(wood, BLOCKS.register(wood + "_shelf", () -> {
         Properties properties = Properties.of().mapColor(color.get()).instrument(NoteBlockInstrument.BASS).sound(ModSounds.SHELF).strength(2.0F, 3.0F);
         return new ShelfBlock(nether ? properties : properties.ignitedByLava());
      }));
   }

   private static Properties stoneProperties(MapColor color, SoundType sound) {
      return Properties.of().sound(sound).mapColor(color).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F);
   }

   private static Properties eyeblossomProperties(MapColor color) {
      return Properties.of()
         .mapColor(color)
         .noCollission()
         .instabreak()
         .sound(SoundType.GRASS)
         .offsetType(OffsetType.XZ)
         .pushReaction(PushReaction.DESTROY)
         .randomTicks();
   }

   private static Properties resinBrickProperties() {
      return Properties.of()
         .mapColor(MapColor.TERRACOTTA_ORANGE)
         .instrument(NoteBlockInstrument.BASEDRUM)
         .requiresCorrectToolForDrops()
         .sound(ModSounds.RESIN_BRICKS)
         .strength(1.5F, 6.0F);
   }

   private static Properties plantProperties(MapColor color) {
      return Properties.of().mapColor(color).noCollission().instabreak().sound(SoundType.GRASS).ignitedByLava().pushReaction(PushReaction.DESTROY);
   }

   static {
      shelf("oak", () -> net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultMapColor());
      shelf("spruce", () -> net.minecraft.world.level.block.Blocks.SPRUCE_LOG.defaultMapColor());
      shelf("birch", () -> net.minecraft.world.level.block.Blocks.BIRCH_PLANKS.defaultMapColor());
      shelf("jungle", () -> net.minecraft.world.level.block.Blocks.JUNGLE_LOG.defaultMapColor());
      shelf("acacia", () -> net.minecraft.world.level.block.Blocks.ACACIA_PLANKS.defaultMapColor());
      shelf("dark_oak", () -> net.minecraft.world.level.block.Blocks.DARK_OAK_LOG.defaultMapColor());
      shelf("mangrove", () -> net.minecraft.world.level.block.Blocks.MANGROVE_LOG.defaultMapColor());
      shelf("cherry", () -> net.minecraft.world.level.block.Blocks.CHERRY_PLANKS.defaultMapColor());
      shelf("pale_oak", () -> PALE_OAK_PLANKS_COLOR);
      shelf("bamboo", () -> net.minecraft.world.level.block.Blocks.BAMBOO_PLANKS.defaultMapColor());
      shelf("crimson", () -> MapColor.CRIMSON_STEM);
      shelf("warped", () -> MapColor.WARPED_STEM);
   }
}
