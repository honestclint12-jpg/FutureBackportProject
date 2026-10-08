package com.futurebackport.fabric.mixin.client;

import com.futurebackport.block.entity.CopperChestBlockEntity;
import com.futurebackport.client.renderer.CopperChestRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Copper chest textures. NeoForge has an overridable ChestRenderer#getMaterial; vanilla asks Sheets directly. */
@Mixin(ChestRenderer.class)
abstract class ChestRendererMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Sheets;chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;"))
    private Material futurebackport$copperChestMaterial(BlockEntity blockEntity, ChestType type, boolean christmas, Operation<Material> original) {
        if (blockEntity instanceof CopperChestBlockEntity copperChest) {
            return CopperChestRenderer.material(copperChest, type);
        }
        return original.call(blockEntity, type, christmas);
    }
}
