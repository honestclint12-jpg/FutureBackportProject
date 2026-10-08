package com.futurebackport.block;

import com.futurebackport.block.entity.CopperGolemStatueBlockEntity;
import com.futurebackport.entity.CopperGolem;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CopperGolemStatueBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<CopperGolemStatueBlock.Pose> POSE = EnumProperty.create("copper_golem_pose", CopperGolemStatueBlock.Pose.class);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);
   private final WeatherState weatherState;

   public CopperGolemStatueBlock(WeatherState weatherState, Properties properties) {
      super(properties);
      this.weatherState = weatherState;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
               .setValue(POSE, CopperGolemStatueBlock.Pose.STANDING))
            .setValue(WATERLOGGED, false)
      );
   }

   public WeatherState getWeatherState() {
      return this.weatherState;
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec()).apply(i, p -> new CopperGolemStatueBlock(this.weatherState, p)));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, POSE, WATERLOGGED});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()))
         .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   protected BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   protected BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected ItemInteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
   ) {
      if (stack.is(ItemTags.AXES)) {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else {
         this.updatePose(level, state, pos, player);
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   protected void updatePose(Level level, BlockState state, BlockPos pos, Player player) {
      level.playSound(null, pos, (SoundEvent)ModSounds.COPPER_GOLEM_BECOME_STATUE.get(), SoundSource.BLOCKS);
      level.setBlock(pos, (BlockState)state.setValue(POSE, ((CopperGolemStatueBlock.Pose)state.getValue(POSE)).next()), 3);
      level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CopperGolemStatueBlockEntity(pos, state);
   }

   protected boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return ((CopperGolemStatueBlock.Pose)state.getValue(POSE)).ordinal() + 1;
   }

   protected FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
   }

   public static enum Pose implements StringRepresentable {
      STANDING,
      SITTING,
      RUNNING,
      STAR;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }

      public CopperGolemStatueBlock.Pose next() {
         return values()[(this.ordinal() + 1) % values().length];
      }
   }

   public static class Weathering extends CopperGolemStatueBlock implements WeatheringCopper {
      public Weathering(WeatherState weatherState, Properties properties) {
         super(weatherState, properties);
      }

      @Override
      protected MapCodec<? extends BaseEntityBlock> codec() {
         return RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec()).apply(i, p -> new CopperGolemStatueBlock.Weathering(this.getWeatherState(), p)));
      }

      public WeatherState getAge() {
         return this.getWeatherState();
      }

      protected boolean isRandomlyTicking(BlockState state) {
         return this.getWeatherState() != WeatherState.OXIDIZED;
      }

      protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
         this.changeOverTime(state, level, pos, random);
      }

      @Override
      protected ItemInteractionResult useItemOn(
         ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
      ) {
         if (level.getBlockEntity(pos) instanceof CopperGolemStatueBlockEntity statue) {
            if (!stack.is(ItemTags.AXES)) {
               if (stack.is(Items.HONEYCOMB)) {
                  return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
               } else {
                  this.updatePose(level, state, pos, player);
                  return ItemInteractionResult.sidedSuccess(level.isClientSide);
               }
            } else if (this.getAge() == WeatherState.UNAFFECTED) {
               if (!level.isClientSide) {
                  CopperGolem golem = statue.removeStatue(state);
                  stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                  if (golem != null) {
                     level.addFreshEntity(golem);
                     level.removeBlock(pos, false);
                  }
               }

               return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
               return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
         } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         }
      }
   }
}
