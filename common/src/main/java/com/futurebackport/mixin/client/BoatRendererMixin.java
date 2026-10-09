package com.futurebackport.mixin.client;

import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Textures for namespaced boat types: futurebackport:textures/entity/(chest_)boat/pale_oak.png. In 1.20.1 the
 * renderer builds the path as a string, so the namespace has to lead it (vanilla would put the colon in the path).
 */
@Mixin(BoatRenderer.class)
abstract class BoatRendererMixin {

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/vehicle/Boat$Type;Z)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void futurebackport$texture(Boat.Type type, boolean chest, CallbackInfoReturnable<String> cir) {
        if (type.getName().indexOf(':') >= 0) {
            ResourceLocation name = new ResourceLocation(type.getName());
            cir.setReturnValue(name.withPath(path -> "textures/entity/" + (chest ? "chest_boat/" : "boat/") + path + ".png").toString());
        }
    }
}
