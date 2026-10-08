package com.futurebackport.neoforge.platform;

import com.futurebackport.platform.attachment.DataAttachment;
import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;
import com.futurebackport.platform.services.AttachmentService;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class NeoForgeAttachmentService implements AttachmentService {

    private RegistrationProvider<AttachmentType<?>> types;

    @Override
    public synchronized <T> DataAttachment<T> register(String modId, String name, Supplier<T> defaultValue, @Nullable Codec<T> codec, boolean copyOnDeath) {
        if (types == null) {
            types = RegistrationProvider.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, modId);
        }
        RegistryEntry<AttachmentType<?>, AttachmentType<T>> type = types.register(name, () -> {
            AttachmentType.Builder<T> builder = AttachmentType.builder(defaultValue);
            if (codec != null) {
                builder.serialize(codec);
            }
            if (copyOnDeath) {
                builder.copyOnDeath();
            }
            return builder.build();
        });
        return new DataAttachment<>() {
            @Override
            public T get(Entity entity) {
                return entity.getData(type.get());
            }

            @Override
            public void set(Entity entity, T value) {
                entity.setData(type.get(), value);
            }
        };
    }
}
