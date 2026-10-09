package com.futurebackport.forge;

import com.futurebackport.FutureBackport;
import com.futurebackport.forge.loot.AddTableLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Loot modifier types used by data/futurebackport/loot_modifiers (listed in data/forge/loot_modifiers). */
public final class ForgeLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FutureBackport.MODID);
    public static final RegistryObject<Codec<AddTableLootModifier>> ADD_TABLE = LOOT_MODIFIERS.register("add_table", () -> AddTableLootModifier.CODEC);

    private ForgeLootModifiers() {
    }

    static void register(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
    }
}
