package com.futurebackport.neoforge;

import com.futurebackport.FutureBackport;
import com.futurebackport.neoforge.platform.NeoForgeNetworkService;
import com.futurebackport.neoforge.platform.NeoForgeRegistrationFactory;
import com.futurebackport.platform.entity.SpawnPlacementRegistrar;
import com.futurebackport.registry.CreativePlacements;
import com.futurebackport.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/** NeoForge entry point: connects NeoForge's registration and lifecycle events to the shared mod code. */
@Mod(FutureBackport.MODID)
public class FutureBackportNeoForge {

    public FutureBackportNeoForge(IEventBus modBus) {
        NeoForgeRegistrationFactory.attach(modBus);
        FutureBackport.init();
        NeoForgeBiomeModifiers.register(modBus);

        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(FutureBackport::commonSetup));
        modBus.addListener((BlockEntityTypeAddBlocksEvent event) -> FutureBackport.addBlockEntityBlocks(event::modify));
        modBus.addListener((EntityAttributeCreationEvent event) -> ModEntities.registerAttributes(event::put));
        modBus.addListener(FutureBackportNeoForge::registerSpawnPlacements);
        modBus.addListener(NeoForgeNetworkService::register);
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> CreativePlacements.apply(
                event.getTabKey(), (after, item) -> event.insertAfter(after, item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));

        NeoForgeGameEvents.register(NeoForge.EVENT_BUS);
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        ModEntities.registerSpawnPlacements(new SpawnPlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }
}
