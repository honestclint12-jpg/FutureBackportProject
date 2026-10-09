package com.futurebackport.client;

import com.futurebackport.platform.client.ClientRegistrars;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import com.futurebackport.platform.registry.BlockEntry;
import com.futurebackport.client.DryFoliageColor;
import com.futurebackport.client.NautilusScreen;
import com.futurebackport.client.model.BabyHumanoids;
import com.futurebackport.client.model.CopperGolemModel;
import com.futurebackport.client.model.CopperGolemStatueModel;
import com.futurebackport.client.model.CreakingModel;
import com.futurebackport.client.model.FarmAnimalModels;
import com.futurebackport.client.model.HappyGhastModels;
import com.futurebackport.client.model.NautilusModel;
import com.futurebackport.client.particle.FallingLeavesParticle;
import com.futurebackport.client.particle.FireflyParticle;
import com.futurebackport.client.particle.SimpleVerticalParticle;
import com.futurebackport.client.particle.TrailParticle;
import com.futurebackport.client.renderer.AnimalBabyRenderers26;
import com.futurebackport.client.renderer.BabyArmorLayer;
import com.futurebackport.client.renderer.CamelHuskRenderer;
import com.futurebackport.client.renderer.CopperChestRenderer;
import com.futurebackport.client.renderer.CopperGolemRenderer;
import com.futurebackport.client.renderer.CopperGolemStatueRenderer;
import com.futurebackport.client.renderer.CreakingRenderer;
import com.futurebackport.client.renderer.FarmAnimalRenderers;
import com.futurebackport.client.renderer.FelineRenderers26;
import com.futurebackport.client.renderer.HappyGhastRenderer;
import com.futurebackport.client.renderer.HumanoidBabyRenderers26;
import com.futurebackport.client.renderer.KeyframeBabyRenderers26;
import com.futurebackport.client.renderer.MountBabyRenderers26;
import com.futurebackport.client.renderer.NautilusRenderer;
import com.futurebackport.client.renderer.NetherBabyRenderers26;
import com.futurebackport.client.renderer.ParchedRenderer;
import com.futurebackport.client.renderer.PolarBearRenderer26;
import com.futurebackport.client.renderer.SheepRenderer26;
import com.futurebackport.client.renderer.ShelfRenderer;
import com.futurebackport.client.renderer.SmallBabyRenderers26;
import com.futurebackport.client.renderer.VillagerRenderer26;
import com.futurebackport.client.renderer.WolfRenderer26;
import com.futurebackport.registry.CopperFamily;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModMenus;
import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModWoodTypes;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.particle.FlameParticle.Provider;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Everything this mod registers on the client, grouped the way NeoForge's client events are. Each loader calls these
 * from its own events (NeoForge) or client entrypoint (Fabric).
 */
public final class ModClientSetup {
   private ModClientSetup() {
   }

   /** Wood types whose sign and hanging sign textures must be added to the sign atlases. */
   public static final WoodType[] SIGN_WOOD_TYPES = {ModWoodTypes.PALE_OAK};

   /** Which render layer each non-solid block uses. */
   public static void blockRenderLayers(BiConsumer<Block, RenderType> layer) {
      cutout(
         layer,
         ModBlocks.PALE_OAK_DOOR,
         ModBlocks.PALE_OAK_TRAPDOOR,
         ModBlocks.PALE_OAK_SAPLING,
         ModBlocks.POTTED_PALE_OAK_SAPLING,
         ModBlocks.PALE_MOSS_CARPET,
         ModBlocks.PALE_HANGING_MOSS,
         ModBlocks.OPEN_EYEBLOSSOM,
         ModBlocks.CLOSED_EYEBLOSSOM,
         ModBlocks.POTTED_OPEN_EYEBLOSSOM,
         ModBlocks.POTTED_CLOSED_EYEBLOSSOM,
         ModBlocks.RESIN_CLUMP,
         ModBlocks.LEAF_LITTER,
         ModBlocks.WILDFLOWERS,
         ModBlocks.BUSH,
         ModBlocks.FIREFLY_BUSH,
         ModBlocks.CACTUS_FLOWER,
         ModBlocks.SHORT_DRY_GRASS,
         ModBlocks.TALL_DRY_GRASS,
         ModBlocks.GOLDEN_DANDELION,
         ModBlocks.POTTED_GOLDEN_DANDELION
      );
      cutout(layer, ModBlocks.COPPER_TORCH, ModBlocks.COPPER_WALL_TORCH);
      cutout(layer, ModBlocks.DRIED_GHAST);

      for (CopperFamily family : List.of(ModBlocks.COPPER_BARS, ModBlocks.COPPER_CHAIN, ModBlocks.COPPER_LANTERN)) {
         family.all().forEach(b -> layer.accept(b.get(), RenderType.cutout()));
      }

      setLayer(layer, RenderType.cutoutMipped(), ModBlocks.PALE_OAK_LEAVES);
   }
   @SafeVarargs
   private static void cutout(BiConsumer<Block, RenderType> layer, BlockEntry<? extends Block>... blocks) {
      setLayer(layer, RenderType.cutout(), blocks);
   }

   @SafeVarargs
   private static void setLayer(BiConsumer<Block, RenderType> layer, RenderType type, BlockEntry<? extends Block>... blocks) {
      for (BlockEntry<? extends Block> block : blocks) {
         layer.accept(block.get(), type);
      }
   }

   public static void entityRenderers(ClientRegistrars.EntityRenderers entities, ClientRegistrars.BlockEntityRenderers blockEntities) {
      entities.register(ModEntities.CREAKING.get(), CreakingRenderer::new);
      blockEntities.register(ModBlockEntities.SHELF.get(), ShelfRenderer::new);
      blockEntities.register(ModBlockEntities.COPPER_CHEST.get(), CopperChestRenderer::new);
      blockEntities.register(ModBlockEntities.COPPER_GOLEM_STATUE.get(), CopperGolemStatueRenderer::new);
      entities.register(ModEntities.HAPPY_GHAST.get(), HappyGhastRenderer::new);
      entities.register(ModEntities.PARCHED.get(), ParchedRenderer::new);
      entities.register(ModEntities.CAMEL_HUSK.get(), CamelHuskRenderer::new);
      entities.register(ModEntities.COPPER_GOLEM.get(), CopperGolemRenderer::new);
      entities.register(EntityType.SHEEP, SheepRenderer26::new);
      entities.register(EntityType.WOLF, WolfRenderer26::new);
      entities.register(EntityType.CAT, FelineRenderers26.Cat26::new);
      entities.register(EntityType.VILLAGER, VillagerRenderer26::new);
      entities.register(EntityType.POLAR_BEAR, PolarBearRenderer26::new);
      entities.register(EntityType.STRIDER, NetherBabyRenderers26.Strider26::new);
      entities.register(EntityType.PANDA, AnimalBabyRenderers26.Panda26::new);
      entities.register(EntityType.HORSE, MountBabyRenderers26::horse);
      entities.register(EntityType.DONKEY, ctx -> new MountBabyRenderers26.Chested26(ctx, 0.87F, ModelLayers.DONKEY, "donkey_baby"));
      entities.register(EntityType.MULE, ctx -> new MountBabyRenderers26.Chested26(ctx, 0.92F, ModelLayers.MULE, "mule_baby"));
      entities.register(EntityType.SKELETON_HORSE, ctx -> new MountBabyRenderers26.Undead26(ctx, ModelLayers.SKELETON_HORSE, "horse_skeleton_baby"));
      entities.register(EntityType.ZOMBIE_HORSE, ctx -> new MountBabyRenderers26.Undead26(ctx, ModelLayers.ZOMBIE_HORSE, "horse_zombie_baby"));
      entities.register(EntityType.CAMEL, MountBabyRenderers26.Camel26::new);
      entities.register(EntityType.RABBIT, KeyframeBabyRenderers26.Rabbit26::new);
      entities.register(EntityType.ZOMBIE, HumanoidBabyRenderers26.Zombie26::new);
      entities.register(EntityType.HUSK, HumanoidBabyRenderers26.Husk26::new);
      entities.register(EntityType.DROWNED, HumanoidBabyRenderers26.Drowned26::new);
      entities.register(EntityType.ZOMBIE_VILLAGER, HumanoidBabyRenderers26.ZombieVillager26::new);
      entities.register(EntityType.PIGLIN, HumanoidBabyRenderers26.Piglin26::piglin);
      entities.register(EntityType.ZOMBIFIED_PIGLIN, HumanoidBabyRenderers26.Piglin26::zombified);
      entities.register(EntityType.AXOLOTL, KeyframeBabyRenderers26.Axolotl26::new);
      entities.register(EntityType.MOOSHROOM, MountBabyRenderers26.Mooshroom26::new);
      entities.register(EntityType.LLAMA, ctx -> new AnimalBabyRenderers26.Llama26(ctx, ModelLayers.LLAMA));
      entities.register(EntityType.TRADER_LLAMA, ctx -> new AnimalBabyRenderers26.Llama26(ctx, ModelLayers.TRADER_LLAMA));
      entities.register(EntityType.BEE, AnimalBabyRenderers26.Bee26::new);
      entities.register(EntityType.FOX, AnimalBabyRenderers26.Fox26::new);
      entities.register(EntityType.HOGLIN, NetherBabyRenderers26.Hoglin26::new);
      entities.register(EntityType.ZOGLIN, NetherBabyRenderers26.Zoglin26::new);
      entities.register(ModEntities.NAUTILUS.get(), NautilusRenderer::nautilus);
      entities.register(ModEntities.ZOMBIE_NAUTILUS.get(), NautilusRenderer::zombie);
      entities.register(EntityType.TURTLE, SmallBabyRenderers26.Turtle26::new);
      entities.register(EntityType.GOAT, SmallBabyRenderers26.Goat26::new);
      entities.register(EntityType.OCELOT, FelineRenderers26.Ocelot26::new);
      entities.register(EntityType.COW, FarmAnimalRenderers.CowRenderer::new);
      entities.register(EntityType.PIG, FarmAnimalRenderers.PigRenderer::new);
      entities.register(EntityType.CHICKEN, FarmAnimalRenderers.ChickenRenderer::new);
   }

   public static void layerDefinitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> layers) {
      layers.accept(CopperGolemStatueRenderer.RUNNING, CopperGolemStatueModel::createRunningPoseLayer);
      layers.accept(CopperGolemStatueRenderer.SITTING, CopperGolemStatueModel::createSittingPoseLayer);
      layers.accept(CopperGolemStatueRenderer.STAR, CopperGolemStatueModel::createStarPoseLayer);
      layers.accept(SmallBabyRenderers26.TURTLE_BABY, SmallBabyRenderers26.BabyTurtleModel::createBodyLayer);
      layers.accept(SmallBabyRenderers26.GOAT_BABY, SmallBabyRenderers26.BabyGoatModel::createBodyLayer);
      layers.accept(NautilusRenderer.ADULT, NautilusModel::createBodyLayer);
      layers.accept(NautilusRenderer.BABY, NautilusModel::createBabyLayer);
      layers.accept(NautilusRenderer.ARMOR, NautilusModel::createArmorLayer);
      layers.accept(NautilusRenderer.SADDLE, NautilusModel::createSaddleLayer);
      layers.accept(NautilusRenderer.CORAL, NautilusModel::createCoralLayer);
      layers.accept(HumanoidBabyRenderers26.ZOMBIE_BABY, () -> BabyHumanoids.zombieLayer(CubeDeformation.NONE));
      layers.accept(HumanoidBabyRenderers26.DROWNED_BABY_OUTER, () -> BabyHumanoids.zombieLayer(new CubeDeformation(0.25F)));
      layers.accept(HumanoidBabyRenderers26.ZOMBIE_VILLAGER_BABY, BabyHumanoids::zombieVillagerLayer);
      layers.accept(HumanoidBabyRenderers26.PIGLIN_BABY, BabyHumanoids::piglinLayer);
      layers.accept(BabyArmorLayer.HUMANOID_INNER, () -> BabyArmorLayer.createMesh(new CubeDeformation(-0.1F, 0.3F, 0.3F), PartPose.ZERO));
      layers.accept(BabyArmorLayer.HUMANOID_OUTER, () -> BabyArmorLayer.createMesh(new CubeDeformation(-0.1F, 0.5F, 0.3F), PartPose.ZERO));
      layers.accept(BabyArmorLayer.PIGLIN_INNER, () -> BabyArmorLayer.createMesh(new CubeDeformation(0.7F), PartPose.offset(0.5F, -0.5F, 0.0F)));
      layers.accept(BabyArmorLayer.PIGLIN_OUTER, () -> BabyArmorLayer.createMesh(new CubeDeformation(0.7F), PartPose.offset(0.5F, -0.5F, 0.0F)));
      layers.accept(KeyframeBabyRenderers26.RABBIT_BABY, KeyframeBabyRenderers26.BabyRabbitModel::createBodyLayer);
      layers.accept(KeyframeBabyRenderers26.AXOLOTL_BABY, KeyframeBabyRenderers26.BabyAxolotlModel::createBodyLayer);
      layers.accept(MountBabyRenderers26.HORSE_BABY, MountBabyRenderers26.BabyEquineModel::createHorseLayer);
      layers.accept(MountBabyRenderers26.DONKEY_BABY, MountBabyRenderers26.BabyEquineModel::createDonkeyLayer);
      layers.accept(MountBabyRenderers26.CAMEL_BABY, MountBabyRenderers26.BabyCamelModel::createBodyLayer);
      layers.accept(AnimalBabyRenderers26.PANDA_BABY, AnimalBabyRenderers26.BabyPandaModel::createBodyLayer);
      layers.accept(AnimalBabyRenderers26.LLAMA_BABY, () -> AnimalBabyRenderers26.BabyLlamaModel.createBodyLayer(CubeDeformation.NONE));
      layers.accept(
         AnimalBabyRenderers26.LLAMA_BABY_DECOR, () -> AnimalBabyRenderers26.BabyLlamaModel.createBodyLayer(new CubeDeformation(0.2F))
      );
      layers.accept(AnimalBabyRenderers26.BEE_BABY, AnimalBabyRenderers26.BabyBeeModel::createBodyLayer);
      layers.accept(AnimalBabyRenderers26.FOX_BABY, AnimalBabyRenderers26.BabyFoxModel::createBodyLayer);
      layers.accept(NetherBabyRenderers26.STRIDER_BABY, NetherBabyRenderers26.BabyStriderModel::createBodyLayer);
      layers.accept(NetherBabyRenderers26.HOGLIN_BABY, NetherBabyRenderers26.BabyHoglinModel::createBodyLayer);
      layers.accept(PolarBearRenderer26.BABY, PolarBearRenderer26.BabyPolarBearModel::createBodyLayer);
      layers.accept(VillagerRenderer26.BABY, VillagerRenderer26.BabyVillagerModel::createBodyLayer);
      layers.accept(FelineRenderers26.BABY, FelineRenderers26.BabyFelineModel::createBabyLayer);
      layers.accept(WolfRenderer26.BABY, WolfRenderer26.BabyWolfModel::createBodyLayer);
      layers.accept(SheepRenderer26.BABY, SheepRenderer26.BabySheepModel::createBodyLayer);
      layers.accept(CopperGolemRenderer.LAYER, CopperGolemModel::createBodyLayer);
      layers.accept(CreakingRenderer.LAYER, CreakingModel::createBodyLayer);
      layers.accept(CreakingRenderer.EYES_LAYER, CreakingModel::createEyesLayer);
      layers.accept(HappyGhastModels.GHAST, () -> HappyGhastModels.body(false));
      layers.accept(HappyGhastModels.BABY, () -> HappyGhastModels.body(true));
      layers.accept(HappyGhastModels.HARNESS, HappyGhastModels::harness);
      layers.accept(FarmAnimalModels.COW, FarmAnimalModels::cow);
      layers.accept(FarmAnimalModels.COW_COLD, FarmAnimalModels::coldCow);
      layers.accept(FarmAnimalModels.COW_WARM, FarmAnimalModels::warmCow);
      layers.accept(FarmAnimalModels.COW_BABY, FarmAnimalModels::babyCow);
      layers.accept(FarmAnimalModels.PIG, FarmAnimalModels::pig);
      layers.accept(FarmAnimalModels.PIG_COLD, FarmAnimalModels::coldPig);
      layers.accept(FarmAnimalModels.PIG_BABY, FarmAnimalModels::babyPig);
      layers.accept(FarmAnimalModels.PIG_SADDLE, FarmAnimalModels::pigSaddle);
      layers.accept(FarmAnimalModels.CHICKEN, FarmAnimalModels::chicken);
      layers.accept(FarmAnimalModels.CHICKEN_COLD, FarmAnimalModels::coldChicken);
      layers.accept(FarmAnimalModels.CHICKEN_BABY, FarmAnimalModels::babyChicken);
   }

   public static void particles(ClientRegistrars.Particles particles) {
      particles.register(ModParticles.PALE_OAK_LEAVES.get(), FallingLeavesParticle.PaleOakProvider::new);
      particles.register(ModParticles.TINTED_LEAVES.get(), FallingLeavesParticle.TintedProvider::new);
      particles.register(ModParticles.FIREFLY.get(), FireflyParticle.Provider::new);
      particles.register(ModParticles.TRAIL.get(), TrailParticle.Provider::new);
      particles.register(ModParticles.COPPER_FIRE_FLAME.get(), Provider::new);
      particles.register(ModParticles.PAUSE_MOB_GROWTH.get(), SimpleVerticalParticle.PauseProvider::new);
      particles.register(ModParticles.RESET_MOB_GROWTH.get(), SimpleVerticalParticle.ResetProvider::new);
   }

   public static void menuScreens(ClientRegistrars.MenuScreens screens) {
      screens.register(ModMenus.NAUTILUS.get(), NautilusScreen::new);
   }

   public static void blockColors(BiConsumer<BlockColor, Block[]> colors) {
      colors.accept(
         (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor(),
         new Block[]{(Block)ModBlocks.BUSH.get(), (Block)ModBlocks.WILDFLOWERS.get()}
      );
      colors.accept(
         (state, level, pos, tintIndex) -> level != null && pos != null ? level.getBlockTint(pos, DryFoliageColor.RESOLVER) : -10732494,
         new Block[]{(Block)ModBlocks.LEAF_LITTER.get()}
      );
   }

   public static void itemColors(BiConsumer<ItemColor, ItemLike[]> colors) {
      colors.accept((stack, tintIndex) -> 0xFF000000 | GrassColor.get(0.5, 1.0), new ItemLike[]{(ItemLike)ModBlocks.BUSH.get()});
   }

   /** Copper chests and copper golem statues render their items with their block entity renderers. */
   public static void itemRenderers(ClientRegistrars.ItemRenderers renderers) {
      renderers.register(
         CopperChestRenderer.ItemRenderer::new,
         ModBlocks.COPPER_CHEST.all().stream().map(b -> ((Block)b.get()).asItem()).toArray(Item[]::new)
      );
      renderers.register(
         CopperGolemStatueRenderer.ItemRenderer::new,
         ModBlocks.COPPER_GOLEM_STATUE.all().stream().map(b -> ((Block)b.get()).asItem()).toArray(Item[]::new)
      );
   }
}
