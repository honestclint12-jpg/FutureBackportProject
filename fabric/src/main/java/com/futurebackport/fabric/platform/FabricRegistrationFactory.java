package com.futurebackport.fabric.platform;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;
import com.futurebackport.platform.services.RegistrationFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric has no registration events, but the shared code is written for NeoForge's deferred model: entries are
 * declared during class initialisation and their factories may reference entries of other registries. So entries are
 * queued and {@link #registerAll()} creates them registry by registry (sounds, effects, particles, armor materials,
 * blocks, entity types, items, then vanilla's order), the way NeoForge's register events do.
 */
public final class FabricRegistrationFactory implements RegistrationFactory {

    private static final List<Provider<?>> PROVIDERS = new ArrayList<>();
    /** Armor materials hold sound events and are held by armor items, so they can't wait for vanilla's slot after items. */
    private static final List<ResourceKey<?>> FIRST = List.of(
            Registries.SOUND_EVENT, Registries.MOB_EFFECT, Registries.PARTICLE_TYPE,
            Registries.BLOCK, Registries.ENTITY_TYPE, Registries.ITEM);
    private static boolean registered;

    @Override
    public synchronized <R> RegistrationProvider<R> create(ResourceKey<? extends Registry<R>> registry, String modId) {
        if (registered) {
            throw new IllegalStateException("Registry " + registry.location() + " for " + modId + " created after registration");
        }
        Provider<R> provider = new Provider<>(registry, modId);
        PROVIDERS.add(provider);
        return provider;
    }

    /** Called once by the Fabric entrypoint, after the shared code has declared everything. */
    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        // Registries other content refers to at construction time go first; the rest follow vanilla's declaration
        // order (iterating the registry of registries; keySet() would be unordered).
        List<ResourceLocation> order = new ArrayList<>();
        for (ResourceKey<?> first : FIRST) {
            order.add(first.location());
        }
        for (Registry<?> builtIn : BuiltInRegistries.REGISTRY) {
            if (!order.contains(builtIn.key().location())) {
                order.add(builtIn.key().location());
            }
        }
        for (ResourceLocation registryId : order) {
            for (Provider<?> provider : PROVIDERS) {
                if (provider.registryKey.location().equals(registryId)) {
                    provider.registerPending();
                }
            }
        }
        for (Provider<?> provider : PROVIDERS) {
            if (!provider.pending.isEmpty()) {
                throw new IllegalStateException("No built-in registry " + provider.registryKey.location());
            }
        }
    }

    private static final class Provider<R> implements RegistrationProvider<R> {
        private final ResourceKey<? extends Registry<R>> registryKey;
        private final String modId;
        private final List<RegistryEntry<R, ? extends R>> entries = new ArrayList<>();
        private final List<Entry<R, ?>> pending = new ArrayList<>();

        Provider(ResourceKey<? extends Registry<R>> registryKey, String modId) {
            this.registryKey = registryKey;
            this.modId = modId;
        }

        @Override
        public synchronized <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory) {
            if (registered) {
                throw new IllegalStateException("Too late to register " + modId + ":" + name);
            }
            ResourceLocation id = new ResourceLocation(modId, name);
            Entry<R, T> entry = new Entry<>(id, ResourceKey.create(registryKey, id), factory);
            entries.add(entry);
            pending.add(entry);
            return entry;
        }

        @SuppressWarnings("unchecked")
        void registerPending() {
            Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(registryKey.location());
            for (Entry<R, ?> entry : pending) {
                entry.register(registry);
            }
            pending.clear();
        }

        @Override
        public Collection<RegistryEntry<R, ? extends R>> getEntries() {
            return Collections.unmodifiableList(entries);
        }

        @Override
        public String getModId() {
            return modId;
        }
    }

    private static final class Entry<R, T extends R> implements RegistryEntry<R, T> {
        private final ResourceLocation id;
        private final ResourceKey<R> key;
        private Supplier<? extends T> factory;
        private T value;
        private Holder<R> holder;

        Entry(ResourceLocation id, ResourceKey<R> key, Supplier<? extends T> factory) {
            this.id = id;
            this.key = key;
            this.factory = factory;
        }

        void register(Registry<R> registry) {
            value = factory.get();
            holder = Registry.registerForHolder(registry, key, value);
            factory = null;
        }

        @Override
        public T get() {
            if (value == null) {
                throw new IllegalStateException(id + " is not registered yet");
            }
            return value;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public ResourceKey<R> getKey() {
            return key;
        }

        @Override
        public Holder<R> holder() {
            if (holder == null) {
                throw new IllegalStateException(id + " is not registered yet");
            }
            return holder;
        }
    }
}
