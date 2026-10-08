package com.futurebackport.block;

import com.futurebackport.entity.HappyGhast;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DriedGhastBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<DriedGhastBlock> CODEC = simpleCodec(DriedGhastBlock::new);
   public static final IntegerProperty HYDRATION_LEVEL = IntegerProperty.create("hydration", 0, 3);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int HYDRATION_TICK_DELAY = 5000;
   private static final VoxelShape SHAPE = box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

   public DriedGhastBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(HYDRATION_LEVEL, 0))
            .setValue(WATERLOGGED, false)
      );
   }

   protected MapCodec<DriedGhastBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, HYDRATION_LEVEL, WATERLOGGED});
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.getValue(WATERLOGGED)) {
         level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }

      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      int hydration = (Integer)state.getValue(HYDRATION_LEVEL);
      if ((Boolean)state.getValue(WATERLOGGED)) {
         if (hydration < 3) {
            level.playSound(null, pos, (SoundEvent)ModSounds.DRIED_GHAST_TRANSITION.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, (BlockState)state.setValue(HYDRATION_LEVEL, hydration + 1), 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(state));
         } else {
            this.spawnGhastling(level, pos, state);
         }
      } else if (hydration > 0) {
         level.setBlock(pos, (BlockState)state.setValue(HYDRATION_LEVEL, hydration - 1), 2);
         level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(state));
      }
   }

   private void spawnGhastling(ServerLevel level, BlockPos pos, BlockState state) {
      level.removeBlock(pos, false);
      HappyGhast ghastling = (HappyGhast)((EntityType)ModEntities.HAPPY_GHAST.get()).create(level);
      if (ghastling != null) {
         Vec3 at = Vec3.atBottomCenterOf(pos);
         float yaw = ((Direction)state.getValue(FACING)).toYRot();
         ghastling.setBaby(true);
         ghastling.setYHeadRot(yaw);
         ghastling.moveTo(at.x(), at.y(), at.z(), yaw, 0.0F);
         level.addFreshEntity(ghastling);
         level.playSound(null, ghastling, (SoundEvent)ModSounds.GHASTLING_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.5;
      double z = pos.getZ() + 0.5;
      if (!(Boolean)state.getValue(WATERLOGGED)) {
         if (random.nextInt(40) == 0 && level.getBlockState(pos.below()).is(Blocks.SOUL_SAND)) {
            level.playLocalSound(x, y, z, (SoundEvent)ModSounds.DRIED_GHAST_AMBIENT.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0.0, 0.02, 0.0);
         }
      } else {
         if (random.nextInt(40) == 0) {
            level.playLocalSound(x, y, z, (SoundEvent)ModSounds.DRIED_GHAST_AMBIENT_WATER.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         if (random.nextInt(6) == 0) {
            level.addParticle(
               ParticleTypes.HAPPY_VILLAGER,
               x + (random.nextFloat() * 2.0F - 1.0F) / 3.0F,
               y + 0.4,
               z + (random.nextFloat() * 2.0F - 1.0F) / 3.0F,
               0.0,
               random.nextFloat(),
               0.0
            );
         }
      }
   }

   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (((Boolean)state.getValue(WATERLOGGED) || (Integer)state.getValue(HYDRATION_LEVEL) > 0) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
         level.scheduleTick(pos, this, 5000);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluid.is(Fluids.WATER)))
         .setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   protected FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
      if (!(Boolean)state.getValue(WATERLOGGED) && fluid.is(Fluids.WATER)) {
         if (!level.isClientSide()) {
            level.setBlock(pos, (BlockState)state.setValue(WATERLOGGED, true), 3);
            level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
            level.playSound(null, pos, (SoundEvent)ModSounds.DRIED_GHAST_PLACE_IN_WATER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         return true;
      } else {
         return false;
      }
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      level.playSound(
         null,
         pos,
         state.getValue(WATERLOGGED) ? (SoundEvent)ModSounds.DRIED_GHAST_PLACE_IN_WATER.get() : (SoundEvent)ModSounds.DRIED_GHAST_PLACE.get(),
         SoundSource.BLOCKS,
         1.0F,
         1.0F
      );
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }
}
