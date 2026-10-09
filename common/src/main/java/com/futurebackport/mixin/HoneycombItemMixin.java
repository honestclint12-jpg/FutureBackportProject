package com.futurebackport.mixin;

import com.futurebackport.registry.ModDataMaps;
import java.util.Optional;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Honeycomb waxes the mod's copper blocks (1.20.1's waxing map is fixed to vanilla blocks). */
@Mixin(HoneycombItem.class)
public abstract class HoneycombItemMixin {

   @Inject(method = "getWaxed", at = @At("HEAD"), cancellable = true)
   private static void futurebackport$waxModBlocks(BlockState state, CallbackInfoReturnable<Optional<BlockState>> cir) {
      ModDataMaps.waxed(state.getBlock()).ifPresent(waxed -> cir.setReturnValue(Optional.of(waxed.withPropertiesOf(state))));
   }
}
