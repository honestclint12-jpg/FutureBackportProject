package com.futurebackport.block;

import com.futurebackport.FutureBackport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusFlowerBlock extends BushBlock {
   public static final TagKey<Block> SUPPORT_OVERRIDE = TagKey.create(Registries.BLOCK, FutureBackport.id("support_override_cactus_flower"));
   private static final VoxelShape SHAPE = box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0);

   public CactusFlowerBlock(Properties properties) {
      super(properties);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return state.is(SUPPORT_OVERRIDE) || state.isFaceSturdy(level, pos, Direction.UP, SupportType.CENTER);
   }
}
