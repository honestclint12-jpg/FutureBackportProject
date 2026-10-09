package com.futurebackport.forge;

import com.futurebackport.FutureBackport;
import com.futurebackport.forge.client.FutureBackportForgeClient;
import com.futurebackport.forge.platform.ForgeNetworkService;
import com.futurebackport.forge.platform.ForgeRegistrationFactory;
import com.futurebackport.platform.entity.SpawnPlacementRegistrar;
import com.futurebackport.registry.CreativePlacements;
import com.futurebackport.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Forge entry point: connects Forge's registration and lifecycle events to the shared mod code. */
@Mod(FutureBackport.MODID)
public class FutureBackportForge {

    public FutureBackportForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistrationFactory.attach(modBus);
        FutureBackport.init();
        ForgeBiomeModifiers.register(modBus);
        ForgeLootModifiers.register(modBus);

        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            FutureBackport.commonSetup();
            ForgeNetworkService.register();
        }));
        modBus.addListener((EntityAttributeCreationEvent event) -> ModEntities.registerAttributes(event::put));
        modBus.addListener(FutureBackportForge::registerSpawnPlacements);
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> CreativePlacements.apply(
                event.getTabKey(), (after, item) -> event.getEntries().putAfter(after, item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));

        ForgeGameEvents.register(MinecraftForge.EVENT_BUS);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FutureBackportForgeClient.init();
        }
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        ModEntities.registerSpawnPlacements(new SpawnPlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacements.Type placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
            }
        });
    }
}
