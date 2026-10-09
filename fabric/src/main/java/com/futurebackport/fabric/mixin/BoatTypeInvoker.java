package com.futurebackport.fabric.mixin;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The enum constructor of Boat.Type, used by BoatTypeMixin to add the pale oak constant. */
@Mixin(Boat.Type.class)
public interface BoatTypeInvoker {

    @Invoker("<init>")
    static Boat.Type futurebackport$create(String constant, int ordinal, Block planks, String name) {
        throw new AssertionError();
    }
}
