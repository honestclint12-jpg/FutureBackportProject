package com.futurebackport.fabric.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla's spawn placement registration is private (NeoForge: RegisterSpawnPlacementsEvent). */
@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsInvoker {

    @Invoker("register")
    static <T extends Mob> void futurebackport$register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
        throw new AssertionError();
    }
}
