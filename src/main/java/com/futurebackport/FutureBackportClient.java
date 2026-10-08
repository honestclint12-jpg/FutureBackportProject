package com.futurebackport;

import com.futurebackport.client.DryFoliageColor;
import com.futurebackport.client.NautilusScreen;
import com.futurebackport.client.dev.Showcase;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ColorResolvers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@Mod(
   value = "futurebackport",
   dist = {Dist.CLIENT}
)
@EventBusSubscriber(
   modid = "futurebackport",
   value = {Dist.CLIENT}
)
public class FutureBackportClient {
   public FutureBackportClient() {
      Showcase.register();
   }

   @SubscribeEvent
   static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(
         () -> {
            Sheets.addWoodType(ModWoodTypes.PALE_OAK);
            cutout(
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
            cutout(ModBlocks.COPPER_TORCH, ModBlocks.COPPER_WALL_TORCH);
            cutout(ModBlocks.DRIED_GHAST);

            for (CopperFamily family : List.of(ModBlocks.COPPER_BARS, ModBlocks.COPPER_CHAIN, ModBlocks.COPPER_LANTERN)) {
               family.all().forEach(FutureBackportClient::cutout);
            }

            setLayer(RenderType.cutoutMipped(), ModBlocks.PALE_OAK_LEAVES);
         }
      );
   }

   @SafeVarargs
   private static void cutout(DeferredBlock<? extends Block>... blocks) {
      setLayer(RenderType.cutout(), blocks);
   }

   private static void cutout(DeferredBlock<? extends Block> block) {
      setLayer(RenderType.cutout(), block);
   }

   @SafeVarargs
   private static void setLayer(RenderType type, DeferredBlock<? extends Block>... blocks) {
      for (DeferredBlock<? extends Block> block : blocks) {
         ItemBlockRenderTypes.setRenderLayer((Block)block.get(), type);
      }
   }

   @SubscribeEvent
   static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.CREAKING.get(), CreakingRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.SHELF.get(), ShelfRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.COPPER_CHEST.get(), CopperChestRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.COPPER_GOLEM_STATUE.get(), CopperGolemStatueRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.HAPPY_GHAST.get(), HappyGhastRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.PARCHED.get(), ParchedRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.CAMEL_HUSK.get(), CamelHuskRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.COPPER_GOLEM.get(), CopperGolemRenderer::new);
      event.registerEntityRenderer(EntityType.SHEEP, SheepRenderer26::new);
      event.registerEntityRenderer(EntityType.WOLF, WolfRenderer26::new);
      event.registerEntityRenderer(EntityType.CAT, FelineRenderers26.Cat26::new);
      event.registerEntityRenderer(EntityType.VILLAGER, VillagerRenderer26::new);
      event.registerEntityRenderer(EntityType.POLAR_BEAR, PolarBearRenderer26::new);
      event.registerEntityRenderer(EntityType.STRIDER, NetherBabyRenderers26.Strider26::new);
      event.registerEntityRenderer(EntityType.PANDA, AnimalBabyRenderers26.Panda26::new);
      event.registerEntityRenderer(EntityType.HORSE, MountBabyRenderers26::horse);
      event.registerEntityRenderer(EntityType.DONKEY, ctx -> new MountBabyRenderers26.Chested26(ctx, 0.87F, ModelLayers.DONKEY, "donkey_baby"));
      event.registerEntityRenderer(EntityType.MULE, ctx -> new MountBabyRenderers26.Chested26(ctx, 0.92F, ModelLayers.MULE, "mule_baby"));
      event.registerEntityRenderer(EntityType.SKELETON_HORSE, ctx -> new MountBabyRenderers26.Undead26(ctx, ModelLayers.SKELETON_HORSE, "horse_skeleton_baby"));
      event.registerEntityRenderer(EntityType.ZOMBIE_HORSE, ctx -> new MountBabyRenderers26.Undead26(ctx, ModelLayers.ZOMBIE_HORSE, "horse_zombie_baby"));
      event.registerEntityRenderer(EntityType.CAMEL, MountBabyRenderers26.Camel26::new);
      event.registerEntityRenderer(EntityType.RABBIT, KeyframeBabyRenderers26.Rabbit26::new);
      event.registerEntityRenderer(EntityType.ZOMBIE, HumanoidBabyRenderers26.Zombie26::new);
      event.registerEntityRenderer(EntityType.HUSK, HumanoidBabyRenderers26.Husk26::new);
      event.registerEntityRenderer(EntityType.DROWNED, HumanoidBabyRenderers26.Drowned26::new);
      event.registerEntityRenderer(EntityType.ZOMBIE_VILLAGER, HumanoidBabyRenderers26.ZombieVillager26::new);
      event.registerEntityRenderer(EntityType.PIGLIN, HumanoidBabyRenderers26.Piglin26::piglin);
      event.registerEntityRenderer(EntityType.ZOMBIFIED_PIGLIN, HumanoidBabyRenderers26.Piglin26::zombified);
      event.registerEntityRenderer(EntityType.ARMADILLO, KeyframeBabyRenderers26.Armadillo26::new);
      event.registerEntityRenderer(EntityType.AXOLOTL, KeyframeBabyRenderers26.Axolotl26::new);
      event.registerEntityRenderer(EntityType.MOOSHROOM, MountBabyRenderers26.Mooshroom26::new);
      event.registerEntityRenderer(EntityType.LLAMA, ctx -> new AnimalBabyRenderers26.Llama26(ctx, ModelLayers.LLAMA));
      event.registerEntityRenderer(EntityType.TRADER_LLAMA, ctx -> new AnimalBabyRenderers26.Llama26(ctx, ModelLayers.TRADER_LLAMA));
      event.registerEntityRenderer(EntityType.BEE, AnimalBabyRenderers26.Bee26::new);
      event.registerEntityRenderer(EntityType.FOX, AnimalBabyRenderers26.Fox26::new);
      event.registerEntityRenderer(EntityType.HOGLIN, NetherBabyRenderers26.Hoglin26::new);
      event.registerEntityRenderer(EntityType.ZOGLIN, NetherBabyRenderers26.Zoglin26::new);
      event.registerEntityRenderer((EntityType)ModEntities.NAUTILUS.get(), NautilusRenderer::nautilus);
      event.registerEntityRenderer((EntityType)ModEntities.ZOMBIE_NAUTILUS.get(), NautilusRenderer::zombie);
      event.registerEntityRenderer(EntityType.TURTLE, SmallBabyRenderers26.Turtle26::new);
      event.registerEntityRenderer(EntityType.GOAT, SmallBabyRenderers26.Goat26::new);
      event.registerEntityRenderer(EntityType.OCELOT, FelineRenderers26.Ocelot26::new);
      event.registerEntityRenderer(EntityType.COW, FarmAnimalRenderers.CowRenderer::new);
      event.registerEntityRenderer(EntityType.PIG, FarmAnimalRenderers.PigRenderer::new);
      event.registerEntityRenderer(EntityType.CHICKEN, FarmAnimalRenderers.ChickenRenderer::new);
   }

   @SubscribeEvent
   static void registerLayers(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(CopperGolemStatueRenderer.RUNNING, CopperGolemStatueModel::createRunningPoseLayer);
      event.registerLayerDefinition(CopperGolemStatueRenderer.SITTING, CopperGolemStatueModel::createSittingPoseLayer);
      event.registerLayerDefinition(CopperGolemStatueRenderer.STAR, CopperGolemStatueModel::createStarPoseLayer);
      event.registerLayerDefinition(SmallBabyRenderers26.TURTLE_BABY, SmallBabyRenderers26.BabyTurtleModel::createBodyLayer);
      event.registerLayerDefinition(SmallBabyRenderers26.GOAT_BABY, SmallBabyRenderers26.BabyGoatModel::createBodyLayer);
      event.registerLayerDefinition(NautilusRenderer.ADULT, NautilusModel::createBodyLayer);
      event.registerLayerDefinition(NautilusRenderer.BABY, NautilusModel::createBabyLayer);
      event.registerLayerDefinition(NautilusRenderer.ARMOR, NautilusModel::createArmorLayer);
      event.registerLayerDefinition(NautilusRenderer.SADDLE, NautilusModel::createSaddleLayer);
      event.registerLayerDefinition(NautilusRenderer.CORAL, NautilusModel::createCoralLayer);
      event.registerLayerDefinition(HumanoidBabyRenderers26.ZOMBIE_BABY, () -> BabyHumanoids.zombieLayer(CubeDeformation.NONE));
      event.registerLayerDefinition(HumanoidBabyRenderers26.DROWNED_BABY_OUTER, () -> BabyHumanoids.zombieLayer(new CubeDeformation(0.25F)));
      event.registerLayerDefinition(HumanoidBabyRenderers26.ZOMBIE_VILLAGER_BABY, BabyHumanoids::zombieVillagerLayer);
      event.registerLayerDefinition(HumanoidBabyRenderers26.PIGLIN_BABY, BabyHumanoids::piglinLayer);
      event.registerLayerDefinition(BabyArmorLayer.HUMANOID_INNER, () -> BabyArmorLayer.createMesh(new CubeDeformation(-0.1F, 0.3F, 0.3F), PartPose.ZERO));
      event.registerLayerDefinition(BabyArmorLayer.HUMANOID_OUTER, () -> BabyArmorLayer.createMesh(new CubeDeformation(-0.1F, 0.5F, 0.3F), PartPose.ZERO));
      event.registerLayerDefinition(BabyArmorLayer.PIGLIN_INNER, () -> BabyArmorLayer.createMesh(new CubeDeformation(0.7F), PartPose.offset(0.5F, -0.5F, 0.0F)));
      event.registerLayerDefinition(BabyArmorLayer.PIGLIN_OUTER, () -> BabyArmorLayer.createMesh(new CubeDeformation(0.7F), PartPose.offset(0.5F, -0.5F, 0.0F)));
      event.registerLayerDefinition(KeyframeBabyRenderers26.RABBIT_BABY, KeyframeBabyRenderers26.BabyRabbitModel::createBodyLayer);
      event.registerLayerDefinition(KeyframeBabyRenderers26.ARMADILLO_BABY, KeyframeBabyRenderers26.BabyArmadilloModel::createBodyLayer);
      event.registerLayerDefinition(KeyframeBabyRenderers26.AXOLOTL_BABY, KeyframeBabyRenderers26.BabyAxolotlModel::createBodyLayer);
      event.registerLayerDefinition(MountBabyRenderers26.HORSE_BABY, MountBabyRenderers26.BabyEquineModel::createHorseLayer);
      event.registerLayerDefinition(MountBabyRenderers26.DONKEY_BABY, MountBabyRenderers26.BabyEquineModel::createDonkeyLayer);
      event.registerLayerDefinition(MountBabyRenderers26.CAMEL_BABY, MountBabyRenderers26.BabyCamelModel::createBodyLayer);
      event.registerLayerDefinition(AnimalBabyRenderers26.PANDA_BABY, AnimalBabyRenderers26.BabyPandaModel::createBodyLayer);
      event.registerLayerDefinition(AnimalBabyRenderers26.LLAMA_BABY, () -> AnimalBabyRenderers26.BabyLlamaModel.createBodyLayer(CubeDeformation.NONE));
      event.registerLayerDefinition(
         AnimalBabyRenderers26.LLAMA_BABY_DECOR, () -> AnimalBabyRenderers26.BabyLlamaModel.createBodyLayer(new CubeDeformation(0.2F))
      );
      event.registerLayerDefinition(AnimalBabyRenderers26.BEE_BABY, AnimalBabyRenderers26.BabyBeeModel::createBodyLayer);
      event.registerLayerDefinition(AnimalBabyRenderers26.FOX_BABY, AnimalBabyRenderers26.BabyFoxModel::createBodyLayer);
      event.registerLayerDefinition(NetherBabyRenderers26.STRIDER_BABY, NetherBabyRenderers26.BabyStriderModel::createBodyLayer);
      event.registerLayerDefinition(NetherBabyRenderers26.HOGLIN_BABY, NetherBabyRenderers26.BabyHoglinModel::createBodyLayer);
      event.registerLayerDefinition(PolarBearRenderer26.BABY, PolarBearRenderer26.BabyPolarBearModel::createBodyLayer);
      event.registerLayerDefinition(VillagerRenderer26.BABY, VillagerRenderer26.BabyVillagerModel::createBodyLayer);
      event.registerLayerDefinition(FelineRenderers26.BABY, FelineRenderers26.BabyFelineModel::createBabyLayer);
      event.registerLayerDefinition(WolfRenderer26.BABY, WolfRenderer26.BabyWolfModel::createBodyLayer);
      event.registerLayerDefinition(SheepRenderer26.BABY, SheepRenderer26.BabySheepModel::createBodyLayer);
      event.registerLayerDefinition(CopperGolemRenderer.LAYER, CopperGolemModel::createBodyLayer);
      event.registerLayerDefinition(CreakingRenderer.LAYER, CreakingModel::createBodyLayer);
      event.registerLayerDefinition(CreakingRenderer.EYES_LAYER, CreakingModel::createEyesLayer);
      event.registerLayerDefinition(HappyGhastModels.GHAST, () -> HappyGhastModels.body(false));
      event.registerLayerDefinition(HappyGhastModels.BABY, () -> HappyGhastModels.body(true));
      event.registerLayerDefinition(HappyGhastModels.HARNESS, HappyGhastModels::harness);
      event.registerLayerDefinition(FarmAnimalModels.COW, FarmAnimalModels::cow);
      event.registerLayerDefinition(FarmAnimalModels.COW_COLD, FarmAnimalModels::coldCow);
      event.registerLayerDefinition(FarmAnimalModels.COW_WARM, FarmAnimalModels::warmCow);
      event.registerLayerDefinition(FarmAnimalModels.COW_BABY, FarmAnimalModels::babyCow);
      event.registerLayerDefinition(FarmAnimalModels.PIG, FarmAnimalModels::pig);
      event.registerLayerDefinition(FarmAnimalModels.PIG_COLD, FarmAnimalModels::coldPig);
      event.registerLayerDefinition(FarmAnimalModels.PIG_BABY, FarmAnimalModels::babyPig);
      event.registerLayerDefinition(FarmAnimalModels.PIG_SADDLE, FarmAnimalModels::pigSaddle);
      event.registerLayerDefinition(FarmAnimalModels.CHICKEN, FarmAnimalModels::chicken);
      event.registerLayerDefinition(FarmAnimalModels.CHICKEN_COLD, FarmAnimalModels::coldChicken);
      event.registerLayerDefinition(FarmAnimalModels.CHICKEN_BABY, FarmAnimalModels::babyChicken);
   }

   @SubscribeEvent
   static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      Item[] chestItems = ModBlocks.COPPER_CHEST.all().stream().map(b -> ((Block)b.get()).asItem()).toArray(Item[]::new);
      event.registerItem(new IClientItemExtensions() {
         private CopperChestRenderer.ItemRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new CopperChestRenderer.ItemRenderer();
            }

            return this.renderer;
         }
      }, chestItems);
      Item[] statueItems = ModBlocks.COPPER_GOLEM_STATUE.all().stream().map(b -> ((Block)b.get()).asItem()).toArray(Item[]::new);
      event.registerItem(new IClientItemExtensions() {
         private CopperGolemStatueRenderer.ItemRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new CopperGolemStatueRenderer.ItemRenderer();
            }

            return this.renderer;
         }
      }, statueItems);
   }

   @SubscribeEvent
   static void registerScreens(RegisterMenuScreensEvent event) {
      event.register((MenuType)ModMenus.NAUTILUS.get(), NautilusScreen::new);
   }

   @SubscribeEvent
   static void registerParticles(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)ModParticles.PALE_OAK_LEAVES.get(), FallingLeavesParticle.PaleOakProvider::new);
      event.registerSpriteSet((ParticleType)ModParticles.TINTED_LEAVES.get(), FallingLeavesParticle.TintedProvider::new);
      event.registerSpriteSet((ParticleType)ModParticles.FIREFLY.get(), FireflyParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.TRAIL.get(), TrailParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.COPPER_FIRE_FLAME.get(), Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.PAUSE_MOB_GROWTH.get(), SimpleVerticalParticle.PauseProvider::new);
      event.registerSpriteSet((ParticleType)ModParticles.RESET_MOB_GROWTH.get(), SimpleVerticalParticle.ResetProvider::new);
   }

   @SubscribeEvent
   static void registerColorResolvers(ColorResolvers event) {
      event.register(DryFoliageColor.RESOLVER);
   }

   @SubscribeEvent
   static void registerBlockColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block event) {
      event.register(
         (state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor(),
         new Block[]{(Block)ModBlocks.BUSH.get(), (Block)ModBlocks.WILDFLOWERS.get()}
      );
      event.register(
         (state, level, pos, tintIndex) -> level != null && pos != null ? level.getBlockTint(pos, DryFoliageColor.RESOLVER) : -10732494,
         new Block[]{(Block)ModBlocks.LEAF_LITTER.get()}
      );
   }

   @SubscribeEvent
   static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
      event.register((stack, tintIndex) -> 0xFF000000 | GrassColor.get(0.5, 1.0), new ItemLike[]{(ItemLike)ModBlocks.BUSH.get()});
   }

   @SubscribeEvent
   static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(new DryFoliageColor.ReloadListener());
   }
}
