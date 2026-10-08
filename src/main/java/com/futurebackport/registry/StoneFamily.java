package com.futurebackport.registry;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.neoforge.registries.DeferredBlock;

public record StoneFamily(DeferredBlock<Block> base, DeferredBlock<SlabBlock> slab, DeferredBlock<StairBlock> stairs, DeferredBlock<WallBlock> wall) {
   public static StoneFamily register(String baseName, String variantPrefix, Supplier<Properties> properties) {
      DeferredBlock<Block> base = ModBlocks.BLOCKS.register(baseName, () -> new Block(properties.get()));
      DeferredBlock<SlabBlock> slab = ModBlocks.BLOCKS
         .register(variantPrefix + "_slab", () -> new SlabBlock(Properties.ofLegacyCopy((BlockBehaviour)base.get())));
      DeferredBlock<StairBlock> stairs = ModBlocks.BLOCKS
         .register(variantPrefix + "_stairs", () -> new StairBlock(((Block)base.get()).defaultBlockState(), Properties.ofFullCopy((BlockBehaviour)base.get())));
      DeferredBlock<WallBlock> wall = ModBlocks.BLOCKS
         .register(variantPrefix + "_wall", () -> new WallBlock(Properties.ofLegacyCopy((BlockBehaviour)base.get()).forceSolidOn()));
      return new StoneFamily(base, slab, stairs, wall);
   }

   public List<DeferredBlock<? extends Block>> all() {
      return List.of(this.base, this.stairs, this.slab, this.wall);
   }
}
