package com.futurebackport.neoforge;

import com.futurebackport.FutureBackport;
import com.futurebackport.neoforge.worldgen.SetSpawnWeightModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Biome modifier types used by the JSON files in data/futurebackport/neoforge/biome_modifier.
 * Other loaders apply the same biome changes in code.
 */
public final class NeoForgeBiomeModifiers {

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, FutureBackport.MODID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<SetSpawnWeightModifier>> SET_SPAWN_WEIGHT =
            BIOME_MODIFIERS.register("set_spawn_weight", () -> SetSpawnWeightModifier.CODEC);

    private NeoForgeBiomeModifiers() {
    }

    static void register(IEventBus modBus) {
        BIOME_MODIFIERS.register(modBus);
    }
}
