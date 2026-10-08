package com.futurebackport.platform.attachment;

import net.minecraft.world.entity.Entity;

/**
 * A value stored on an entity and saved with it. NeoForge backs it with an {@code AttachmentType}, Fabric with the
 * Data Attachment API. Create through {@link com.futurebackport.platform.Services#ATTACHMENTS}.
 */
public interface DataAttachment<T> {

    T get(Entity entity);

    void set(Entity entity, T value);
}
