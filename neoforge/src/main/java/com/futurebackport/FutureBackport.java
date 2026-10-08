package com.futurebackport;

import com.futurebackport.platform.registry.BlockEntry;

import com.futurebackport.entity.AgeLock;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.futurebackport.entity.SoundVariants;
import com.futurebackport.entity.nautilus.BreathOfTheNautilus;
import com.futurebackport.network.FarmAnimalVariantPayload;
import com.futurebackport.network.SpearJabPayload;
import com.futurebackport.registry.CreativePlacements;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModCreativeTabs;
import com.futurebackport.registry.ModEffects;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import com.futurebackport.registry.ModMaterials;
import com.futurebackport.registry.ModMenus;
import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModSounds;
import com.futurebackport.registry.ModWorldgen;
import com.futurebackport.neoforge.platform.NeoForgeRegistrationFactory;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod("futurebackport")
public class FutureBackport {
   public static final String MODID = "futurebackport";
   public static final Logger LOGGER = LogUtils.getLogger();

   public FutureBackport(IEventBus modEventBus, ModContainer modContainer) {
      NeoForgeRegistrationFactory.attach(modEventBus);
      bootstrapRegistries();
      FarmAnimalVariant.ATTACHMENTS.register(modEventBus);
      ModWorldgen.BIOME_MODIFIERS.register(modEventBus);
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(this::addBlockEntityBlocks);
      modEventBus.addListener(ModEntities::registerAttributes);
      modEventBus.addListener(ModEntities::registerSpawnPlacements);
      modEventBus.addListener(this::registerPayloads);
      modEventBus.addListener(CreativePlacements::onBuildTab);
      NeoForge.EVENT_BUS.register(new FarmAnimalVariantEvents());
      NeoForge.EVENT_BUS.register(new AgeLock());
      NeoForge.EVENT_BUS.register(SoundVariants.class);
      NeoForge.EVENT_BUS.register(BreathOfTheNautilus.class);
   }

   /**
    * Loads every registry class so its static fields create their entries. Order matters on loaders that register
    * immediately (Fabric): sounds and blocks before the items, entities and tabs that reference them.
    */
   public static void bootstrapRegistries() {
      Object[] registries = {
         ModSounds.SOUNDS, ModParticles.PARTICLES, ModEffects.EFFECTS, ModMaterials.ARMOR_MATERIALS,
         ModBlocks.BLOCKS, ModEntities.ENTITIES, ModItems.ITEMS, ModBlockEntities.BLOCK_ENTITIES,
         ModWorldgen.FEATURES, ModWorldgen.TREE_DECORATORS, ModMenus.MENUS, ModCreativeTabs.CREATIVE_MODE_TABS
      };
      LOGGER.debug("Bootstrapped {} registries", registries.length);
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("futurebackport", path);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(
         () -> {
            flammable(
               5, 20, ModBlocks.PALE_OAK_PLANKS, ModBlocks.PALE_OAK_SLAB, ModBlocks.PALE_OAK_STAIRS, ModBlocks.PALE_OAK_FENCE, ModBlocks.PALE_OAK_FENCE_GATE
            );
            flammable(5, 5, ModBlocks.PALE_OAK_LOG, ModBlocks.PALE_OAK_WOOD, ModBlocks.STRIPPED_PALE_OAK_LOG, ModBlocks.STRIPPED_PALE_OAK_WOOD);
            flammable(30, 60, ModBlocks.PALE_OAK_LEAVES);
            ModBlocks.SHELVES.forEach((wood, shelf) -> {
               if (!wood.equals("crimson") && !wood.equals("warped")) {
                  flammable(30, 20, shelf);
               }
            });
            flammable(5, 100, ModBlocks.PALE_MOSS_BLOCK, ModBlocks.PALE_MOSS_CARPET, ModBlocks.PALE_HANGING_MOSS);
            flammable(
               60,
               100,
               ModBlocks.OPEN_EYEBLOSSOM,
               ModBlocks.CLOSED_EYEBLOSSOM,
               ModBlocks.GOLDEN_DANDELION,
               ModBlocks.WILDFLOWERS,
               ModBlocks.LEAF_LITTER,
               ModBlocks.CACTUS_FLOWER,
               ModBlocks.SHORT_DRY_GRASS,
               ModBlocks.TALL_DRY_GRASS,
               ModBlocks.FIREFLY_BUSH,
               ModBlocks.BUSH
            );
            DispenserBlock.registerProjectileBehavior((ItemLike)ModItems.BLUE_EGG.get());
            DispenserBlock.registerProjectileBehavior((ItemLike)ModItems.BROWN_EGG.get());
            FlowerPotBlock pot = (FlowerPotBlock)Blocks.FLOWER_POT;
            pot.addPlant(ModBlocks.PALE_OAK_SAPLING.getId(), ModBlocks.POTTED_PALE_OAK_SAPLING);
            pot.addPlant(ModBlocks.OPEN_EYEBLOSSOM.getId(), ModBlocks.POTTED_OPEN_EYEBLOSSOM);
            pot.addPlant(ModBlocks.CLOSED_EYEBLOSSOM.getId(), ModBlocks.POTTED_CLOSED_EYEBLOSSOM);
            pot.addPlant(ModBlocks.GOLDEN_DANDELION.getId(), ModBlocks.POTTED_GOLDEN_DANDELION);
         }
      );
   }

   @SafeVarargs
   private static void flammable(int encouragement, int flammability, BlockEntry<? extends Block>... blocks) {
      for (BlockEntry<? extends Block> block : blocks) {
         ((FireBlock)Blocks.FIRE).setFlammable((Block)block.get(), encouragement, flammability);
      }
   }

   private void registerPayloads(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToClient(FarmAnimalVariantPayload.TYPE, FarmAnimalVariantPayload.STREAM_CODEC, FarmAnimalVariantPayload::handle);
      registrar.playToServer(SpearJabPayload.TYPE, SpearJabPayload.STREAM_CODEC, SpearJabPayload::handle);
   }

   private void addBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
      event.modify(BlockEntityType.SIGN, new Block[]{(Block)ModBlocks.PALE_OAK_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_SIGN.get()});
      event.modify(BlockEntityType.HANGING_SIGN, new Block[]{(Block)ModBlocks.PALE_OAK_HANGING_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get()});
   }
}
