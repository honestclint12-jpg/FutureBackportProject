package com.futurebackport.fabric.compat;

import com.futurebackport.FutureBackport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

/**
 * Applies the mod's NeoForge-format data on Fabric, reading the same JSON files the NeoForge build loads as data:
 * biome modifiers, block/item data maps and global loot modifiers. Keeping one copy of the data means both loaders
 * generate the same world.
 */
public final class NeoForgeDataOnFabric {

    private NeoForgeDataOnFabric() {
    }

    public static void apply() {
        forEachJson("data/futurebackport/neoforge/biome_modifier", NeoForgeDataOnFabric::biomeModifier);
        dataMap("data/neoforge/data_maps/block/strippables.json", (from, to) ->
                StrippableBlockRegistry.register(BuiltInRegistries.BLOCK.get(from), BuiltInRegistries.BLOCK.get(to)));
        dataMap("data/neoforge/data_maps/block/oxidizables.json", (from, to) ->
                OxidizableBlocksRegistry.registerOxidizableBlockPair(BuiltInRegistries.BLOCK.get(from), BuiltInRegistries.BLOCK.get(to)));
        dataMap("data/neoforge/data_maps/block/waxables.json", (from, to) ->
                OxidizableBlocksRegistry.registerWaxableBlockPair(BuiltInRegistries.BLOCK.get(from), BuiltInRegistries.BLOCK.get(to)));
        compostables();
        lootModifiers();
    }

    // ---- biome modifiers ----

    private static void biomeModifier(ResourceLocation id, JsonObject json) {
        String type = json.get("type").getAsString();
        Predicate<BiomeSelectionContext> biomes = biomes(json.get("biomes"));
        switch (type) {
            case "neoforge:add_features" -> {
                GenerationStep.Decoration step = step(json.get("step").getAsString());
                List<ResourceKey<PlacedFeature>> features = features(json.get("features"));
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes,
                        context -> features.forEach(feature -> context.getGenerationSettings().addFeature(step, feature)));
            }
            case "neoforge:remove_features" -> {
                List<ResourceKey<PlacedFeature>> features = features(json.get("features"));
                List<GenerationStep.Decoration> steps = new ArrayList<>();
                if (json.has("steps")) {
                    json.getAsJsonArray("steps").forEach(step -> steps.add(step(step.getAsString())));
                } else {
                    steps.addAll(List.of(GenerationStep.Decoration.values()));
                }
                BiomeModifications.create(id).add(ModificationPhase.REMOVALS, biomes,
                        context -> features.forEach(feature -> steps.forEach(step -> context.getGenerationSettings().removeFeature(step, feature))));
            }
            case "neoforge:add_spawns" -> {
                JsonElement spawners = json.get("spawners");
                List<JsonObject> list = new ArrayList<>();
                if (spawners.isJsonArray()) {
                    spawners.getAsJsonArray().forEach(element -> list.add(element.getAsJsonObject()));
                } else {
                    list.add(spawners.getAsJsonObject());
                }
                BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomes, context -> {
                    for (JsonObject spawner : list) {
                        EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(spawner.get("type").getAsString()));
                        context.getSpawnSettings().addSpawn(entity.getCategory(), new MobSpawnSettings.SpawnerData(entity,
                                spawner.get("weight").getAsInt(), spawner.get("minCount").getAsInt(), spawner.get("maxCount").getAsInt()));
                    }
                });
            }
            case "futurebackport:set_spawn_weight" -> setSpawnWeight(id, json, biomes);
            default -> FutureBackport.LOGGER.warn("Biome modifier {} has type {}, which Fabric doesn't support yet", id, type);
        }
    }

    /** Same as the NeoForge SetSpawnWeightModifier: keep the existing spawn entry but change its weight. */
    private static void setSpawnWeight(ResourceLocation id, JsonObject json, Predicate<BiomeSelectionContext> biomes) {
        EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(json.get("entity_type").getAsString()));
        int weight = json.get("weight").getAsInt();
        // Fabric's modification context can't read existing spawns, so remember them while selecting biomes.
        Map<ResourceKey<Biome>, List<MobSpawnSettings.SpawnerData>> existing = new HashMap<>();
        Predicate<BiomeSelectionContext> selector = context -> {
            if (!biomes.test(context)) {
                return false;
            }
            List<MobSpawnSettings.SpawnerData> found = context.getBiome().getMobSettings().getMobs(entity.getCategory()).unwrap()
                    .stream().filter(data -> data.type == entity).toList();
            existing.put(context.getBiomeKey(), found);
            return !found.isEmpty();
        };
        BiomeModifications.create(id).add(ModificationPhase.REPLACEMENTS, selector, (BiomeSelectionContext selection, BiomeModificationContext context) -> {
            context.getSpawnSettings().removeSpawnsOfEntityType(entity);
            for (MobSpawnSettings.SpawnerData data : existing.getOrDefault(selection.getBiomeKey(), List.of())) {
                context.getSpawnSettings().addSpawn(entity.getCategory(), new MobSpawnSettings.SpawnerData(entity, weight, data.minCount, data.maxCount));
            }
        });
    }

    private static Predicate<BiomeSelectionContext> biomes(JsonElement element) {
        List<String> ids = new ArrayList<>();
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(id -> ids.add(id.getAsString()));
        } else {
            ids.add(element.getAsString());
        }
        Predicate<BiomeSelectionContext> selector = context -> false;
        for (String id : ids) {
            Predicate<BiomeSelectionContext> one = id.startsWith("#")
                    ? BiomeSelectors.tag(TagKey.create(Registries.BIOME, ResourceLocation.parse(id.substring(1))))
                    : BiomeSelectors.includeByKey(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(id)));
            selector = selector.or(one);
        }
        return selector;
    }

    private static List<ResourceKey<PlacedFeature>> features(JsonElement element) {
        List<ResourceKey<PlacedFeature>> features = new ArrayList<>();
        JsonArray array = element.isJsonArray() ? element.getAsJsonArray() : null;
        if (array == null) {
            features.add(ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(element.getAsString())));
        } else {
            array.forEach(feature -> features.add(ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(feature.getAsString()))));
        }
        return features;
    }

    private static GenerationStep.Decoration step(String name) {
        for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
            if (step.getSerializedName().equals(name)) {
                return step;
            }
        }
        throw new IllegalArgumentException("Unknown generation step " + name);
    }

    // ---- data maps ----

    private static void dataMap(String path, BiConsumer<ResourceLocation, ResourceLocation> consumer) {
        JsonObject json = read(path);
        if (json != null) {
            json.getAsJsonObject("values").entrySet().forEach(entry ->
                    consumer.accept(ResourceLocation.parse(entry.getKey()), ResourceLocation.parse(entry.getValue().getAsString())));
        }
    }

    private static void compostables() {
        JsonObject json = read("data/neoforge/data_maps/item/compostables.json");
        if (json != null) {
            json.getAsJsonObject("values").entrySet().forEach(entry -> {
                JsonElement value = entry.getValue();
                float chance = value.isJsonObject() ? value.getAsJsonObject().get("chance").getAsFloat() : value.getAsFloat();
                CompostingChanceRegistry.INSTANCE.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey())), chance);
            });
        }
    }

    // ---- loot ----

    /** NeoForge "add_table" loot modifiers become an extra pool that rolls the injected table once. */
    private static void lootModifiers() {
        JsonObject global = read("data/neoforge/loot_modifiers/global_loot_modifiers.json");
        if (global == null) {
            return;
        }
        Map<ResourceKey<LootTable>, List<ResourceKey<LootTable>>> injections = new HashMap<>();
        for (JsonElement entry : global.getAsJsonArray("entries")) {
            ResourceLocation id = ResourceLocation.parse(entry.getAsString());
            JsonObject modifier = read("data/" + id.getNamespace() + "/loot_modifiers/" + id.getPath() + ".json");
            if (modifier == null || !modifier.get("type").getAsString().equals("neoforge:add_table")) {
                FutureBackport.LOGGER.warn("Loot modifier {} isn't an add_table modifier; Fabric doesn't support it yet", id);
                continue;
            }
            ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(modifier.get("table").getAsString()));
            for (JsonElement condition : modifier.getAsJsonArray("conditions")) {
                JsonObject object = condition.getAsJsonObject();
                if (object.get("condition").getAsString().equals("neoforge:loot_table_id")) {
                    ResourceKey<LootTable> target = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(object.get("loot_table_id").getAsString()));
                    injections.computeIfAbsent(target, key -> new ArrayList<>()).add(table);
                }
            }
        }
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            for (ResourceKey<LootTable> table : injections.getOrDefault(key, List.of())) {
                builder.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(table)));
            }
        });
    }

    // ---- reading the mod's own resources ----

    private static void forEachJson(String directory, BiConsumer<ResourceLocation, JsonObject> consumer) {
        Path root = resource(directory);
        if (root == null) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(file -> file.toString().endsWith(".json")).sorted().forEach(file -> {
                String name = root.relativize(file).toString().replace('\\', '/');
                ResourceLocation id = FutureBackport.id(name.substring(0, name.length() - ".json".length()));
                try (Reader reader = Files.newBufferedReader(file)) {
                    consumer.accept(id, JsonParser.parseReader(reader).getAsJsonObject());
                } catch (IOException | RuntimeException e) {
                    FutureBackport.LOGGER.error("Could not apply {}", file, e);
                }
            });
        } catch (IOException e) {
            FutureBackport.LOGGER.error("Could not list {}", directory, e);
        }
    }

    private static JsonObject read(String path) {
        Path file = resource(path);
        if (file == null) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            FutureBackport.LOGGER.error("Could not read {}", path, e);
            return null;
        }
    }

    private static Path resource(String path) {
        return FabricLoader.getInstance().getModContainer(FutureBackport.MODID)
                .flatMap(container -> container.findPath(path))
                .filter(Files::exists)
                .orElse(null);
    }
}
