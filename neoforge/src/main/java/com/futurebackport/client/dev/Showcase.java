package com.futurebackport.client.dev;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.block.DryVegetationBlock;
import com.futurebackport.block.HangingMossBlock;
import com.futurebackport.block.ShelfBlock;
import com.futurebackport.block.entity.ShelfBlockEntity;
import com.futurebackport.client.NautilusScreen;
import com.futurebackport.entity.CamelHusk;
import com.futurebackport.entity.CamelHuskSpawning;
import com.futurebackport.entity.CopperGolem;
import com.futurebackport.entity.Creaking;
import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.entity.Parched;
import com.futurebackport.entity.nautilus.AbstractNautilus;
import com.futurebackport.entity.nautilus.ZombieNautilus;
import com.futurebackport.menu.NautilusMenu;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModBoats;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModItems;
import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.io.FileUtils;

public final class Showcase {
   private static final String WORLD = "futurebackport_showcase";
   private static boolean WORLDGEN;
   private static final List<String> WORLDGEN_BIOMES = List.of(
      "futurebackport:pale_garden", "meadow", "birch_forest", "forest", "dark_forest", "desert", "badlands", "swamp", "plains", "taiga"
   );
   private static final int PER_ROW = 6;
   private static final int ROW_SPACING = 7;
   private static final double EYE = 1.62;
   private static final int Y = -60;
   private final List<Runnable> steps = new ArrayList<>();
   private boolean started;
   private int wait;
   private int rows;
   private BlockPos fireflyBush = BlockPos.ZERO;

   public static void register() {
      String mode = System.getProperty("futurebackport.showcase", "");
      if (mode.equals("true") || mode.equals("worldgen")) {
         WORLDGEN = mode.equals("worldgen");
         FutureBackport.LOGGER.info("Showcase: enabled");
         NeoForge.EVENT_BUS.register(new Showcase());
      }
   }

   private void createWorld() {
      this.started = true;
      Minecraft mc = Minecraft.getInstance();
      FutureBackport.LOGGER.info("Showcase: creating world");
      mc.options.renderDistance().set(4);

      try {
         File dir = mc.getLevelSource().getBaseDir().resolve("futurebackport_showcase").toFile();
         FileUtils.deleteDirectory(dir);
      } catch (Exception var4) {
         FutureBackport.LOGGER.warn("Showcase: could not clear old world", var4);
      }

      GameRules rules = new GameRules();
      ((BooleanValue)rules.getRule(GameRules.RULE_DAYLIGHT)).set(false, null);
      ((BooleanValue)rules.getRule(GameRules.RULE_WEATHER_CYCLE)).set(false, null);
      ((BooleanValue)rules.getRule(GameRules.RULE_DOMOBSPAWNING)).set(false, null);
      ((IntegerValue)rules.getRule(GameRules.RULE_RANDOMTICKING)).set(0, null);
      LevelSettings settings = new LevelSettings(
         "futurebackport_showcase", GameType.CREATIVE, false, Difficulty.EASY, true, rules, WorldDataConfiguration.DEFAULT
      );
      mc.createWorldOpenFlows()
         .createFreshLevel(
            "futurebackport_showcase",
            settings,
            new WorldOptions(WORLDGEN ? 12345L : 0L, false, false),
            registries -> ((WorldPreset)registries.registryOrThrow(Registries.WORLD_PRESET)
                  .getHolderOrThrow(WORLDGEN ? WorldPresets.NORMAL : WorldPresets.FLAT)
                  .value())
               .createWorldDimensions(),
            mc.screen
         );
      this.planSteps();
   }

   @SubscribeEvent
   public void onTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (!this.started) {
         if (mc.screen != null && mc.level == null && mc.getOverlay() == null) {
            FutureBackport.LOGGER.info("Showcase: starting from {}", mc.screen.getClass().getSimpleName());
            this.createWorld();
         }
      } else if (this.started && mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
         if (this.wait-- <= 0) {
            if (this.steps.isEmpty()) {
               mc.stop();
            } else {
               this.steps.remove(0).run();
            }
         }
      }
   }

   private void planSteps() {
      Minecraft mc = Minecraft.getInstance();
      if (WORLDGEN) {
         this.planWorldgenSteps();
      } else {
         this.after(60, () -> {
            mc.options.hideGui = true;
            mc.options.pauseOnLostFocus = false;
            command("time set 6000");
            command("weather clear");
            mc.getSingleplayerServer().execute(() -> this.build(mc.getSingleplayerServer().overworld()));
         });
         this.after(
            40,
            () -> {
               for (int row = 0; row < this.rows; row++) {
                  int z = row * 7;
                  int r = row;
                  this.steps.add(() -> {
                     look(5.0, z, 0.0F);
                     this.wait = 40;
                  });
                  this.steps.add(() -> {
                     shot("row" + r);
                     this.wait = 5;
                  });
               }

               int extra = this.rows * 7 + 2;
               this.steps.add(() -> {
                  look(8.0, extra, 0.0F);
                  this.wait = 40;
               });
               this.steps.add(() -> {
                  shot("entities");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  look(16.0, extra, 0.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("mobs");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(21.5, -59.62, extra - 7.0, 0.0F, 8.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("camel_husk");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(2.4, -60.22, extra - 7.5, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("copper_golem");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(2.4, -59.019999999999996, extra + 0.6, 180.0F, 40.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("copper_golem_statues");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-8.5, -59.32, extra - 9.5, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("sheep");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-18.0, -59.62, extra - 8.0, 0.0F, 15.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("wolves");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-27.5, -59.82, extra - 7.0, 0.0F, 15.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("kittens");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-35.4, -59.62, extra - 7.5, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("villagers");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-43.4, -59.62, extra - 8.0, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("polar_bears");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-49.1, -59.82, extra - 7.0, 0.0F, 15.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("turtles_goats");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-57.5, -59.019999999999996, extra - 9.5, 0.0F, 14.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("nautilus");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-72.0, -59.419999999999995, extra - 10.5, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("babies_a");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-86.5, -59.32, extra - 11.0, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("babies_b");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-106.5, -59.62, extra - 10.0, 0.0F, 12.0F);
                  this.wait = 30;
               });
               this.steps.add(() -> {
                  shot("babies_c");
                  this.wait = 5;
               });
               this.steps
                  .add(
                     () -> {
                        List<AbstractNautilus> armored = mc.level
                           .getEntitiesOfClass(AbstractNautilus.class, mc.player.getBoundingBox().inflate(20.0), AbstractNautilus::isSaddled);
                        if (!armored.isEmpty()) {
                           mc.setScreen(
                              new NautilusScreen(
                                 new NautilusMenu(0, mc.player.getInventory(), armored.get(0)), mc.player.getInventory(), armored.get(0).getDisplayName()
                              )
                           );
                        }

                        this.wait = 10;
                     }
                  );
               this.steps.add(() -> {
                  shot("nautilus_inventory");
                  Minecraft.getInstance().setScreen(null);
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(23.75, -56.62, extra + 3 - 8.0, 0.0F, 22.0F);
                  this.wait = 40;
               });
               this.steps.add(() -> {
                  shot("farm");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(34.0, -57.62, extra - 9.0, 0.0F, 10.0F);
                  this.wait = 40;
               });
               this.steps.add(() -> {
                  shot("ghast");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(8.5, -60.12, extra + 6 + 7.5, 180.0F, 5.0F);
                  this.wait = 40;
               });
               this.steps.add(() -> {
                  shot("items");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  command("item replace entity @s weapon.mainhand with futurebackport:diamond_spear");
                  command("item replace entity @s weapon.offhand with futurebackport:wooden_spear");
                  Minecraft.getInstance().options.hideGui = false;
                  this.wait = 20;
               });
               this.steps.add(() -> {
                  shot("spear_in_hand");
                  Minecraft.getInstance().options.hideGui = true;
                  command("item replace entity @s weapon.mainhand with air");
                  command("item replace entity @s weapon.offhand with air");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  teleport(-12.5, -60.0, -14.5, 0.0F, -25.0F);
                  this.wait = 300;
               });
               this.steps.add(() -> {
                  shot("leaves");
                  this.wait = 5;
               });
               this.steps.add(() -> {
                  logCreativeNeighbours();
                  this.wait = 1;
               });
               this.steps.add(() -> {
                  command("time set 18000");
                  look(this.fireflyBush.getX(), this.fireflyBush.getZ(), 0.0F);
                  this.wait = 100;
               });
               this.steps.add(() -> {
                  shot("night");
                  this.wait = 5;
               });
            }
         );
      }
   }

   private void planWorldgenSteps() {
      Minecraft mc = Minecraft.getInstance();
      this.after(60, () -> {
         mc.options.hideGui = true;
         command("time set 6000");
         command("weather clear");
         IntegratedServer server = mc.getSingleplayerServer();
         server.execute(() -> {
            for (ServerPlayer player : server.overworld().players()) {
               player.getAbilities().flying = true;
               player.onUpdateAbilities();
            }
         });
      });

      for (String biome : WORLDGEN_BIOMES) {
         this.steps.add(() -> {
            IntegratedServer server = mc.getSingleplayerServer();
            server.execute(() -> {
               ServerLevel level = server.overworld();
               ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(biome));
               Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(h -> h.is(key), BlockPos.ZERO.atY(64), 6400, 32, 64);
               if (found == null) {
                  FutureBackport.LOGGER.warn("Showcase: no {} found", biome);
               } else {
                  BlockPos p = (BlockPos)found.getFirst();
                  level.getChunk(p.getX() >> 4, p.getZ() >> 4);
                  int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
                  mc.execute(() -> teleport(p.getX() + 0.5, y + 0.4, p.getZ() + 0.5, 0.0F, 20.0F));
               }
            });
            this.wait = 120;
         });
         this.steps.add(() -> {
            shot("worldgen_" + biome.replace("futurebackport:", ""));
            this.wait = 5;
         });
      }
   }

   private void after(int ticks, Runnable step) {
      this.steps.add(() -> this.wait = ticks);
      this.steps.add(step);
   }

   private static void command(String command) {
      Minecraft.getInstance().player.connection.sendCommand(command);
   }

   private static void look(double x, double z, float yaw) {
      teleport(x, -59.12, z + 0.5 - 5.4, yaw, 25.0F);
   }

   private static void teleport(double x, double y, double z, float yaw, float pitch) {
      command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", x, y, z, yaw, pitch));
   }

   private static void logCreativeNeighbours() {
      Minecraft mc = Minecraft.getInstance();
      CreativeModeTabs.tryRebuildTabContents(mc.player.connection.enabledFeatures(), true, mc.level.registryAccess());

      for (String id : List.of("pale_oak_log", "copper_sword", "leaf_litter", "music_disc_tears", "resin_brick")) {
         Item item = (Item)BuiltInRegistries.ITEM.get(FutureBackport.id(id));

         for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            ArrayList<ItemStack> items = new ArrayList<>(tab.getDisplayItems());

            for (int i = 1; i < items.size(); i++) {
               if (items.get(i).is(item)) {
                  FutureBackport.LOGGER
                     .info(
                        "Showcase: creative {} -> {} after {}",
                        new Object[]{tab.getDisplayName().getString(), id, BuiltInRegistries.ITEM.getKey(items.get(i - 1).getItem())}
                     );
               }
            }
         }
      }
   }

   private static void shot(String name) {
      Minecraft mc = Minecraft.getInstance();
      Screenshot.grab(
         mc.gameDirectory, "showcase_" + name + ".png", mc.getMainRenderTarget(), msg -> FutureBackport.LOGGER.info("Showcase: {}", msg.getString())
      );
   }

   private void build(ServerLevel level) {
      for (ServerPlayer player : level.players()) {
         player.getAbilities().flying = true;
         player.onUpdateAbilities();
      }

      List<Block> blocks = new ArrayList<>();

      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof BlockItem blockItem && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("futurebackport")) {
            blocks.add(blockItem.getBlock());
         }
      }

      blocks.add((Block)ModBlocks.POTTED_PALE_OAK_SAPLING.get());
      blocks.add((Block)ModBlocks.POTTED_OPEN_EYEBLOSSOM.get());
      blocks.add((Block)ModBlocks.POTTED_CLOSED_EYEBLOSSOM.get());
      blocks.add((Block)ModBlocks.POTTED_GOLDEN_DANDELION.get());
      this.rows = (blocks.size() + 6 - 1) / 6;

      for (int i = 0; i < blocks.size(); i++) {
         place(level, new BlockPos(i % 6 * 2, -60, i / 6 * 7), blocks.get(i));
         if (blocks.get(i) == ModBlocks.FIREFLY_BUSH.get()) {
            this.fireflyBush = new BlockPos(i % 6 * 2, -60, i / 6 * 7);
         }
      }

      for (int lx = -20; lx < -4; lx++) {
         for (int lz = -20; lz < -4; lz++) {
            for (int ly = 0; ly < 2; ly++) {
               level.setBlock(
                  new BlockPos(lx, -56 + ly, lz),
                  (BlockState)(lx < -12 ? Blocks.AZALEA_LEAVES : Blocks.OAK_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT, true),
                  2
               );
            }
         }
      }

      int z = this.rows * 7 + 2;
      Boat boat = new Boat(level, 2.0, -60.0, z);
      boat.setVariant((Type)ModBoats.PALE_OAK.getValue());
      level.addFreshEntity(boat);
      ChestBoat chestBoat = new ChestBoat(level, 5.0, -60.0, z);
      chestBoat.setVariant((Type)ModBoats.PALE_OAK.getValue());
      level.addFreshEntity(chestBoat);
      armorStand(level, 8.0, z, null);
      armorStand(
         level,
         10.0,
         z,
         Holder.direct(
            (TrimMaterial)level.registryAccess()
               .registryOrThrow(Registries.TRIM_MATERIAL)
               .getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.withDefaultNamespace("copper")))
         )
      );
      armorStand(
         level,
         12.0,
         z,
         Holder.direct(
            (TrimMaterial)level.registryAccess()
               .registryOrThrow(Registries.TRIM_MATERIAL)
               .getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, FutureBackport.id("resin")))
         )
      );
      Horse horse = (Horse)EntityType.HORSE.create(level);
      horse.moveTo(14.0, -60.0, z, 180.0F, 0.0F);
      horse.setTamed(true);
      horse.setNoAi(true);
      horse.setBodyArmorItem(new ItemStack((ItemLike)ModItems.COPPER_HORSE_ARMOR.get()));
      level.addFreshEntity(horse);
      Creaking creaking = (Creaking)((EntityType)ModEntities.CREAKING.get()).create(level);
      creaking.moveTo(16.0, -60.0, z, 180.0F, 0.0F);
      creaking.setNoAi(true);
      creaking.setPersistenceRequired();
      level.addFreshEntity(creaking);
      HappyGhast happyGhast = (HappyGhast)((EntityType)ModEntities.HAPPY_GHAST.get()).create(level);
      happyGhast.moveTo(36.0, -59.0, z + 4, 180.0F, 0.0F);
      happyGhast.setNoAi(true);
      happyGhast.setItemSlot(EquipmentSlot.BODY, new ItemStack((ItemLike)ModItems.HARNESSES.get(DyeColor.LIGHT_BLUE).get()));
      level.addFreshEntity(happyGhast);
      Parched parched = (Parched)((EntityType)ModEntities.PARCHED.get()).create(level);
      parched.moveTo(18.0, -60.0, z, 180.0F, 0.0F);
      parched.setNoAi(true);
      parched.setPersistenceRequired();
      parched.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
      level.addFreshEntity(parched);
      Husk husk = (Husk)EntityType.HUSK.create(level);
      husk.moveTo(21.0, -60.0, z, 150.0F, 0.0F);
      CamelHusk camelHusk = CamelHuskSpawning.mount(husk, level, level.getCurrentDifficultyAt(husk.blockPosition()), MobSpawnType.SPAWN_EGG);
      level.addFreshEntity(husk);
      camelHusk.equipSaddle(new ItemStack(Items.SADDLE), null);
      camelHusk.setYRot(150.0F);
      camelHusk.setYBodyRot(150.0F);
      camelHusk.setPersistenceRequired();

      for (Entity e : List.of(camelHusk, husk, (Entity)camelHusk.getPassengers().get(1))) {
         ((Mob)e).setNoAi(true);
      }

      WeatherState[] weather = WeatherState.values();

      for (int ix = 0; ix < weather.length; ix++) {
         CopperGolem golem = (CopperGolem)((EntityType)ModEntities.COPPER_GOLEM.get()).create(level);
         golem.moveTo(0.5 + ix * 1.25, -60.0, z - 4, 180.0F, 0.0F);
         golem.setYHeadRot(180.0F);
         golem.setYBodyRot(180.0F);
         golem.setNoAi(true);
         golem.setWeatherState(weather[ix]);
         if (ix == 1) {
            golem.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 16));
         }

         level.addFreshEntity(golem);
         level.setBlock(
            new BlockPos(ix * 5 / 4, -60, z - 2),
            (BlockState)((BlockState)((Block)ModBlocks.COPPER_GOLEM_STATUE.weathering().get(weather[ix]).get())
                  .defaultBlockState()
                  .setValue(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.Pose.values()[ix]))
               .setValue(CopperGolemStatueBlock.FACING, Direction.SOUTH),
            2
         );
      }

      Object[][] sheep = new Object[][]{
         {false, DyeColor.WHITE, false, null},
         {false, DyeColor.RED, true, null},
         {false, DyeColor.WHITE, false, "jeb_"},
         {true, DyeColor.WHITE, false, null},
         {true, DyeColor.LIGHT_BLUE, false, null},
         {true, DyeColor.WHITE, true, null}
      };

      for (int ix = 0; ix < sheep.length; ix++) {
         Sheep s = (Sheep)EntityType.SHEEP.create(level);
         s.moveTo(-12.5 + ix * 1.6, -60.0, z - 4, 180.0F, 0.0F);
         s.setYHeadRot(180.0F);
         s.setYBodyRot(180.0F);
         s.setNoAi(true);
         if ((Boolean)sheep[ix][0]) {
            s.setAge(-24000);
         }

         s.setColor((DyeColor)sheep[ix][1]);
         s.setSheared((Boolean)sheep[ix][2]);
         if (sheep[ix][3] != null) {
            s.setCustomName(Component.literal((String)sheep[ix][3]));
         }

         level.addFreshEntity(s);
      }

      for (int ix = 0; ix < 4; ix++) {
         Wolf w = (Wolf)EntityType.WOLF.create(level);
         w.moveTo(-21.5 + ix * 1.5, -60.0, z - 4, 180.0F, 0.0F);
         w.setYHeadRot(180.0F);
         w.setYBodyRot(180.0F);
         w.setNoAi(true);
         if (ix > 0) {
            w.setAge(-24000);
         }

         if (ix == 2) {
            w.setTame(true, false);
            w.setInSittingPose(true);
            w.setOrderedToSit(true);
         }

         if (ix == 3) {
            w.setRemainingPersistentAngerTime(100000);
         }

         level.addFreshEntity(w);
      }

      for (int ix = 0; ix < 3; ix++) {
         Cat c = (Cat)EntityType.CAT.create(level);
         c.moveTo(-29.5 + ix * 1.3, -60.0, z - 4, 180.0F, 0.0F);
         c.setYHeadRot(180.0F);
         c.setYBodyRot(180.0F);
         c.setNoAi(true);
         c.setAge(-24000);
         Registry<CatVariant> catVariants = level.registryAccess().registryOrThrow(Registries.CAT_VARIANT);
         c.setVariant(catVariants.getHolderOrThrow(ix == 1 ? CatVariant.BLACK : CatVariant.TABBY));
         if (ix == 1) {
            c.setTame(true, false);
            c.setInSittingPose(true);
            c.setOrderedToSit(true);
         }

         level.addFreshEntity(c);
      }

      Ocelot ocelotKitten = (Ocelot)EntityType.OCELOT.create(level);
      ocelotKitten.moveTo(-25.6, -60.0, z - 4, 180.0F, 0.0F);
      ocelotKitten.setYHeadRot(180.0F);
      ocelotKitten.setYBodyRot(180.0F);
      ocelotKitten.setNoAi(true);
      ocelotKitten.setAge(-24000);
      level.addFreshEntity(ocelotKitten);
      String[] villagerTypes = new String[]{"plains", "plains", "desert", "snow"};

      for (int ix = 0; ix < villagerTypes.length; ix++) {
         Villager v = (Villager)EntityType.VILLAGER.create(level);
         v.moveTo(-37.5 + ix * 1.4, -60.0, z - 4, 180.0F, 0.0F);
         v.setYHeadRot(180.0F);
         v.setYBodyRot(180.0F);
         v.setNoAi(true);
         if (ix > 0) {
            v.setAge(-24000);
         }

         v.setVillagerData(
            v.getVillagerData().setType((VillagerType)BuiltInRegistries.VILLAGER_TYPE.get(ResourceLocation.withDefaultNamespace(villagerTypes[ix])))
         );
         level.addFreshEntity(v);
      }

      for (int ix = 0; ix < 2; ix++) {
         PolarBear bear = (PolarBear)EntityType.POLAR_BEAR.create(level);
         bear.moveTo(-44.5 + ix * 2.2, -60.0, z - 4, 180.0F, 0.0F);
         bear.setYHeadRot(180.0F);
         bear.setYBodyRot(180.0F);
         bear.setNoAi(true);
         if (ix == 1) {
            bear.setAge(-24000);
         }

         level.addFreshEntity(bear);
      }

      List<EntityType<? extends Animal>> turtleTypes = List.of(EntityType.TURTLE, EntityType.TURTLE, EntityType.GOAT, EntityType.GOAT);

      for (int ix = 0; ix < turtleTypes.size(); ix++) {
         Animal animal = (Animal)turtleTypes.get(ix).create(level);
         animal.moveTo(-51.5 + ix * 1.6, -60.0, z - 4, 180.0F, 0.0F);
         animal.setYHeadRot(180.0F);
         animal.setYBodyRot(180.0F);
         animal.setNoAi(true);
         if (ix % 2 == 1) {
            animal.setAge(-24000);
         }

         level.addFreshEntity(animal);
      }

      for (int ix = 0; ix < 5; ix++) {
         AbstractNautilus n = ix < 3
            ? (AbstractNautilus)((EntityType)ModEntities.NAUTILUS.get()).create(level)
            : (AbstractNautilus)((EntityType)ModEntities.ZOMBIE_NAUTILUS.get()).create(level);
         n.moveTo(-61.5 + ix * 2.0, -60.0, z - 4, 180.0F, 0.0F);
         n.setYHeadRot(180.0F);
         n.setYBodyRot(180.0F);
         n.setNoAi(true);
         n.setPersistenceRequired();
         if (ix == 1) {
            n.setTame(true, false);
            n.setOwnerUUID(Minecraft.getInstance().player.getUUID());
            n.getInventory().setItem(0, new ItemStack(Items.SADDLE));
            n.getInventory().setItem(1, new ItemStack((ItemLike)ModItems.DIAMOND_NAUTILUS_ARMOR.get()));
         }

         if (ix == 2) {
            n.setAge(-24000);
         }

         if (ix >= 3) {
            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  level.setBlock(new BlockPos(-62 + ix * 2 + dx, -52, z - 4 + dz), Blocks.STONE.defaultBlockState(), 2);
               }
            }
         }

         if (ix == 4) {
            ((ZombieNautilus)n).setVariant(ZombieNautilus.Variant.WARM);
         }

         level.addFreshEntity(n);
         if (ix == 4) {
            Drowned drowned = (Drowned)EntityType.DROWNED.create(level);
            drowned.moveTo(n.getX(), n.getY(), n.getZ(), 180.0F, 0.0F);
            drowned.setNoAi(true);
            drowned.setPersistenceRequired();
            drowned.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TRIDENT));
            drowned.startRiding(n, true);
            level.addFreshEntity(drowned);
         }
      }

      List<EntityType<? extends Mob>> babyRowA = List.of(
         EntityType.STRIDER, EntityType.HOGLIN, EntityType.ZOGLIN, EntityType.PANDA, EntityType.LLAMA, EntityType.TRADER_LLAMA, EntityType.BEE, EntityType.FOX
      );

      for (int ix = 0; ix < babyRowA.size(); ix++) {
         Mob m = (Mob)babyRowA.get(ix).create(level);
         m.moveTo(-76.5 + ix * 1.6, -60.0, z - 4, 180.0F, 0.0F);
         m.setYHeadRot(180.0F);
         m.setYBodyRot(180.0F);
         m.setNoAi(true);
         m.setPersistenceRequired();
         m.setBaby(true);
         if (m instanceof Zoglin) {
            m.setBaby(true);
         }

         level.addFreshEntity(m);
      }

      List<EntityType<? extends Mob>> babyRowB = List.of(
         EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE, EntityType.CAMEL, EntityType.MOOSHROOM
      );

      for (int ix = 0; ix < babyRowB.size(); ix++) {
         Mob m = (Mob)babyRowB.get(ix).create(level);
         m.moveTo(-92.5 + ix * 2.0, -60.0, z - 4, 180.0F, 0.0F);
         m.setYHeadRot(180.0F);
         m.setYBodyRot(180.0F);
         m.setNoAi(true);
         m.setPersistenceRequired();
         m.setBaby(true);
         level.addFreshEntity(m);
      }

      List<EntityType<? extends Mob>> babyRowC = List.of(
         EntityType.RABBIT,
         EntityType.ARMADILLO,
         EntityType.AXOLOTL,
         EntityType.ZOMBIE,
         EntityType.HUSK,
         EntityType.DROWNED,
         EntityType.ZOMBIE_VILLAGER,
         EntityType.PIGLIN,
         EntityType.ZOMBIFIED_PIGLIN,
         EntityType.ZOMBIE
      );

      for (int ix = 0; ix < babyRowC.size(); ix++) {
         Mob m = (Mob)babyRowC.get(ix).create(level);
         m.moveTo(-112.5 + ix * 1.5, -60.0, z - 4, 180.0F, 0.0F);
         m.setYHeadRot(180.0F);
         m.setYBodyRot(180.0F);
         m.setNoAi(true);
         m.setPersistenceRequired();
         m.setBaby(ix < 9);
         EquipmentSlot head = EquipmentSlot.HEAD;
         switch (ix) {
            case 3:
            case 9:
               m.setItemSlot(head, new ItemStack(Items.IRON_HELMET));
               m.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
               m.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
               break;
            case 4:
               ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
               legs.set(DataComponents.DYED_COLOR, new DyedItemColor(3368652, true));
               m.setItemSlot(EquipmentSlot.LEGS, legs);
               break;
            case 5:
               m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            case 6:
            default:
               break;
            case 7:
               m.setItemSlot(head, new ItemStack(Items.GOLDEN_HELMET));
               m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
               break;
            case 8:
               m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
         }

         boolean burnsInSun = m instanceof Zombie && !(m instanceof Husk);
         if (burnsInSun) {
            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  level.setBlock(new BlockPos((int)Math.floor(m.getX()) + dx, -52, z - 4 + dz), Blocks.STONE.defaultBlockState(), 2);
               }
            }
         }

         if (m instanceof Piglin p) {
            p.setImmuneToZombification(true);
         }

         level.addFreshEntity(m);
      }

      HappyGhast ghastling = (HappyGhast)((EntityType)ModEntities.HAPPY_GHAST.get()).create(level);
      ghastling.moveTo(31.0, -59.0, z + 1, 180.0F, 0.0F);
      ghastling.setNoAi(true);
      ghastling.setBaby(true);
      level.addFreshEntity(ghastling);
      int farmZ = z + 3;
      List<EntityType<? extends Animal>> animalTypes = List.of(EntityType.CHICKEN, EntityType.PIG, EntityType.COW);

      for (int a = 0; a < animalTypes.size(); a++) {
         for (FarmAnimalVariant variant : FarmAnimalVariant.values()) {
            for (boolean baby : new boolean[]{false, true}) {
               Animal animal = (Animal)animalTypes.get(a).create(level);
               animal.moveTo(20 + variant.ordinal() * 3 + (baby ? 1.5 : 0.0), -60.0, farmZ + a * 2.5, 180.0F, 0.0F);
               animal.setNoAi(true);
               if (baby) {
                  animal.setAge(-24000);
               }

               animal.setData(FarmAnimalVariant.ATTACHMENT, variant);
               level.addFreshEntity(animal);
            }
         }
      }

      int wallZ = z + 6;
      List<Item> items = new ArrayList<>();

      for (Item itemx : BuiltInRegistries.ITEM) {
         if (!(itemx instanceof BlockItem) && BuiltInRegistries.ITEM.getKey(itemx).getNamespace().equals("futurebackport")) {
            items.add(itemx);
         }
      }

      items.add(Items.IRON_HELMET);
      items.add(((Block)ModBlocks.COPPER_GOLEM_STATUE.weathering().get(WeatherState.UNAFFECTED).get()).asItem());

      for (int ix = 0; ix < items.size(); ix++) {
         BlockPos wall = new BlockPos(ix % 12, -60 + ix / 12, wallZ);
         level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
         ItemFrame frame = new ItemFrame(level, wall.south(), Direction.SOUTH);
         ItemStack stack = new ItemStack((ItemLike)items.get(ix));
         if (items.get(ix) == Items.IRON_HELMET) {
            trim(level, stack, FutureBackport.id("resin"));
         }

         frame.setItem(stack, false);
         level.addFreshEntity(frame);
      }

      for (int x = 13; x <= 15; x++) {
         for (int y = -60; y <= -58; y++) {
            level.setBlockAndUpdate(new BlockPos(x, y, wallZ), Blocks.STONE.defaultBlockState());
         }
      }

      Registry<PaintingVariant> paintings = level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
      level.addFreshEntity(
         new Painting(
            level,
            new BlockPos(14, -59, wallZ + 1),
            Direction.SOUTH,
            paintings.getHolderOrThrow(ResourceKey.create(Registries.PAINTING_VARIANT, FutureBackport.id("dennis")))
         )
      );
   }

   private static void trim(ServerLevel level, ItemStack stack, ResourceLocation material) {
      Registry<TrimMaterial> materials = level.registryAccess().registryOrThrow(Registries.TRIM_MATERIAL);
      Registry<TrimPattern> patterns = level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN);
      Holder<TrimMaterial> mat = materials.getHolderOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, material));
      Holder<TrimPattern> pattern = patterns.getHolderOrThrow(TrimPatterns.COAST);
      stack.set(DataComponents.TRIM, new ArmorTrim(mat, pattern));
   }

   private static void armorStand(ServerLevel level, double x, int z, Holder<TrimMaterial> trim) {
      ArmorStand stand = new ArmorStand(level, x, -60.0, z);
      stand.setYRot(180.0F);

      for (Entry<EquipmentSlot, ArmorItem> entry : List.of(
         Map.entry(EquipmentSlot.HEAD, (ArmorItem)ModItems.COPPER_HELMET.get()),
         Map.entry(EquipmentSlot.CHEST, (ArmorItem)ModItems.COPPER_CHESTPLATE.get()),
         Map.entry(EquipmentSlot.LEGS, (ArmorItem)ModItems.COPPER_LEGGINGS.get()),
         Map.entry(EquipmentSlot.FEET, (ArmorItem)ModItems.COPPER_BOOTS.get())
      )) {
         ItemStack stack = new ItemStack((ItemLike)entry.getValue());
         if (trim != null) {
            stack.set(
               DataComponents.TRIM, new ArmorTrim(trim, level.registryAccess().registryOrThrow(Registries.TRIM_PATTERN).getHolderOrThrow(TrimPatterns.COAST))
            );
         }

         stand.setItemSlot(entry.getKey(), stack);
      }

      stand.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)ModItems.COPPER_SWORD.get()));
      level.addFreshEntity(stand);
   }

   private static void place(ServerLevel level, BlockPos pos, Block block) {
      BlockState state = block.defaultBlockState();
      if (block instanceof DryVegetationBlock) {
         level.setBlockAndUpdate(pos.below(), Blocks.SAND.defaultBlockState());
      }

      if (block == ModBlocks.CACTUS_FLOWER.get()) {
         level.setBlockAndUpdate(pos.below(), Blocks.SAND.defaultBlockState());
         level.setBlockAndUpdate(pos, (BlockState)Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 0));
         pos = pos.above();
      }

      if (block instanceof HangingMossBlock || block instanceof CeilingHangingSignBlock) {
         level.setBlockAndUpdate(pos.above(2), ((RotatedPillarBlock)ModBlocks.PALE_OAK_LOG.get()).defaultBlockState());
         pos = pos.above();
      }

      if (block instanceof ShelfBlock) {
         state = (BlockState)state.setValue(ShelfBlock.FACING, Direction.NORTH);
      }

      if (block instanceof StandingSignBlock) {
         state = (BlockState)state.setValue(StandingSignBlock.ROTATION, 8);
      }

      if (block instanceof CeilingHangingSignBlock) {
         state = (BlockState)state.setValue(CeilingHangingSignBlock.ROTATION, 8);
      }

      if (block instanceof MultifaceBlock) {
         state = (BlockState)state.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
      }

      if (block instanceof LeavesBlock) {
         state = (BlockState)state.setValue(LeavesBlock.PERSISTENT, true);
      }

      if (block instanceof DoorBlock) {
         level.setBlock(pos, (BlockState)state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
         level.setBlock(pos.above(), (BlockState)state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
      } else {
         level.setBlockAndUpdate(pos, state);
         if (block instanceof ShelfBlock && level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
            shelf.swapItemNoUpdate(0, new ItemStack(Items.DIAMOND_SWORD));
            shelf.swapItemNoUpdate(1, new ItemStack((ItemLike)ModItems.RESIN_BRICK.get()));
            shelf.swapItemNoUpdate(2, new ItemStack((ItemLike)ModBlocks.PALE_OAK_PLANKS.get()));
         }

         if ((block instanceof StandingSignBlock || block instanceof CeilingHangingSignBlock) && level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            sign.setText(new SignText().setMessage(1, Component.literal("Future")).setMessage(2, Component.literal("Backport")), true);
         }
      }
   }
}
