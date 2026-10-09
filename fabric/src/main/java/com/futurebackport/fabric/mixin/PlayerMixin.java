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
 * Stands in for Forge's EntityInteract event, which fires at the start of Player#interactOn. Fabric's
 * UseEntityCallback only covers interactions that arrive over the network, so fake players would skip the age lock.
 */
@Mixin(Player.class)
abstract class PlayerMixin {

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void futurebackport$ageLock(Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Player self = (Player) (Object) this;
        InteractionResult result = AgeLock.onInteract(self, self.level(), target, self.getItemInHand(hand));
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
