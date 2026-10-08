package com.futurebackport.registry;

import com.futurebackport.platform.registry.ItemRegistration;
import com.futurebackport.platform.registry.ItemEntry;
import com.futurebackport.platform.Services;
import net.minecraft.world.item.SpawnEggItem;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.item.HarnessItem;
import com.futurebackport.item.NautilusArmorItem;
import com.futurebackport.item.SpearItem;
import com.futurebackport.item.VariantEggItem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.AnimalArmorItem.BodyType;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;

public class ModItems {
   public static final ItemRegistration ITEMS = ItemRegistration.create("futurebackport");
   public static final ItemEntry<SignItem> PALE_OAK_SIGN = ITEMS.register(
      "pale_oak_sign", () -> new SignItem(new Properties().stacksTo(16), (Block)ModBlocks.PALE_OAK_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_SIGN.get())
   );
   public static final ItemEntry<HangingSignItem> PALE_OAK_HANGING_SIGN = ITEMS.register(
      "pale_oak_hanging_sign",
      () -> new HangingSignItem((Block)ModBlocks.PALE_OAK_HANGING_SIGN.get(), (Block)ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get(), new Properties().stacksTo(16))
   );
   public static final ItemEntry<BoatItem> PALE_OAK_BOAT = ITEMS.register(
      "pale_oak_boat", () -> new BoatItem(false, ModBoats.paleOak(), new Properties().stacksTo(1))
   );
   public static final ItemEntry<BoatItem> PALE_OAK_CHEST_BOAT = ITEMS.register(
      "pale_oak_chest_boat", () -> new BoatItem(true, ModBoats.paleOak(), new Properties().stacksTo(1))
   );
   public static final ItemEntry<Item> RESIN_BRICK = ITEMS.registerSimpleItem("resin_brick");
   public static final ItemEntry<SpawnEggItem> CREAKING_SPAWN_EGG = ITEMS.register(
      "creaking_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.CREAKING, 16777215, 16777215, new Properties())
   );
   public static final ItemEntry<Item> COPPER_NUGGET = ITEMS.registerSimpleItem("copper_nugget");
   public static final ItemEntry<SwordItem> COPPER_SWORD = ITEMS.register(
      "copper_sword",
      () -> new SwordItem(ModMaterials.COPPER_TIER, new Properties().attributes(SwordItem.createAttributes(ModMaterials.COPPER_TIER, 3, -2.4F)))
   );
   public static final ItemEntry<ShovelItem> COPPER_SHOVEL = ITEMS.register(
      "copper_shovel",
      () -> new ShovelItem(ModMaterials.COPPER_TIER, new Properties().attributes(DiggerItem.createAttributes(ModMaterials.COPPER_TIER, 1.5F, -3.0F)))
   );
   public static final ItemEntry<PickaxeItem> COPPER_PICKAXE = ITEMS.register(
      "copper_pickaxe",
      () -> new PickaxeItem(ModMaterials.COPPER_TIER, new Properties().attributes(DiggerItem.createAttributes(ModMaterials.COPPER_TIER, 1.0F, -2.8F)))
   );
   public static final ItemEntry<AxeItem> COPPER_AXE = ITEMS.register(
      "copper_axe",
      () -> new AxeItem(ModMaterials.COPPER_TIER, new Properties().attributes(DiggerItem.createAttributes(ModMaterials.COPPER_TIER, 7.0F, -3.2F)))
   );
   public static final ItemEntry<HoeItem> COPPER_HOE = ITEMS.register(
      "copper_hoe",
      () -> new HoeItem(ModMaterials.COPPER_TIER, new Properties().attributes(DiggerItem.createAttributes(ModMaterials.COPPER_TIER, -1.0F, -2.0F)))
   );
   public static final ItemEntry<ArmorItem> COPPER_HELMET = copperArmor("copper_helmet", net.minecraft.world.item.ArmorItem.Type.HELMET);
   public static final ItemEntry<ArmorItem> COPPER_CHESTPLATE = copperArmor("copper_chestplate", net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
   public static final ItemEntry<ArmorItem> COPPER_LEGGINGS = copperArmor("copper_leggings", net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
   public static final ItemEntry<ArmorItem> COPPER_BOOTS = copperArmor("copper_boots", net.minecraft.world.item.ArmorItem.Type.BOOTS);
   public static final ItemEntry<AnimalArmorItem> COPPER_HORSE_ARMOR = ITEMS.register(
      "copper_horse_armor", () -> new AnimalArmorItem(ModMaterials.COPPER_ARMOR.holder(), BodyType.EQUESTRIAN, false, new Properties().stacksTo(1))
   );
   public static final ItemEntry<StandingAndWallBlockItem> COPPER_TORCH = ITEMS.register(
      "copper_torch",
      () -> new StandingAndWallBlockItem((Block)ModBlocks.COPPER_TORCH.get(), (Block)ModBlocks.COPPER_WALL_TORCH.get(), new Properties(), Direction.DOWN)
   );
   public static final ItemEntry<Item> MUSIC_DISC_TEARS;
   public static final ItemEntry<Item> MUSIC_DISC_LAVA_CHICKEN;
   public static final ItemEntry<Item> MUSIC_DISC_BOUNCE;
   public static final ItemEntry<BannerPatternItem> BORDURE_INDENTED_BANNER_PATTERN;
   public static final ItemEntry<BannerPatternItem> FIELD_MASONED_BANNER_PATTERN;
   public static final ItemEntry<AnimalArmorItem> NETHERITE_HORSE_ARMOR;
   public static final ItemEntry<VariantEggItem> BLUE_EGG;
   public static final ItemEntry<VariantEggItem> BROWN_EGG;
   public static final ItemEntry<SpawnEggItem> HAPPY_GHAST_SPAWN_EGG;
   public static final Map<DyeColor, ItemEntry<HarnessItem>> HARNESSES;
   public static final ItemEntry<SpawnEggItem> PARCHED_SPAWN_EGG;
   public static final ItemEntry<SpearItem> WOODEN_SPEAR;
   public static final ItemEntry<SpearItem> STONE_SPEAR;
   public static final ItemEntry<SpearItem> COPPER_SPEAR;
   public static final ItemEntry<SpearItem> IRON_SPEAR;
   public static final ItemEntry<SpearItem> GOLDEN_SPEAR;
   public static final ItemEntry<SpearItem> DIAMOND_SPEAR;
   public static final ItemEntry<SpearItem> NETHERITE_SPEAR;
   public static final ItemEntry<SpawnEggItem> COPPER_GOLEM_SPAWN_EGG;
   public static final ItemEntry<SpawnEggItem> NAUTILUS_SPAWN_EGG;
   public static final ItemEntry<SpawnEggItem> ZOMBIE_NAUTILUS_SPAWN_EGG;
   public static final ItemEntry<NautilusArmorItem> COPPER_NAUTILUS_ARMOR;
   public static final ItemEntry<NautilusArmorItem> IRON_NAUTILUS_ARMOR;
   public static final ItemEntry<NautilusArmorItem> GOLDEN_NAUTILUS_ARMOR;
   public static final ItemEntry<NautilusArmorItem> DIAMOND_NAUTILUS_ARMOR;
   public static final ItemEntry<NautilusArmorItem> NETHERITE_NAUTILUS_ARMOR;
   public static final ItemEntry<SpawnEggItem> CAMEL_HUSK_SPAWN_EGG;

   private static ItemEntry<Item> musicDisc(String name, String song, Rarity rarity) {
      ResourceKey<JukeboxSong> key = ResourceKey.create(Registries.JUKEBOX_SONG, FutureBackport.id(song));
      return ITEMS.register(name, () -> new Item(new Properties().stacksTo(1).rarity(rarity).jukeboxPlayable(key)));
   }

   private static ItemEntry<BannerPatternItem> bannerPattern(String name, String tag) {
      TagKey<BannerPattern> patterns = TagKey.create(Registries.BANNER_PATTERN, FutureBackport.id("pattern_item/" + tag));
      return ITEMS.register(name, () -> new BannerPatternItem(patterns, new Properties().stacksTo(1)));
   }

   private static ItemEntry<ArmorItem> copperArmor(String name, net.minecraft.world.item.ArmorItem.Type type) {
      return ITEMS.register(name, () -> new ArmorItem(ModMaterials.COPPER_ARMOR.holder(), type, new Properties().durability(type.getDurability(11))));
   }

   static {
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_LOG);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_WOOD);
      ITEMS.registerSimpleBlockItem(ModBlocks.STRIPPED_PALE_OAK_LOG);
      ITEMS.registerSimpleBlockItem(ModBlocks.STRIPPED_PALE_OAK_WOOD);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_PLANKS);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_STAIRS);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_SLAB);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_FENCE);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_FENCE_GATE);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_DOOR);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_TRAPDOOR);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_PRESSURE_PLATE);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_BUTTON);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_LEAVES);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_OAK_SAPLING);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_MOSS_BLOCK);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_MOSS_CARPET);
      ITEMS.registerSimpleBlockItem(ModBlocks.PALE_HANGING_MOSS);
      ITEMS.registerSimpleBlockItem(ModBlocks.OPEN_EYEBLOSSOM);
      ITEMS.registerSimpleBlockItem(ModBlocks.CLOSED_EYEBLOSSOM);
      ITEMS.registerSimpleBlockItem(ModBlocks.CREAKING_HEART);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_CLUMP);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_BLOCK);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_BRICKS);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_BRICK_STAIRS);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_BRICK_SLAB);
      ITEMS.registerSimpleBlockItem(ModBlocks.RESIN_BRICK_WALL);
      ITEMS.registerSimpleBlockItem(ModBlocks.CHISELED_RESIN_BRICKS);
      ITEMS.registerSimpleBlockItem(ModBlocks.LEAF_LITTER);
      ITEMS.registerSimpleBlockItem(ModBlocks.WILDFLOWERS);
      ITEMS.registerSimpleBlockItem(ModBlocks.BUSH);
      ITEMS.registerSimpleBlockItem(ModBlocks.FIREFLY_BUSH);
      ITEMS.registerSimpleBlockItem(ModBlocks.CACTUS_FLOWER);
      ITEMS.registerSimpleBlockItem(ModBlocks.SHORT_DRY_GRASS);
      ITEMS.registerSimpleBlockItem(ModBlocks.TALL_DRY_GRASS);
      ITEMS.registerSimpleBlockItem(ModBlocks.GOLDEN_DANDELION);

      for (CopperFamily family : List.of(
         ModBlocks.COPPER_BARS,
         ModBlocks.COPPER_CHAIN,
         ModBlocks.COPPER_LANTERN,
         ModBlocks.LIGHTNING_ROD,
         ModBlocks.COPPER_CHEST,
         ModBlocks.COPPER_GOLEM_STATUE
      )) {
         family.all().forEach(ITEMS::registerSimpleBlockItem);
      }

      MUSIC_DISC_TEARS = musicDisc("music_disc_tears", "tears", Rarity.UNCOMMON);
      MUSIC_DISC_LAVA_CHICKEN = musicDisc("music_disc_lava_chicken", "lava_chicken", Rarity.RARE);
      MUSIC_DISC_BOUNCE = musicDisc("music_disc_bounce", "bounce", Rarity.UNCOMMON);
      BORDURE_INDENTED_BANNER_PATTERN = bannerPattern("bordure_indented_banner_pattern", "bordure_indented");
      FIELD_MASONED_BANNER_PATTERN = bannerPattern("field_masoned_banner_pattern", "field_masoned");
      NETHERITE_HORSE_ARMOR = ITEMS.register(
         "netherite_horse_armor", () -> new AnimalArmorItem(ArmorMaterials.NETHERITE, BodyType.EQUESTRIAN, false, new Properties().stacksTo(1).fireResistant())
      );
      BLUE_EGG = ITEMS.register("blue_egg", () -> new VariantEggItem(FarmAnimalVariant.COLD, new Properties().stacksTo(16)));
      BROWN_EGG = ITEMS.register("brown_egg", () -> new VariantEggItem(FarmAnimalVariant.WARM, new Properties().stacksTo(16)));
      HAPPY_GHAST_SPAWN_EGG = ITEMS.register(
         "happy_ghast_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.HAPPY_GHAST, 16777215, 16777215, new Properties())
      );
      HARNESSES = new EnumMap<>(DyeColor.class);
      ITEMS.registerSimpleBlockItem(ModBlocks.DRIED_GHAST);

      for (DyeColor color : DyeColor.values()) {
         HARNESSES.put(color, ITEMS.register(color.getSerializedName() + "_harness", () -> new HarnessItem(color, new Properties().stacksTo(1))));
      }

      PARCHED_SPAWN_EGG = ITEMS.register("parched_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.PARCHED, 16777215, 16777215, new Properties()));
      WOODEN_SPEAR = ITEMS.register(
         "wooden_spear", () -> new SpearItem(Tiers.WOOD, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 10.0F, 5.1F, 15.0F, 4.6F, new Properties())
      );
      STONE_SPEAR = ITEMS.register("stone_spear", () -> new SpearItem(Tiers.STONE, 0.75F, 0.82F, 0.7F, 4.5F, 13.0F, 9.0F, 5.1F, 13.75F, 4.6F, new Properties()));
      COPPER_SPEAR = ITEMS.register(
         "copper_spear", () -> new SpearItem(ModMaterials.COPPER_TIER, 0.85F, 0.82F, 0.65F, 4.0F, 12.0F, 8.25F, 5.1F, 12.5F, 4.6F, new Properties())
      );
      IRON_SPEAR = ITEMS.register("iron_spear", () -> new SpearItem(Tiers.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F, new Properties()));
      GOLDEN_SPEAR = ITEMS.register("golden_spear", () -> new SpearItem(Tiers.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 13.0F, 8.5F, 5.1F, 13.75F, 4.6F, new Properties()));
      DIAMOND_SPEAR = ITEMS.register(
         "diamond_spear", () -> new SpearItem(Tiers.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F, new Properties())
      );
      NETHERITE_SPEAR = ITEMS.register(
         "netherite_spear", () -> new SpearItem(Tiers.NETHERITE, 1.15F, 1.2F, 0.4F, 2.5F, 9.0F, 5.5F, 5.1F, 8.75F, 4.6F, new Properties().fireResistant())
      );
      COPPER_GOLEM_SPAWN_EGG = ITEMS.register(
         "copper_golem_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.COPPER_GOLEM, 16777215, 16777215, new Properties())
      );
      NAUTILUS_SPAWN_EGG = ITEMS.register("nautilus_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.NAUTILUS, 16777215, 16777215, new Properties()));
      ZOMBIE_NAUTILUS_SPAWN_EGG = ITEMS.register(
         "zombie_nautilus_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.ZOMBIE_NAUTILUS, 16777215, 16777215, new Properties())
      );
      COPPER_NAUTILUS_ARMOR = ITEMS.register("copper_nautilus_armor", () -> new NautilusArmorItem("copper", 4, 0.0F, 0.0F, new Properties()));
      IRON_NAUTILUS_ARMOR = ITEMS.register("iron_nautilus_armor", () -> new NautilusArmorItem("iron", 5, 0.0F, 0.0F, new Properties()));
      GOLDEN_NAUTILUS_ARMOR = ITEMS.register("golden_nautilus_armor", () -> new NautilusArmorItem("gold", 7, 0.0F, 0.0F, new Properties()));
      DIAMOND_NAUTILUS_ARMOR = ITEMS.register("diamond_nautilus_armor", () -> new NautilusArmorItem("diamond", 11, 2.0F, 0.0F, new Properties()));
      NETHERITE_NAUTILUS_ARMOR = ITEMS.register(
         "netherite_nautilus_armor", () -> new NautilusArmorItem("netherite", 19, 3.0F, 0.1F, new Properties().fireResistant())
      );
      CAMEL_HUSK_SPAWN_EGG = ITEMS.register(
         "camel_husk_spawn_egg", () -> Services.PLATFORM.spawnEgg(ModEntities.CAMEL_HUSK, 16777215, 16777215, new Properties())
      );
      ModBlocks.SHELVES.values().forEach(ITEMS::registerSimpleBlockItem);

      for (StoneFamily family : List.of(ModBlocks.SULFUR, ModBlocks.POLISHED_SULFUR, ModBlocks.SULFUR_BRICKS)) {
         family.all().forEach(ITEMS::registerSimpleBlockItem);
      }

      ITEMS.registerSimpleBlockItem(ModBlocks.CHISELED_SULFUR);

      for (StoneFamily family : List.of(ModBlocks.CINNABAR, ModBlocks.POLISHED_CINNABAR, ModBlocks.CINNABAR_BRICKS)) {
         family.all().forEach(ITEMS::registerSimpleBlockItem);
      }

      ITEMS.registerSimpleBlockItem(ModBlocks.CHISELED_CINNABAR);
   }
}
