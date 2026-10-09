package com.futurebackport.platform.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/** Receives this mod's natural-spawn rules. NeoForge feeds them to RegisterSpawnPlacementsEvent; Fabric to SpawnPlacements. */
@FunctionalInterface
public interface SpawnPlacementRegistrar {

    <T extends Mob> void register(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate);
}
