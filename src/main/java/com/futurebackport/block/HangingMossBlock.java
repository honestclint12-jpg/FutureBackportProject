package com.futurebackport.block;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingMossBlock extends Block implements BonemealableBlock {
   public static final MapCodec<HangingMossBlock> CODEC = simpleCodec(HangingMossBlock::new);
   public static final BooleanProperty TIP = BooleanProperty.create("tip");
   private static final TagKey<Block> PALE_OAK_LOGS = TagKey.create(Registries.BLOCK, FutureBackport.id("pale_oak_logs"));
   private static final VoxelShape SHAPE_BASE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   private static final VoxelShape SHAPE_TIP = box(1.0, 2.0, 1.0, 15.0, 16.0, 15.0);

   public HangingMossBlock(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(TIP, true));
   }

   protected MapCodec<HangingMossBlock> codec() {
      return CODEC;
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(TIP) ? SHAPE_TIP : SHAPE_BASE;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(500) == 0) {
         BlockState above = level.getBlockState(pos.above());
         if (above.is(PALE_OAK_LOGS) || above.is(ModBlocks.PALE_OAK_LEAVES.get())) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), (SoundEvent)ModSounds.PALE_HANGING_MOSS_IDLE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
         }
      }
   }

   protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return this.canStayAt(level, pos);
   }

   private boolean canStayAt(BlockGetter level, BlockPos pos) {
      BlockPos above = pos.above();
      BlockState aboveState = level.getBlockState(above);
      return MultifaceBlock.canAttachTo(level, Direction.UP, above, aboveState) || aboveState.is(this);
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      if (!this.canStayAt(level, pos)) {
         level.scheduleTick(pos, this, 1);
      }

      return (BlockState)state.setValue(TIP, !level.getBlockState(pos.below()).is(this));
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!this.canStayAt(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{TIP});
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
      BlockPos growPos = this.getTip(level, pos).below();
      return level.getBlockState(growPos).isAir() && !level.isOutsideBuildHeight(growPos);
   }

   private BlockPos getTip(BlockGetter level, BlockPos pos) {
      MutableBlockPos cursor = pos.mutable();

      do {
         cursor.move(Direction.DOWN);
      } while (level.getBlockState(cursor).is(this));

      return cursor.above().immutable();
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      BlockPos growPos = this.getTip(level, pos).below();
      if (level.getBlockState(growPos).isAir()) {
         level.setBlockAndUpdate(growPos, (BlockState)state.setValue(TIP, true));
      }
   }
}
