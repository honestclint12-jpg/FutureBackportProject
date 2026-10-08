package com.futurebackport.platform.services;

import com.futurebackport.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface RegistrationFactory {

    <R> RegistrationProvider<R> create(ResourceKey<? extends Registry<R>> registry, String modId);
}
