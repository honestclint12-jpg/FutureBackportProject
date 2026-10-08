package com.futurebackport.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** A registered block. Replaces NeoForge's {@code DeferredBlock}. */
public final class BlockEntry<B extends Block> implements RegistryEntry<Block, B>, ItemLike {

    private final RegistryEntry<Block, B> entry;

    BlockEntry(RegistryEntry<Block, B> entry) {
        this.entry = entry;
    }

    @Override
    public B get() {
        return entry.get();
    }

    @Override
    public ResourceLocation getId() {
        return entry.getId();
    }

    @Override
    public ResourceKey<Block> getKey() {
        return entry.getKey();
    }

    @Override
    public Holder<Block> holder() {
        return entry.holder();
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
