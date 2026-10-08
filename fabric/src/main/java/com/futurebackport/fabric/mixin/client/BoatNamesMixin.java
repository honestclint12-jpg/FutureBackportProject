package com.futurebackport.fabric.mixin.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Model layers for namespaced boat types ("futurebackport:pale_oak" becomes futurebackport:boat/pale_oak), as NeoForge
 * patches them. Vanilla would put the colon in a minecraft: path and crash.
 */
@Mixin(ModelLayers.class)
abstract class BoatNamesMixin {

    @Inject(method = "createBoatModelName", at = @At("HEAD"), cancellable = true)
    private static void futurebackport$boat(Boat.Type type, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (type.getName().indexOf(':') >= 0) {
            cir.setReturnValue(new ModelLayerLocation(prefixed(type, "boat/"), "main"));
        }
    }

    @Inject(method = "createChestBoatModelName", at = @At("HEAD"), cancellable = true)
    private static void futurebackport$chestBoat(Boat.Type type, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (type.getName().indexOf(':') >= 0) {
            cir.setReturnValue(new ModelLayerLocation(prefixed(type, "chest_boat/"), "main"));
        }
    }

    private static ResourceLocation prefixed(Boat.Type type, String prefix) {
        ResourceLocation name = ResourceLocation.parse(type.getName());
        return name.withPrefix(prefix);
    }
}
