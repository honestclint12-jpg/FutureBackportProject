package com.futurebackport.mixin;

import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModBoats;
import java.util.Arrays;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the pale oak constant to Boat.Type, as NeoForge's enum extension does. It goes in right after $VALUES is
 * assigned, so CODEC and BY_ID (built later in the static initializer) include it.
 */
@Mixin(Boat.Type.class)
abstract class BoatTypeMixin {
    @Shadow @Final @Mutable private static Boat.Type[] $VALUES;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.PUTSTATIC,
            target = "Lnet/minecraft/world/entity/vehicle/Boat$Type;$VALUES:[Lnet/minecraft/world/entity/vehicle/Boat$Type;", shift = At.Shift.AFTER))
    private static void futurebackport$addPaleOak(CallbackInfo ci) {
        Boat.Type[] values = Arrays.copyOf($VALUES, $VALUES.length + 1);
        // Mod blocks don't exist yet; getPlanks below answers with the real planks.
        values[values.length - 1] = BoatTypeInvoker.futurebackport$create("FUTUREBACKPORT_PALE_OAK", values.length - 1, Blocks.OAK_PLANKS, ModBoats.PALE_OAK_NAME);
        $VALUES = values;
    }

    @Inject(method = "getPlanks", at = @At("HEAD"), cancellable = true)
    private void futurebackport$paleOakPlanks(CallbackInfoReturnable<Block> cir) {
        if (((Boat.Type) (Object) this).getName().equals(ModBoats.PALE_OAK_NAME)) {
            cir.setReturnValue(ModBlocks.PALE_OAK_PLANKS.get());
        }
    }
}
