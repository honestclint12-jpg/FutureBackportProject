package com.futurebackport.platform.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Item registration with the same helpers as NeoForge's {@code DeferredRegister.Items}. */
public final class ItemRegistration {

    private final RegistrationProvider<Item> provider;
    private final List<ItemEntry<? extends Item>> entries = new ArrayList<>();

    private ItemRegistration(String modId) {
        this.provider = RegistrationProvider.create(Registries.ITEM, modId);
    }

    public static ItemRegistration create(String modId) {
        return new ItemRegistration(modId);
    }

    public <I extends Item> ItemEntry<I> register(String name, Supplier<? extends I> factory) {
        ItemEntry<I> entry = new ItemEntry<>(provider.<I>register(name, factory));
        entries.add(entry);
        return entry;
    }

    public <I extends Item> ItemEntry<I> registerItem(String name, Function<Item.Properties, ? extends I> factory, Item.Properties properties) {
        return register(name, () -> factory.apply(properties));
    }

    public ItemEntry<Item> registerSimpleItem(String name) {
        return registerSimpleItem(name, new Item.Properties());
    }

    public ItemEntry<Item> registerSimpleItem(String name, Item.Properties properties) {
        return registerItem(name, Item::new, properties);
    }

    public ItemEntry<BlockItem> registerSimpleBlockItem(BlockEntry<? extends Block> block) {
        return registerSimpleBlockItem(block, new Item.Properties());
    }

    public ItemEntry<BlockItem> registerSimpleBlockItem(BlockEntry<? extends Block> block, Item.Properties properties) {
        return register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }

    /** Every item registered so far, in registration order. */
    public List<ItemEntry<? extends Item>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public RegistrationProvider<Item> provider() {
        return provider;
    }
}
