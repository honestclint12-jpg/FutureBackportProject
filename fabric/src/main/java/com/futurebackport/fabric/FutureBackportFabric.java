package com.futurebackport.fabric;

import com.futurebackport.FutureBackport;
import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.futurebackport.fabric.compat.ForgeDataOnFabric;
import com.futurebackport.fabric.mixin.BlockEntityTypeAccessor;
import com.futurebackport.fabric.platform.FabricRegistrationFactory;
import com.futurebackport.fabric.mixin.SpawnPlacementsInvoker;
import com.futurebackport.platform.entity.SpawnPlacementRegistrar;
import com.futurebackport.registry.CreativePlacements;
import com.futurebackport.registry.ModEntities;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Fabric entry point: everything Forge does through events happens here, right after registration. Interaction with
 * entities (the golden dandelion age lock) goes through PlayerMixin, where Forge fires EntityInteract.
 */
public class FutureBackportFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FutureBackport.init();
        FabricRegistrationFactory.registerAll();

        ModEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        ModEntities.registerSpawnPlacements(new SpawnPlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacementsInvoker.futurebackport$register(type, placement, heightmap, predicate);
            }
        });
        FutureBackport.addBlockEntityBlocks((type, blocks) -> {
            BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) type;
            Set<Block> valid = new HashSet<>(accessor.futurebackport$getValidBlocks());
            valid.addAll(List.of(blocks));
            accessor.futurebackport$setValidBlocks(valid);
        });
        FutureBackport.commonSetup();
        ForgeDataOnFabric.apply();

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) -> BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab)
                .ifPresent(key -> CreativePlacements.apply(key, (after, item) -> entries.addAfter(after, item))));
        EntityTrackingEvents.START_TRACKING.register(FarmAnimalVariantEvents::onStartTracking);
    }
}
