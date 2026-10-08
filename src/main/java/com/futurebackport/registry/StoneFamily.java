package com.futurebackport.registry;

import com.futurebackport.platform.registry.BlockEntry;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public record StoneFamily(BlockEntry<Block> base, BlockEntry<SlabBlock> slab, BlockEntry<StairBlock> stairs, BlockEntry<WallBlock> wall) {
   public static StoneFamily register(String baseName, String variantPrefix, Supplier<Properties> properties) {
      BlockEntry<Block> base = ModBlocks.BLOCKS.register(baseName, () -> new Block(properties.get()));
      BlockEntry<SlabBlock> slab = ModBlocks.BLOCKS
         .register(variantPrefix + "_slab", () -> new SlabBlock(Properties.ofLegacyCopy((BlockBehaviour)base.get())));
      BlockEntry<StairBlock> stairs = ModBlocks.BLOCKS
         .register(variantPrefix + "_stairs", () -> new StairBlock(((Block)base.get()).defaultBlockState(), Properties.ofFullCopy((BlockBehaviour)base.get())));
      BlockEntry<WallBlock> wall = ModBlocks.BLOCKS
         .register(variantPrefix + "_wall", () -> new WallBlock(Properties.ofLegacyCopy((BlockBehaviour)base.get()).forceSolidOn()));
      return new StoneFamily(base, slab, stairs, wall);
   }

   public List<BlockEntry<? extends Block>> all() {
      return List.of(this.base, this.stairs, this.slab, this.wall);
   }
}
