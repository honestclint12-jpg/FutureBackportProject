package com.futurebackport.platform.registry;

import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Something registered through a {@link RegistrationProvider}. Like NeoForge's {@code DeferredHolder}: the value may not
 * exist until the loader runs registration, so call {@link #get()} late (inside suppliers, after mod construction).
 */
public interface RegistryEntry<R, T extends R> extends Supplier<T> {

    ResourceLocation getId();

    ResourceKey<R> getKey();

    /** The registry holder, for vanilla APIs that take a {@code Holder} (mob effects, armor materials, ...). */
    Holder<R> holder();

    default T value() {
        return get();
    }
}
