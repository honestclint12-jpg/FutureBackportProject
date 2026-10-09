package com.futurebackport.mixin;

import com.futurebackport.platform.attachment.MixinAttachmentService;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps copy-on-death attachments when a player respawns. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerAttachmentMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void futurebackport$copyAttachments(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        MixinAttachmentService.copyOnDeath(oldPlayer, (ServerPlayer) (Object) this);
    }
}
