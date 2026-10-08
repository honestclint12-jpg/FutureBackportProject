package com.futurebackport.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** A registered item. Replaces NeoForge's {@code DeferredItem}. */
public final class ItemEntry<I extends Item> implements RegistryEntry<Item, I>, ItemLike {

    private final RegistryEntry<Item, I> entry;

    ItemEntry(RegistryEntry<Item, I> entry) {
        this.entry = entry;
    }

    @Override
    public I get() {
        return entry.get();
    }

    @Override
    public ResourceLocation getId() {
        return entry.getId();
    }

    @Override
    public ResourceKey<Item> getKey() {
        return entry.getKey();
    }

    @Override
    public Holder<Item> holder() {
        return entry.holder();
    }

    @Override
    public Item asItem() {
        return get();
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        return new ItemStack(get(), count);
    }
}
