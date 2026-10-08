package com.futurebackport.block;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.entity.CreakingHeartBlockEntity;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class CreakingHeartBlock extends BaseEntityBlock {
   public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
   public static final EnumProperty<CreakingHeartState> STATE = EnumProperty.create("creaking_heart_state", CreakingHeartState.class);
   public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
   private static final TagKey<Block> PALE_OAK_LOGS = TagKey.create(Registries.BLOCK, FutureBackport.id("pale_oak_logs"));

   public CreakingHeartBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AXIS, Axis.Y)).setValue(STATE, CreakingHeartState.UPROOTED))
            .setValue(NATURAL, false)
      );
   }

   protected MapCodec<CreakingHeartBlock> codec() {
      return CODEC;
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CreakingHeartBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide() && state.getValue(STATE) != CreakingHeartState.UPROOTED
         ? createTickerHelper(type, (BlockEntityType)ModBlockEntities.CREAKING_HEART.get(), CreakingHeartBlockEntity::serverTick)
         : null;
   }

   public static boolean isNaturalNight(Level level) {
      return level.dimensionType().natural() && level.isNight();
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (isNaturalNight(level) && state.getValue(STATE) != CreakingHeartState.UPROOTED && random.nextInt(16) == 0 && isSurroundedByLogs(level, pos)) {
         level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), (SoundEvent)ModSounds.CREAKING_HEART_IDLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
      }
   }

   protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      level.scheduleTick(pos, this, 1);
      return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      BlockState updated = updateState(state, level, pos);
      if (updated != state) {
         level.setBlock(pos, updated, 3);
      }
   }

   private static BlockState updateState(BlockState state, Level level, BlockPos pos) {
      return hasRequiredLogs(state, level, pos) && state.getValue(STATE) == CreakingHeartState.UPROOTED
         ? (BlockState)state.setValue(STATE, isNaturalNight(level) ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT)
         : state;
   }

   public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos) {
      Axis axis = (Axis)state.getValue(AXIS);

      for (Direction direction : new Direction[]{
         Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE), Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE)
      }) {
         BlockState neighbour = level.getBlockState(pos.relative(direction));
         if (!neighbour.is(PALE_OAK_LOGS) || !neighbour.hasProperty(AXIS) || neighbour.getValue(AXIS) != axis) {
            return false;
         }
      }

      return true;
   }

   private static boolean isSurroundedByLogs(LevelAccessor level, BlockPos pos) {
      for (Direction direction : Direction.values()) {
         if (!level.getBlockState(pos.relative(direction)).is(PALE_OAK_LOGS)) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return updateState((BlockState)this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis()), context.getLevel(), context.getClickedPos());
   }

   protected BlockState rotate(BlockState state, Rotation rotation) {
      return RotatedPillarBlock.rotatePillar(state, rotation);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{AXIS, STATE, NATURAL});
   }

   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
      if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart) {
         heart.removeProtector(null);
      }

      super.onRemove(state, level, pos, newState, movedByPiston);
   }

   protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
      if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart && explosion.getBlockInteraction() != BlockInteraction.TRIGGER_BLOCK) {
         heart.removeProtector(level.damageSources().explosion(explosion));
         if (explosion.getIndirectSourceEntity() instanceof Player player) {
            this.tryAwardExperience(player, state, level, pos);
         }
      }

      super.onExplosionHit(state, level, pos, explosion, dropConsumer);
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart) {
         heart.removeProtector(player.damageSources().playerAttack(player));
         this.tryAwardExperience(player, state, level, pos);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   private void tryAwardExperience(Player player, BlockState state, Level level, BlockPos pos) {
      if (!player.isCreative() && !player.isSpectator() && (Boolean)state.getValue(NATURAL) && level instanceof ServerLevel serverLevel) {
         this.popExperience(serverLevel, pos, level.getRandom().nextIntBetweenInclusive(20, 24));
      }
   }

   protected boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      if (state.getValue(STATE) == CreakingHeartState.UPROOTED) {
         return 0;
      } else {
         return level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity heart ? heart.getAnalogOutputSignal() : 0;
      }
   }
}
