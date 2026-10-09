package com.futurebackport.registry;

import com.futurebackport.FutureBackport;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * The block and item pairings that NeoForge 1.21 reads from data maps (oxidation, waxing, stripping, composting).
 * 1.20.1 has no data maps, so the same JSON files ship in data/futurebackport/data_maps and are read here once,
 * after registration. Shared code and both loaders look pairings up through this class.
 */
public final class ModDataMaps {
   private static Data data;

   private ModDataMaps() {
   }

   public static Optional<Block> nextOxidized(Block block) {
      return Optional.ofNullable(data().oxidizables.get(block));
   }

   public static Optional<Block> previousOxidized(Block block) {
      return Optional.ofNullable(data().oxidizables.inverse().get(block));
   }

   public static Optional<Block> waxed(Block block) {
      return Optional.ofNullable(data().waxables.get(block));
   }

   public static Optional<Block> unwaxed(Block block) {
      return Optional.ofNullable(data().waxables.inverse().get(block));
   }

   public static Optional<Block> stripped(Block block) {
      return Optional.ofNullable(data().strippables.get(block));
   }

   public static Map<Block, Block> oxidizables() {
      return data().oxidizables;
   }

   public static Map<Block, Block> waxables() {
      return data().waxables;
   }

   public static Map<Block, Block> strippables() {
      return data().strippables;
   }

   /** Adds the compostable items to the composter (vanilla's table is mutable, so this works on every loader). */
   public static void registerCompostables() {
      data().compostables.forEach((item, chance) -> ComposterBlock.COMPOSTABLES.put(item, chance.floatValue()));
   }

   private static synchronized Data data() {
      if (data == null) {
         data = new Data(
            ImmutableBiMap.copyOf(read("block/oxidizables", ModDataMaps::block, value -> block(value.getAsString()))),
            ImmutableBiMap.copyOf(read("block/waxables", ModDataMaps::block, value -> block(value.getAsString()))),
            read("block/strippables", ModDataMaps::block, value -> block(value.getAsString())),
            read("item/compostables", id -> BuiltInRegistries.ITEM.get(new ResourceLocation(id)), value -> value.isJsonObject()
               ? value.getAsJsonObject().get("chance").getAsFloat()
               : value.getAsFloat())
         );
      }
      return data;
   }

   private static Block block(String id) {
      return BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
   }

   private static <K, V> Map<K, V> read(String name, Function<String, K> key, Function<JsonElement, V> value) {
      String path = "/data/" + FutureBackport.MODID + "/data_maps/" + name + ".json";
      ImmutableMap.Builder<K, V> map = ImmutableMap.builder();
      try (InputStream stream = ModDataMaps.class.getResourceAsStream(path)) {
         if (stream == null) {
            FutureBackport.LOGGER.error("Missing data map {}", path);
            return Map.of();
         }
         JsonObject json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
         for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "values").entrySet()) {
            map.put(key.apply(entry.getKey()), value.apply(entry.getValue()));
         }
      } catch (Exception e) {
         FutureBackport.LOGGER.error("Could not read data map {}", path, e);
      }
      return map.build();
   }

   private record Data(ImmutableBiMap<Block, Block> oxidizables, ImmutableBiMap<Block, Block> waxables, Map<Block, Block> strippables, Map<Item, Float> compostables) {
   }
}
