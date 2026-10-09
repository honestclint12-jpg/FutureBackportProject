package com.futurebackport.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MossyCarpetBlock extends Block implements BonemealableBlock {
   public static final BooleanProperty BASE = BlockStateProperties.BOTTOM;
   public static final EnumProperty<WallSide> NORTH = BlockStateProperties.NORTH_WALL;
   public static final EnumProperty<WallSide> EAST = BlockStateProperties.EAST_WALL;
   public static final EnumProperty<WallSide> SOUTH = BlockStateProperties.SOUTH_WALL;
   public static final EnumProperty<WallSide> WEST = BlockStateProperties.WEST_WALL;
   public static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION = ImmutableMap.of(
      Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST
   );
   private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

   public MossyCarpetBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(BASE, true))
                     .setValue(NORTH, WallSide.NONE))
                  .setValue(EAST, WallSide.NONE))
               .setValue(SOUTH, WallSide.NONE))
            .setValue(WEST, WallSide.NONE)
      );
      UnmodifiableIterator var2 = this.stateDefinition.getPossibleStates().iterator();

      while (var2.hasNext()) {
         BlockState state = (BlockState)var2.next();
         this.shapes.put(state, makeShape(state));
      }
   }

   private static VoxelShape sideShape(Direction direction, boolean tall) {
      double height = tall ? 16.0 : 10.0;

      return switch (direction) {
         case NORTH -> box(0.0, 0.0, 0.0, 16.0, height, 1.0);
         case SOUTH -> box(0.0, 0.0, 15.0, 16.0, height, 16.0);
         case WEST -> box(0.0, 0.0, 0.0, 1.0, height, 16.0);
         default -> box(15.0, 0.0, 0.0, 16.0, height, 16.0);
      };
   }

   private static VoxelShape makeShape(BlockState state) {
      VoxelShape shape = state.getValue(BASE) ? box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0) : Shapes.empty();

      for (Entry<Direction, EnumProperty<WallSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
         switch ((WallSide)state.getValue((Property)entry.getValue())) {
            case LOW:
               shape = Shapes.or(shape, sideShape(entry.getKey(), false));
               break;
            case TALL:
               shape = Shapes.or(shape, sideShape(entry.getKey(), true));
         }
      }

      return shape.isEmpty() ? Shapes.block() : shape;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapes.get(state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(BASE) ? this.shapes.get(this.defaultBlockState()) : Shapes.empty();
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState below = level.getBlockState(pos.below());
      return state.getValue(BASE) ? !below.isAir() : below.is(this) && (Boolean)below.getValue(BASE);
   }

   private static boolean hasFaces(BlockState state) {
      if ((Boolean)state.getValue(BASE)) {
         return true;
      } else {
         for (EnumProperty<WallSide> property : PROPERTY_BY_DIRECTION.values()) {
            if (state.getValue(property) != WallSide.NONE) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean canSupportAtFace(BlockGetter level, BlockPos pos, Direction direction) {
      if (direction == Direction.UP) {
         return false;
      } else {
         BlockPos neighbour = pos.relative(direction);
         return MultifaceBlock.canAttachTo(level, direction, neighbour, level.getBlockState(neighbour));
      }
   }

   private BlockState getUpdatedState(BlockState state, BlockGetter level, BlockPos pos, boolean createSides) {
      BlockState above = null;
      BlockState below = null;
      createSides |= state.getValue(BASE);

      for (Direction direction : Plane.HORIZONTAL) {
         EnumProperty<WallSide> property = PROPERTY_BY_DIRECTION.get(direction);
         WallSide side = canSupportAtFace(level, pos, direction) ? (createSides ? WallSide.LOW : (WallSide)state.getValue(property)) : WallSide.NONE;
         if (side == WallSide.LOW) {
            if (above == null) {
               above = level.getBlockState(pos.above());
            }

            if (above.is(this) && above.getValue(property) != WallSide.NONE && !(Boolean)above.getValue(BASE)) {
               side = WallSide.TALL;
            }

            if (!(Boolean)state.getValue(BASE)) {
               if (below == null) {
                  below = level.getBlockState(pos.below());
               }

               if (below.is(this) && below.getValue(property) == WallSide.NONE) {
                  side = WallSide.NONE;
               }
            }
         }

         state = (BlockState)state.setValue(property, side);
      }

      return state;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.getUpdatedState(this.defaultBlockState(), context.getLevel(), context.getClickedPos(), true);
   }

   public void placeAt(LevelAccessor level, BlockPos pos, RandomSource random, int flags) {
      BlockState carpet = this.getUpdatedState(this.defaultBlockState(), level, pos, true);
      level.setBlock(pos, carpet, flags);
      BlockState topper = this.createTopperWithSideChance(level, pos, random::nextBoolean);
      if (!topper.isAir()) {
         level.setBlock(pos.above(), topper, flags);
         level.setBlock(pos, this.getUpdatedState(carpet, level, pos, true), flags);
      }
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      if (!level.isClientSide()) {
         BlockState topper = this.createTopperWithSideChance(level, pos, level.getRandom()::nextBoolean);
         if (!topper.isAir()) {
            level.setBlock(pos.above(), topper, 3);
         }
      }
   }

   private BlockState createTopperWithSideChance(BlockGetter level, BlockPos pos, BooleanSupplier keepSide) {
      BlockPos abovePos = pos.above();
      BlockState previous = level.getBlockState(abovePos);
      boolean carpetAbove = previous.is(this);
      if ((!carpetAbove || !(Boolean)previous.getValue(BASE)) && (carpetAbove || previous.canBeReplaced())) {
         BlockState topper = this.getUpdatedState((BlockState)this.defaultBlockState().setValue(BASE, false), level, abovePos, true);

         for (Direction direction : Plane.HORIZONTAL) {
            EnumProperty<WallSide> property = PROPERTY_BY_DIRECTION.get(direction);
            if (topper.getValue(property) != WallSide.NONE && !keepSide.getAsBoolean()) {
               topper = (BlockState)topper.setValue(property, WallSide.NONE);
            }
         }

         return hasFaces(topper) && topper != previous ? topper : Blocks.AIR.defaultBlockState();
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!state.canSurvive(level, pos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         BlockState updated = this.getUpdatedState(state, level, pos, false);
         return hasFaces(updated) ? updated : Blocks.AIR.defaultBlockState();
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BASE, NORTH, EAST, SOUTH, WEST});
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return switch (rotation) {
         case CLOCKWISE_180 -> (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH, (WallSide)state.getValue(SOUTH)))
                  .setValue(EAST, (WallSide)state.getValue(WEST)))
               .setValue(SOUTH, (WallSide)state.getValue(NORTH)))
            .setValue(WEST, (WallSide)state.getValue(EAST));
         case COUNTERCLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH, (WallSide)state.getValue(EAST)))
                  .setValue(EAST, (WallSide)state.getValue(SOUTH)))
               .setValue(SOUTH, (WallSide)state.getValue(WEST)))
            .setValue(WEST, (WallSide)state.getValue(NORTH));
         case CLOCKWISE_90 -> (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(NORTH, (WallSide)state.getValue(WEST)))
                  .setValue(EAST, (WallSide)state.getValue(NORTH)))
               .setValue(SOUTH, (WallSide)state.getValue(EAST)))
            .setValue(WEST, (WallSide)state.getValue(SOUTH));
         default -> state;
      };
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return switch (mirror) {
         case LEFT_RIGHT -> (BlockState)((BlockState)state.setValue(NORTH, (WallSide)state.getValue(SOUTH))).setValue(SOUTH, (WallSide)state.getValue(NORTH));
         case FRONT_BACK -> (BlockState)((BlockState)state.setValue(EAST, (WallSide)state.getValue(WEST))).setValue(WEST, (WallSide)state.getValue(EAST));
         default -> super.mirror(state, mirror);
      };
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
      return (Boolean)state.getValue(BASE) && !this.createTopperWithSideChance(level, pos, () -> true).isAir();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      BlockState topper = this.createTopperWithSideChance(level, pos, () -> true);
      if (!topper.isAir()) {
         level.setBlock(pos.above(), topper, 3);
      }
   }
}
