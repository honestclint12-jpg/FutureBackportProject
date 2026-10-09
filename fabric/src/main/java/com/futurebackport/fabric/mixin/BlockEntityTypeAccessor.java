package com.futurebackport.fabric.mixin;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets pale oak signs use vanilla's sign block entity types (NeoForge: BlockEntityTypeAddBlocksEvent). */
@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {

    @Accessor("validBlocks")
    Set<Block> futurebackport$getValidBlocks();

    @Mutable
    @Accessor("validBlocks")
    void futurebackport$setValidBlocks(Set<Block> blocks);
}
