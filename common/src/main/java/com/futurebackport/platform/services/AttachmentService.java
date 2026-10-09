package com.futurebackport.platform.services;

import com.futurebackport.platform.attachment.DataAttachment;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public interface AttachmentService {

    /**
     * @param codec saves the value with the entity; {@code null} keeps it in memory only
     * @param copyOnDeath keep the value when a player respawns
     */
    <T> DataAttachment<T> register(String modId, String name, Supplier<T> defaultValue, @Nullable Codec<T> codec, boolean copyOnDeath);
}
