package com.futurebackport.fabric.mixin.client;

import com.futurebackport.client.DryFoliageColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers the dry foliage color resolver with the level's tint caches (Forge: ColorResolvers event; Fabric API has
 * no color resolver registry for 1.20.1, and an unknown resolver would have no cache).
 */
@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {

    @Shadow
    @Final
    private Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void futurebackport$addDryFoliageCache(CallbackInfo ci) {
        ClientLevel self = (ClientLevel) (Object) this;
        this.tintCaches.put(DryFoliageColor.RESOLVER, new BlockTintCache(pos -> self.calculateBlockTint(pos, DryFoliageColor.RESOLVER)));
    }
}
