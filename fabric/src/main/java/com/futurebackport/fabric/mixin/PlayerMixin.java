package com.futurebackport.fabric.mixin;

import com.futurebackport.entity.AgeLock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stands in for NeoForge's EntityInteract event, which fires inside interactOn (after the spectator check). Fabric's
 * UseEntityCallback only fires from the network handlers, so fake players and direct calls would skip it.
 */
@Mixin(Player.class)
abstract class PlayerMixin {

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void futurebackport$interactOn(Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Player self = (Player) (Object) this;
        if (!self.isSpectator()) {
            InteractionResult result = AgeLock.onInteract(self, self.level(), target, self.getItemInHand(hand));
            if (result != null) {
                cir.setReturnValue(result);
            }
        }
    }
}
