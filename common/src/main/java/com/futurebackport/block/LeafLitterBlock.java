package com.futurebackport.block;

import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeafLitterBlock extends BushBlock {
   public static final MapCodec<LeafLitterBlock> CODEC = simpleCodec(LeafLitterBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final IntegerProperty SEGMENT_AMOUNT = IntegerProperty.create("segment_amount", 1, 4);
   private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

   public LeafLitterBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(SEGMENT_AMOUNT, 1)
      );
      UnmodifiableIterator var2 = this.stateDefinition.getPossibleStates().iterator();

      while (var2.hasNext()) {
         BlockState state = (BlockState)var2.next();
         this.shapes.put(state, segmentShape((Direction)state.getValue(FACING), (Integer)state.getValue(SEGMENT_AMOUNT), 1.0));
      }
   }

   static VoxelShape segmentShape(Direction facing, int count, double height) {
      VoxelShape shape = Shapes.empty();
      Direction direction = facing;

      for (int i = 0; i < count; i++) {
         shape = Shapes.or(shape, quarter(direction, height));
         direction = direction.getCounterClockWise();
      }

      return shape;
   }

   private static VoxelShape quarter(Direction direction, double height) {
      return switch (direction) {
         case NORTH -> Block.box(0.0, 0.0, 0.0, 8.0, height, 8.0);
         case EAST -> Block.box(8.0, 0.0, 0.0, 16.0, height, 8.0);
         case SOUTH -> Block.box(8.0, 0.0, 8.0, 16.0, height, 16.0);
         default -> Block.box(0.0, 0.0, 8.0, 8.0, height, 16.0);
      };
   }

   protected MapCodec<LeafLitterBlock> codec() {
      return CODEC;
   }

   protected BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   protected BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(SEGMENT_AMOUNT) < 4
         ? true
         : super.canBeReplaced(state, context);
   }

   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos below = pos.below();
      return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapes.get(state);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = context.getLevel().getBlockState(context.getClickedPos());
      return state.is(this)
         ? (BlockState)state.setValue(SEGMENT_AMOUNT, Math.min(4, (Integer)state.getValue(SEGMENT_AMOUNT) + 1))
         : (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, SEGMENT_AMOUNT});
   }
}
