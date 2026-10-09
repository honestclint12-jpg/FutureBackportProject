package com.futurebackport.forge.platform;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Backs every {@link RegistrationProvider} with a Forge {@link DeferredRegister}. */
public final class ForgeRegistrationFactory implements RegistrationFactory {

    private static final List<DeferredRegister<?>> REGISTERS = new ArrayList<>();
    private static IEventBus modBus;

    /** Called once from the mod constructor. Providers created later attach to the bus immediately. */
    public static synchronized void attach(IEventBus bus) {
        modBus = bus;
        REGISTERS.forEach(register -> register.register(bus));
    }

    @Override
    public <R> RegistrationProvider<R> create(ResourceKey<? extends Registry<R>> registry, String modId) {
        DeferredRegister<R> register = DeferredRegister.create(registry, modId);
        synchronized (ForgeRegistrationFactory.class) {
            REGISTERS.add(register);
            if (modBus != null) {
                register.register(modBus);
            }
        }
        return new Provider<>(register, modId);
    }

    private static final class Provider<R> implements RegistrationProvider<R> {
        private final DeferredRegister<R> register;
        private final String modId;
        private final List<RegistryEntry<R, ? extends R>> entries = new ArrayList<>();

        Provider(DeferredRegister<R> register, String modId) {
            this.register = register;
            this.modId = modId;
        }

        @Override
        public <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory) {
            Entry<R, T> entry = new Entry<>(register.register(name, factory));
            entries.add(entry);
            return entry;
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

    private record Entry<R, T extends R>(RegistryObject<T> object) implements RegistryEntry<R, T> {
        @Override
        public T get() {
            return object.get();
        }

        @Override
        public ResourceLocation getId() {
            return object.getId();
        }

        @Override
        @SuppressWarnings("unchecked")
        public ResourceKey<R> getKey() {
            return (ResourceKey<R>) object.getKey();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Holder<R> holder() {
            return (Holder<R>) object.getHolder().orElseThrow(() -> new IllegalStateException("No holder for " + object.getId()));
        }
    }
}
