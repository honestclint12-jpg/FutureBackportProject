package com.futurebackport.neoforge;

import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModBoats;
import com.futurebackport.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/** Parameters for the pale oak {@link Boat.Type} constant declared in META-INF/enumextensions.json. */
public final class NeoForgeBoats {

    public static final EnumProxy<Boat.Type> PALE_OAK = new EnumProxy<>(
            Boat.Type.class,
            (Supplier<Block>) () -> ModBlocks.PALE_OAK_PLANKS.get(),
            ModBoats.PALE_OAK_NAME,
            (Supplier<Item>) () -> ModItems.PALE_OAK_BOAT.get(),
            (Supplier<Item>) () -> ModItems.PALE_OAK_CHEST_BOAT.get(),
            (Supplier<Item>) () -> Items.STICK,
            false
    );

    private NeoForgeBoats() {
    }
}
