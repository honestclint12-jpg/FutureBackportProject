package com.futurebackport.block;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.entity.ShelfBlockEntity;
import com.futurebackport.registry.ModSounds;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ShelfBlock extends BaseEntityBlock implements SideChainPartBlock, SimpleWaterloggedBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<SideChainPart> SIDE_CHAIN_PART = EnumProperty.create("side_chain", SideChainPart.class);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final TagKey<Block> WOODEN_SHELVES = TagKey.create(Registries.BLOCK, FutureBackport.id("wooden_shelves"));
   private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

   private static VoxelShape rotated(Direction facing, double x1, double y1, double z1, double x2, double y2, double z2) {
      return switch (facing) {
         case SOUTH -> box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
         case WEST -> box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
         case EAST -> box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
         default -> box(x1, y1, z1, x2, y2, z2);
      };
   }

   public ShelfBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(POWERED, false))
               .setValue(SIDE_CHAIN_PART, SideChainPart.UNCONNECTED))
            .setValue(WATERLOGGED, false)
      );
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPES.get(state.getValue(FACING));
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public boolean isPathfindable(BlockState state, PathComputationType type) {
      return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new ShelfBlockEntity(pos, state);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, POWERED, SIDE_CHAIN_PART, WATERLOGGED});
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock())) {
         if (level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
            level.updateNeighbourForOutputSignal(pos, this);
         }

         this.updateNeighborsAfterPoweringDown(level, pos, state);
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
      if (!level.isClientSide()) {
         boolean signal = level.hasNeighborSignal(pos);
         if ((Boolean)state.getValue(POWERED) != signal) {
            BlockState updated = (BlockState)state.setValue(POWERED, signal);
            if (!signal) {
               updated = (BlockState)updated.setValue(SIDE_CHAIN_PART, SideChainPart.UNCONNECTED);
            }

            level.setBlock(pos, updated, 3);
            this.playSound(level, pos, signal ? (SoundEvent)ModSounds.SHELF_ACTIVATE.get() : (SoundEvent)ModSounds.SHELF_DEACTIVATE.get());
            level.gameEvent(signal ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, Context.of(updated));
         }
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
            .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())))
         .setValue(WATERLOGGED, fluid.is(Fluids.WATER));
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf && hand != InteractionHand.OFF_HAND) {
         OptionalInt slot = getHitSlot(hit, (Direction)state.getValue(FACING));
         if (slot.isEmpty()) {
            return InteractionResult.PASS;
         } else {
            Inventory inventory = player.getInventory();
            if (level.isClientSide()) {
               return inventory.getSelected().isEmpty() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide());
            } else if ((Boolean)state.getValue(POWERED)) {
               if (this.swapHotbar(level, pos, inventory)) {
                  this.playSound(level, pos, (SoundEvent)ModSounds.SHELF_MULTI_SWAP.get());
               }

               return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
               ItemStack removed = shelf.swapItemNoUpdate(slot.getAsInt(), stack);
               ItemStack toInventory = player.getAbilities().instabuild && removed.isEmpty() ? stack.copy() : removed;
               inventory.setItem(inventory.selected, toInventory);
               inventory.setChanged();
               shelf.setChanged(GameEvent.ITEM_INTERACT_FINISH);
               if (!removed.isEmpty()) {
                  this.playSound(level, pos, stack.isEmpty() ? (SoundEvent)ModSounds.SHELF_TAKE_ITEM.get() : (SoundEvent)ModSounds.SHELF_SINGLE_SWAP.get());
               } else {
                  if (stack.isEmpty()) {
                     return InteractionResult.PASS;
                  }

                  this.playSound(level, pos, (SoundEvent)ModSounds.SHELF_PLACE_ITEM.get());
               }

               return InteractionResult.sidedSuccess(level.isClientSide());
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }


   private boolean swapHotbar(Level level, BlockPos pos, Inventory inventory) {
      List<BlockPos> connected = this.getAllBlocksConnectedTo(level, pos);
      if (connected.isEmpty()) {
         return false;
      } else {
         boolean anySwapped = false;

         for (int part = 0; part < connected.size(); part++) {
            if (level.getBlockEntity(connected.get(part)) instanceof ShelfBlockEntity shelf) {
               for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                  int inventorySlot = 9 - (connected.size() - part) * shelf.getContainerSize() + slot;
                  if (inventorySlot >= 0 && inventorySlot <= inventory.getContainerSize()) {
                     ItemStack fromInventory = inventory.removeItemNoUpdate(inventorySlot);
                     ItemStack fromShelf = shelf.swapItemNoUpdate(slot, fromInventory);
                     if (!fromInventory.isEmpty() || !fromShelf.isEmpty()) {
                        inventory.setItem(inventorySlot, fromShelf);
                        anySwapped = true;
                     }
                  }
               }

               inventory.setChanged();
               shelf.setChanged(GameEvent.ENTITY_INTERACT);
            }
         }

         return anySwapped;
      }
   }

   private static OptionalInt getHitSlot(BlockHitResult hit, Direction facing) {
      if (hit.getDirection() != facing) {
         return OptionalInt.empty();
      } else {
         BlockPos front = hit.getBlockPos().relative(facing);
         Vec3 relative = hit.getLocation().subtract(front.getX(), front.getY(), front.getZ());

         double x = switch (facing) {
            case SOUTH -> relative.x();
            case WEST -> relative.z();
            case EAST -> 1.0 - relative.z();
            case NORTH -> 1.0 - relative.x();
            default -> 0.0;
         };
         return OptionalInt.of(Mth.clamp(Mth.floor(x * 16.0 / 5.3333335F), 0, 2));
      }
   }

   @Override
   public SideChainPart getSideChainPart(BlockState state) {
      return (SideChainPart)state.getValue(SIDE_CHAIN_PART);
   }

   @Override
   public BlockState setSideChainPart(BlockState state, SideChainPart part) {
      return (BlockState)state.setValue(SIDE_CHAIN_PART, part);
   }

   @Override
   public Direction getFacing(BlockState state) {
      return (Direction)state.getValue(FACING);
   }

   @Override
   public boolean isConnectable(BlockState state) {
      return state.is(WOODEN_SHELVES) && state.hasProperty(POWERED) && (Boolean)state.getValue(POWERED);
   }

   @Override
   public int getMaxChainLength() {
      return 3;
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      if ((Boolean)state.getValue(POWERED)) {
         this.updateSelfAndNeighborsOnPoweringUp(level, pos, state, oldState);
      } else {
         this.updateNeighborsAfterPoweringDown(level, pos, state);
      }
   }

   private void playSound(LevelAccessor level, BlockPos pos, SoundEvent sound) {
      level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return !level.isClientSide() && level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf
         ? (shelf.getItem(0).isEmpty() ? 0 : 1) | (shelf.getItem(1).isEmpty() ? 0 : 2) | (shelf.getItem(2).isEmpty() ? 0 : 4)
         : 0;
   }

   static {
      for (Direction facing : Plane.HORIZONTAL) {
         SHAPES.put(
            facing,
            Shapes.or(
               rotated(facing, 0.0, 12.0, 11.0, 16.0, 16.0, 13.0),
               new VoxelShape[]{rotated(facing, 0.0, 0.0, 13.0, 16.0, 16.0, 16.0), rotated(facing, 0.0, 0.0, 11.0, 16.0, 4.0, 13.0)}
            )
         );
      }
   }
}
