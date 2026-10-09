package com.futurebackport;

import com.futurebackport.item.VariantEggItem;

import com.futurebackport.platform.registry.BlockEntry;

import com.futurebackport.entity.AgeLock;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.entity.SoundVariants;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModDataMaps;
import com.futurebackport.registry.ModCreativeTabs;
import com.futurebackport.registry.ModEffects;
import com.futurebackport.registry.ModEnchantments;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import com.futurebackport.registry.ModMaterials;
import com.futurebackport.registry.ModMenus;
import com.futurebackport.registry.ModPaintings;
import com.futurebackport.registry.ModParticles;
import com.futurebackport.registry.ModSounds;
import com.futurebackport.registry.ModWorldgen;
import com.futurebackport.network.ModNetwork;
import com.futurebackport.platform.Services;
import com.mojang.logging.LogUtils;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;

public class FutureBackport {
   public static final String MODID = "futurebackport";
   public static final Logger LOGGER = LogUtils.getLogger();

   private FutureBackport() {
   }

   /** Mod construction, on every loader: create all registry entries and declare packets. */
   public static void init() {
      bootstrapRegistries();
      ModNetwork.register();
   }

   /**
    * Loads every registry class so its static fields create their entries. Order matters on loaders that register
    * immediately (Fabric): sounds and blocks before the items, entities and tabs that reference them.
    */
   public static void bootstrapRegistries() {
      Object[] registries = {
         ModSounds.SOUNDS, ModParticles.PARTICLES, ModEffects.EFFECTS, 
         ModBlocks.BLOCKS, ModEntities.ENTITIES, ModItems.ITEMS, ModEnchantments.ENCHANTMENTS, ModPaintings.PAINTINGS, ModBlockEntities.BLOCK_ENTITIES,
         ModWorldgen.FEATURES, ModWorldgen.TREE_DECORATORS, ModMenus.MENUS, ModCreativeTabs.CREATIVE_MODE_TABS,
         FarmAnimalVariant.ATTACHMENT, AgeLock.LOCKED, SoundVariants.ATTACHMENT
      };
      LOGGER.debug("Bootstrapped {} registries", registries.length);
   }

   public static ResourceLocation id(String path) {
      return new ResourceLocation("futurebackport", path);
   }

   /** Common setup, on the main thread: hook this mod's blocks into vanilla systems. */
   public static void commonSetup() {
      {
         {
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
            DispenserBlock.registerBehavior(ModItems.BLUE_EGG.get(), VariantEggItem.DISPENSE_BEHAVIOR);
            DispenserBlock.registerBehavior(ModItems.BROWN_EGG.get(), VariantEggItem.DISPENSE_BEHAVIOR);
            ModDataMaps.registerCompostables();
            Services.PLATFORM.addPottedPlant(ModBlocks.PALE_OAK_SAPLING.getId(), ModBlocks.POTTED_PALE_OAK_SAPLING);
            Services.PLATFORM.addPottedPlant(ModBlocks.OPEN_EYEBLOSSOM.getId(), ModBlocks.POTTED_OPEN_EYEBLOSSOM);
            Services.PLATFORM.addPottedPlant(ModBlocks.CLOSED_EYEBLOSSOM.getId(), ModBlocks.POTTED_CLOSED_EYEBLOSSOM);
            Services.PLATFORM.addPottedPlant(ModBlocks.GOLDEN_DANDELION.getId(), ModBlocks.POTTED_GOLDEN_DANDELION);
         }
      }
   }

   @SafeVarargs
   private static void flammable(int encouragement, int flammability, BlockEntry<? extends Block>... blocks) {
      for (BlockEntry<? extends Block> block : blocks) {
         Services.PLATFORM.setFlammable(block.get(), encouragement, flammability);
      }
   }

   /** Vanilla block entity types that this mod's blocks also use (pale oak signs). */
   public static void addBlockEntityBlocks(BiConsumer<BlockEntityType<?>, Block[]> modifier) {
      modifier.accept(BlockEntityType.SIGN, new Block[]{(Block)ModBlocks.PALE_OAK_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_SIGN.get()});
      modifier.accept(BlockEntityType.HANGING_SIGN, new Block[]{(Block)ModBlocks.PALE_OAK_HANGING_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get()});
   }
}
