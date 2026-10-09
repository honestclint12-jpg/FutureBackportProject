package com.futurebackport.forge;

import com.futurebackport.FutureBackport;
import com.futurebackport.forge.worldgen.SetSpawnWeightModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Biome modifier types used by the JSON files in data/futurebackport/forge/biome_modifier.
 * Fabric applies the same files in code (ForgeDataOnFabric).
 */
public final class ForgeBiomeModifiers {

    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, FutureBackport.MODID);
    public static final RegistryObject<Codec<SetSpawnWeightModifier>> SET_SPAWN_WEIGHT =
            BIOME_MODIFIERS.register("set_spawn_weight", () -> SetSpawnWeightModifier.CODEC);

    private ForgeBiomeModifiers() {
    }

    static void register(IEventBus modBus) {
        BIOME_MODIFIERS.register(modBus);
    }
}
