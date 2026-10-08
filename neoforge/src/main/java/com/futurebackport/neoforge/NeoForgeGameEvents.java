package com.futurebackport.neoforge;

import com.futurebackport.entity.AgeLock;
import com.futurebackport.entity.FarmAnimalVariantEvents;
import com.futurebackport.entity.HappyGhast;
import com.futurebackport.entity.SoundVariants;
import com.futurebackport.entity.nautilus.BreathOfTheNautilus;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Forwards NeoForge game-bus events to the shared handlers. */
final class NeoForgeGameEvents {

    private NeoForgeGameEvents() {
    }

    static void register(IEventBus bus) {
        bus.addListener((FinalizeSpawnEvent event) -> {
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
        bus.addListener((EntityTickEvent.Post event) -> AgeLock.onTick(event.getEntity()));
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
}
