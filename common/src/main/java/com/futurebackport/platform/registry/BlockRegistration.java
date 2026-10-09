package com.futurebackport.platform.registry;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Block registration with the same helpers as NeoForge's {@code DeferredRegister.Blocks}. */
public final class BlockRegistration {

    private final RegistrationProvider<Block> provider;

    private BlockRegistration(String modId) {
        this.provider = RegistrationProvider.create(Registries.BLOCK, modId);
    }

    public static BlockRegistration create(String modId) {
        return new BlockRegistration(modId);
    }

    public <B extends Block> BlockEntry<B> register(String name, Supplier<? extends B> factory) {
        return new BlockEntry<>(provider.<B>register(name, factory));
    }

    public <B extends Block> BlockEntry<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> factory, BlockBehaviour.Properties properties) {
        return register(name, () -> factory.apply(properties));
    }

    public BlockEntry<Block> registerSimpleBlock(String name, BlockBehaviour.Properties properties) {
        return registerBlock(name, Block::new, properties);
    }

    public RegistrationProvider<Block> provider() {
        return provider;
    }
}
