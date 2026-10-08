package com.futurebackport.registry;

import java.util.function.Supplier;
import net.minecraft.world.entity.vehicle.Boat.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class ModBoats {
   public static final EnumProxy<Type> PALE_OAK = new EnumProxy(
      Type.class,
      new Object[]{
         (Supplier<Block>)() -> (Block)ModBlocks.PALE_OAK_PLANKS.get(),
         "futurebackport:pale_oak",
         (Supplier<Item>)() -> (Item)ModItems.PALE_OAK_BOAT.get(),
         (Supplier<Item>)() -> (Item)ModItems.PALE_OAK_CHEST_BOAT.get(),
         (Supplier<Item>)() -> Items.STICK,
         false
      }
   );
}
