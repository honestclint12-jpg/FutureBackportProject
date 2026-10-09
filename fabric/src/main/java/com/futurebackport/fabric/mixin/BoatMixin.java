package com.futurebackport.fabric.mixin;

import com.futurebackport.registry.ModBoats;
import com.futurebackport.registry.ModItems;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pale oak boats drop pale oak boat items. NeoForge reads the item from the enum extension; vanilla falls back to oak. */
@Mixin({Boat.class, ChestBoat.class})
abstract class BoatMixin {

    @Inject(method = "getDropItem", at = @At("HEAD"), cancellable = true)
    private void futurebackport$paleOakDrop(CallbackInfoReturnable<Item> cir) {
        Boat self = (Boat) (Object) this;
        if (self.getVariant() == ModBoats.paleOak()) {
            cir.setReturnValue(self instanceof ChestBoat ? ModItems.PALE_OAK_CHEST_BOAT.get() : ModItems.PALE_OAK_BOAT.get());
        }
    }
}
