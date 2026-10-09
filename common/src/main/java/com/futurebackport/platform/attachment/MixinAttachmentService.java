package com.futurebackport.platform.attachment;

import com.futurebackport.FutureBackport;
import com.futurebackport.platform.services.AttachmentService;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Entity attachments for 1.20.1, where neither loader has an attachment API (Forge has capabilities, Fabric API
 * added attachments later). Values live in a map that {@code EntityAttachmentMixin} adds to every entity and are
 * saved under {@value #TAG} in the entity's NBT. Both 1.20.1 loaders use this one implementation.
 */
public final class MixinAttachmentService implements AttachmentService {

    public static final String TAG = "futurebackport:attachments";
    private static final Map<String, Type<?>> TYPES = new ConcurrentHashMap<>();

    @Override
    public <T> DataAttachment<T> register(String modId, String name, Supplier<T> defaultValue, @Nullable Codec<T> codec, boolean copyOnDeath) {
        String id = modId + ":" + name;
        Type<T> type = new Type<>(id, defaultValue, codec, copyOnDeath);
        if (TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalStateException("Duplicate attachment " + id);
        }
        return type;
    }

    /** Writes every saved attachment of {@code entity} into {@code tag}. */
    public static void save(Entity entity, CompoundTag tag) {
        Map<String, Object> values = ((AttachmentHolder) entity).futurebackport$attachments();
        if (values.isEmpty()) {
            return;
        }
        CompoundTag out = new CompoundTag();
        values.forEach((id, value) -> {
            Type<?> type = TYPES.get(id);
            if (type != null && type.codec != null) {
                type.encode(value).ifPresent(encoded -> out.put(id, encoded));
            }
        });
        if (!out.isEmpty()) {
            tag.put(TAG, out);
        }
    }

    /** Reads the attachments {@link #save} wrote. */
    public static void load(Entity entity, CompoundTag tag) {
        if (!tag.contains(TAG, 10)) {
            return;
        }
        CompoundTag in = tag.getCompound(TAG);
        Map<String, Object> values = ((AttachmentHolder) entity).futurebackport$attachments();
        for (String id : in.getAllKeys()) {
            Type<?> type = TYPES.get(id);
            if (type == null || type.codec == null) {
                continue;
            }
            type.codec.parse(NbtOps.INSTANCE, in.get(id))
                    .resultOrPartial(error -> FutureBackport.LOGGER.warn("Could not load attachment {}: {}", id, error))
                    .ifPresent(value -> values.put(id, value));
        }
    }

    /** Copies the copy-on-death attachments from a dead player to its respawned self. */
    public static void copyOnDeath(Entity from, Entity to) {
        Map<String, Object> source = ((AttachmentHolder) from).futurebackport$attachments();
        Map<String, Object> target = ((AttachmentHolder) to).futurebackport$attachments();
        source.forEach((id, value) -> {
            Type<?> type = TYPES.get(id);
            if (type != null && type.copyOnDeath) {
                target.put(id, value);
            }
        });
    }

    private static final class Type<T> implements DataAttachment<T> {
        private final String id;
        private final Supplier<T> defaultValue;
        @Nullable
        private final Codec<T> codec;
        private final boolean copyOnDeath;

        Type(String id, Supplier<T> defaultValue, @Nullable Codec<T> codec, boolean copyOnDeath) {
            this.id = id;
            this.defaultValue = defaultValue;
            this.codec = codec;
            this.copyOnDeath = copyOnDeath;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(Entity entity) {
            return (T) ((AttachmentHolder) entity).futurebackport$attachments().computeIfAbsent(this.id, key -> this.defaultValue.get());
        }

        @Override
        public void set(Entity entity, T value) {
            ((AttachmentHolder) entity).futurebackport$attachments().put(this.id, value);
        }

        @SuppressWarnings("unchecked")
        java.util.Optional<net.minecraft.nbt.Tag> encode(Object value) {
            return this.codec.encodeStart(NbtOps.INSTANCE, (T) value).result();
        }
    }
}
