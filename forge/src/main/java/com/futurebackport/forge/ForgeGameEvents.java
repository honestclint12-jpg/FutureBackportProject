package com.futurebackport.forge;

import com.futurebackport.entity.AgeLock;
import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.entity.SoundVariants;
import com.futurebackport.entity.nautilus.BreathOfTheNautilus;
import com.futurebackport.registry.ModDataMaps;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Forwards Forge game-bus events to the shared handlers. */
final class ForgeGameEvents {

    private ForgeGameEvents() {
    }

    static void register(IEventBus bus) {
        bus.addListener((MobSpawnEvent.FinalizeSpawn event) -> {
            FarmAnimalVariantEvents.onFinalizeSpawn(event.getEntity(), event.getLevel(), event.getSpawnType());
            SoundVariants.onFinalizeSpawn(event.getEntity(), event.getLevel());
        });
        bus.addListener((BabyEntitySpawnEvent event) -> FarmAnimalVariantEvents.onBabySpawn(event.getParentA(), event.getParentB(), event.getChild()));
        bus.addListener((PlayerEvent.StartTracking event) -> FarmAnimalVariantEvents.onStartTracking(event.getTarget(), event.getEntity()));
        bus.addListener((PlayerInteractEvent.EntityInteract event) -> {
            InteractionResult result = AgeLock.onInteract(event.getEntity(), event.getLevel(), event.getTarget(), event.getItemStack());
            if (result != null) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        });
        bus.addListener((LivingEvent.LivingTickEvent event) -> AgeLock.onTick(event.getEntity()));
        bus.addListener(ForgeGameEvents::toolModification);
        bus.addListener((LivingBreatheEvent event) -> {
            if (event.getEntity() instanceof HappyGhast ghast && !ghast.canDrown()) {
                event.setCanBreathe(true);
            }
            if (BreathOfTheNautilus.canBreathe(event.getEntity())) {
                event.setCanBreathe(true);
                if (BreathOfTheNautilus.blocksAirRefill(event.getEntity())) {
                    event.setRefillAirAmount(0);
                }
            }
        });
    }

    /** Stripping, scraping and wax removal for the mod's blocks (the 1.20.1 vanilla maps only know vanilla blocks). */
    private static void toolModification(BlockEvent.BlockToolModificationEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        if (event.getToolAction() == ToolActions.AXE_STRIP) {
            ModDataMaps.stripped(block).ifPresent(stripped -> event.setFinalState(stripped.withPropertiesOf(state)));
        } else if (event.getToolAction() == ToolActions.AXE_SCRAPE) {
            ModDataMaps.previousOxidized(block).ifPresent(previous -> event.setFinalState(previous.withPropertiesOf(state)));
        } else if (event.getToolAction() == ToolActions.AXE_WAX_OFF) {
            ModDataMaps.unwaxed(block).ifPresent(unwaxed -> event.setFinalState(unwaxed.withPropertiesOf(state)));
        }
    }
}
