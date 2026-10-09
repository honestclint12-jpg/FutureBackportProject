package com.futurebackport.registry;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModWoodTypes {
   public static final BlockSetType PALE_OAK_SET = BlockSetType.register(new BlockSetType("futurebackport:pale_oak"));
   public static final WoodType PALE_OAK = WoodType.register(new WoodType("futurebackport:pale_oak", PALE_OAK_SET));
}
