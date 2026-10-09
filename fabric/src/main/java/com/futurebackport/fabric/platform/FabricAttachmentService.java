package com.futurebackport.fabric.platform;

import com.futurebackport.platform.attachment.DataAttachment;
import com.futurebackport.platform.services.AttachmentService;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class FabricAttachmentService implements AttachmentService {

    @Override
    public <T> DataAttachment<T> register(String modId, String name, Supplier<T> defaultValue, @Nullable Codec<T> codec, boolean copyOnDeath) {
        AttachmentRegistry.Builder<T> builder = AttachmentRegistry.<T>builder().initializer(defaultValue);
        if (codec != null) {
            builder.persistent(codec);
        }
        if (copyOnDeath) {
            builder.copyOnDeath();
        }
        AttachmentType<T> type = builder.buildAndRegister(ResourceLocation.fromNamespaceAndPath(modId, name));
        return new DataAttachment<>() {
            @Override
            public T get(Entity entity) {
                return entity.getAttachedOrCreate(type);
            }

            @Override
            public void set(Entity entity, T value) {
                entity.setAttached(type, value);
            }
        };
    }
}
