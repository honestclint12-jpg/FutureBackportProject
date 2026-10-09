package com.futurebackport.block;

import com.futurebackport.FutureBackport;
import com.futurebackport.registry.ModSounds;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryVegetationBlock extends BushBlock implements BonemealableBlock {
   public static final TagKey<Block> SUPPORTS_DRY_VEGETATION = TagKey.create(Registries.BLOCK, FutureBackport.id("supports_dry_vegetation"));
   public static final TagKey<Block> TRIGGERS_DESERT_SOUNDS = TagKey.create(
      Registries.BLOCK, FutureBackport.id("triggers_ambient_desert_dry_vegetation_block_sounds")
   );
   private final boolean tall;
   private final VoxelShape shape;
   private final Supplier<? extends Block> other;

   public DryVegetationBlock(boolean tall, Supplier<? extends Block> other, Properties properties) {
      super(properties);
      this.tall = tall;
      this.other = other;
      this.shape = tall ? box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0) : box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shape;
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return state.is(SUPPORTS_DRY_VEGETATION);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(200) == 0
         && level.getBlockState(pos.below()).is(TRIGGERS_DESERT_SOUNDS)
         && level.getBlockState(pos.below(2)).is(TRIGGERS_DESERT_SOUNDS)) {
         level.playLocalSound(pos, (SoundEvent)ModSounds.DRY_GRASS.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
      }
   }

   public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
      return !this.tall || BonemealSpreading.hasSpreadableNeighbourPos(level, pos, this.other.get().defaultBlockState());
   }

   public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      BlockState otherState = this.other.get().defaultBlockState();
      if (this.tall) {
         BonemealSpreading.findSpreadableNeighbourPos(level, pos, otherState).ifPresent(p -> level.setBlockAndUpdate(p, otherState));
      } else {
         level.setBlockAndUpdate(pos, otherState);
      }
   }
}
