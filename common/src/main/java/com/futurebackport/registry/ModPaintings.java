package com.futurebackport.registry;

import com.futurebackport.platform.registry.RegistrationProvider;
import com.futurebackport.platform.registry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;

/** Painting variants are code-registered in 1.20.1 (sizes in pixels); the texture is textures/painting/{name}.png. */
public class ModPaintings {
   public static final RegistrationProvider<PaintingVariant> PAINTINGS = RegistrationProvider.create(Registries.PAINTING_VARIANT, "futurebackport");
   public static final RegistryEntry<PaintingVariant, PaintingVariant> DENNIS = PAINTINGS.register("dennis", () -> new PaintingVariant(48, 48));
}
