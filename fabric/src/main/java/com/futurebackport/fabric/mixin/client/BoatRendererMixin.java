package com.futurebackport.fabric.mixin.client;

import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Textures for namespaced boat types: futurebackport:textures/entity/(chest_)boat/pale_oak.png, as on NeoForge. */
@Mixin(BoatRenderer.class)
abstract class BoatRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/vehicle/Boat$Type;Z)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"), cancellable = true)
    private static void futurebackport$texture(Boat.Type type, boolean chest, CallbackInfoReturnable<ResourceLocation> cir) {
        if (type.getName().indexOf(':') >= 0) {
            ResourceLocation name = ResourceLocation.parse(type.getName());
            cir.setReturnValue(name.withPath(path -> "textures/entity/" + (chest ? "chest_boat/" : "boat/") + path + ".png"));
        }
    }
}
