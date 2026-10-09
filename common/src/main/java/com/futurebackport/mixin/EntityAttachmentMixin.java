package com.futurebackport.mixin;

import com.futurebackport.platform.attachment.AttachmentHolder;
import com.futurebackport.platform.attachment.MixinAttachmentService;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives every entity the attachment map behind {@link MixinAttachmentService}, saved with the entity. */
@Mixin(Entity.class)
public abstract class EntityAttachmentMixin implements AttachmentHolder {

    @Unique
    private final Map<String, Object> futurebackport$attachments = new HashMap<>();

    @Override
    public Map<String, Object> futurebackport$attachments() {
        return this.futurebackport$attachments;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void futurebackport$saveAttachments(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        MixinAttachmentService.save((Entity) (Object) this, cir.getReturnValue());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void futurebackport$loadAttachments(CompoundTag tag, CallbackInfo ci) {
        MixinAttachmentService.load((Entity) (Object) this, tag);
    }
}
