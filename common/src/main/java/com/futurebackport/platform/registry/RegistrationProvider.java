package com.futurebackport.platform.registry;

import com.futurebackport.platform.Services;
import java.util.Collection;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Loader-neutral replacement for NeoForge's {@code DeferredRegister}. NeoForge wraps a real DeferredRegister,
 * Fabric registers immediately.
 */
public interface RegistrationProvider<R> {

    static <R> RegistrationProvider<R> create(ResourceKey<? extends Registry<R>> registry, String modId) {
        return Services.REGISTRATION.create(registry, modId);
    }

    <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory);

    /** Every entry registered so far, in registration order. */
    Collection<RegistryEntry<R, ? extends R>> getEntries();

    String getModId();
}
