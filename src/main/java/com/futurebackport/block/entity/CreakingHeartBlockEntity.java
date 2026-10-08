package com.futurebackport.block.entity;

import com.futurebackport.FutureBackport;
import com.futurebackport.block.CreakingHeartBlock;
import com.futurebackport.block.CreakingHeartState;
import com.futurebackport.block.ResinClumpBlock;
import com.futurebackport.entity.Creaking;
import com.futurebackport.particle.TrailParticleOption;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModEntities;
import com.futurebackport.registry.ModSounds;
import com.mojang.datafixers.util.Either;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.SpawnUtil.Strategy;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CreakingHeartBlockEntity extends BlockEntity {
   private static final TagKey<Block> PALE_OAK_LOGS = TagKey.create(Registries.BLOCK, FutureBackport.id("pale_oak_logs"));
   @Nullable
   private Either<Creaking, UUID> creakingInfo;
   private long ticksExisted;
   private int ticker;
   private int emitter;
   @Nullable
   private Vec3 emitterTarget;
   private int outputSignal;

   public CreakingHeartBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.CREAKING_HEART.get(), pos, state);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, CreakingHeartBlockEntity heart) {
      heart.ticksExisted++;
      if (level instanceof ServerLevel serverLevel) {
         int signal = heart.computeAnalogOutputSignal();
         if (heart.outputSignal != signal) {
            heart.outputSignal = signal;
            level.updateNeighbourForOutputSignal(pos, (Block)ModBlocks.CREAKING_HEART.get());
         }

         if (heart.emitter > 0) {
            if (heart.emitter > 50) {
               heart.emitParticles(serverLevel, 1, true);
               heart.emitParticles(serverLevel, 1, false);
            }

            if (heart.emitter % 10 == 0 && heart.emitterTarget != null) {
               heart.getCreakingProtector().ifPresent(creakingx -> heart.emitterTarget = creakingx.getBoundingBox().getCenter());
               Vec3 center = Vec3.atCenterOf(pos);
               float progress = 0.2F + 0.8F * (100 - heart.emitter) / 100.0F;
               Vec3 soundLocation = center.subtract(heart.emitterTarget).scale(progress).add(heart.emitterTarget);
               serverLevel.playSound(
                  null,
                  BlockPos.containing(soundLocation),
                  (SoundEvent)ModSounds.CREAKING_HEART_HURT.get(),
                  SoundSource.BLOCKS,
                  heart.emitter / 2.0F / 100.0F + 0.5F,
                  1.0F
               );
            }

            heart.emitter--;
         }

         if (heart.ticker-- < 0) {
            heart.ticker = level.getRandom().nextInt(5) + 20;
            BlockState updated = updateCreakingState(level, state, pos, heart);
            if (updated != state) {
               level.setBlock(pos, updated, 3);
               if (updated.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED) {
                  return;
               }
            }

            if (heart.creakingInfo == null) {
               if (updated.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE
                  && level.getDifficulty() != Difficulty.PEACEFUL
                  && serverLevel.getServer().isSpawningMonsters()) {
                  Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 32.0, false);
                  if (player != null) {
                     Creaking creaking = spawnProtector(serverLevel, heart);
                     if (creaking != null) {
                        heart.setCreakingInfo(creaking);
                        creaking.makeSound((SoundEvent)ModSounds.CREAKING_SPAWN.get());
                        level.playSound(null, pos, (SoundEvent)ModSounds.CREAKING_HEART_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                     }
                  }
               }
            } else {
               heart.getCreakingProtector()
                  .ifPresent(
                     creakingx -> {
                        if (!CreakingHeartBlock.isNaturalNight(level) && !creakingx.isPersistenceRequired()
                           || heart.distanceToCreaking() > 34.0
                           || creakingx.playerIsStuckInYou()) {
                           heart.removeProtector(null);
                        }
                     }
                  );
            }
         }
      }
   }

   private static BlockState updateCreakingState(Level level, BlockState state, BlockPos pos, CreakingHeartBlockEntity heart) {
      return !CreakingHeartBlock.hasRequiredLogs(state, level, pos) && heart.creakingInfo == null
         ? (BlockState)state.setValue(CreakingHeartBlock.STATE, CreakingHeartState.UPROOTED)
         : (BlockState)state.setValue(
            CreakingHeartBlock.STATE, CreakingHeartBlock.isNaturalNight(level) ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT
         );
   }

   private double distanceToCreaking() {
      return this.getCreakingProtector().map(creaking -> Math.sqrt(creaking.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos())))).orElse(0.0);
   }

   private void clearCreakingInfo() {
      this.creakingInfo = null;
      this.setChanged();
   }

   public void setCreakingInfo(Creaking creaking) {
      this.creakingInfo = Either.left(creaking);
      this.setChanged();
   }

   public void setCreakingInfo(UUID uuid) {
      this.creakingInfo = Either.right(uuid);
      this.ticksExisted = 0L;
      this.setChanged();
   }

   private Optional<Creaking> getCreakingProtector() {
      if (this.creakingInfo == null) {
         return Optional.empty();
      } else {
         if (this.creakingInfo.left().isPresent()) {
            Creaking creaking = (Creaking)this.creakingInfo.left().get();
            if (!creaking.isRemoved()) {
               return Optional.of(creaking);
            }

            this.setCreakingInfo(creaking.getUUID());
         }

         if (this.level instanceof ServerLevel serverLevel && this.creakingInfo.right().isPresent()) {
            if (serverLevel.getEntity((UUID)this.creakingInfo.right().get()) instanceof Creaking resolved) {
               this.setCreakingInfo(resolved);
               return Optional.of(resolved);
            }

            if (this.ticksExisted >= 30L) {
               this.clearCreakingInfo();
            }
         }

         return Optional.empty();
      }
   }

   @Nullable
   private static Creaking spawnProtector(ServerLevel level, CreakingHeartBlockEntity heart) {
      BlockPos pos = heart.getBlockPos();
      Optional<Creaking> spawned = SpawnUtil.trySpawnMob(
         (EntityType)ModEntities.CREAKING.get(), MobSpawnType.SPAWNER, level, pos, 5, 16, 8, Strategy.ON_TOP_OF_COLLIDER
      );
      if (spawned.isEmpty()) {
         return null;
      } else {
         Creaking creaking = spawned.get();
         level.gameEvent(creaking, GameEvent.ENTITY_PLACE, creaking.position());
         level.broadcastEntityEvent(creaking, (byte)60);
         creaking.setTransient(pos);
         return creaking;
      }
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public void creakingHurt() {
      Optional<Creaking> creaking = this.getCreakingProtector();
      if (!creaking.isEmpty() && this.level instanceof ServerLevel serverLevel && this.emitter <= 0) {
         this.emitParticles(serverLevel, 20, false);
         if (this.getBlockState().getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE) {
            int clumps = this.level.getRandom().nextIntBetweenInclusive(2, 3);

            for (int i = 0; i < clumps; i++) {
               this.spreadResin(serverLevel).ifPresent(placed -> {
                  this.level.playSound(null, placed, (SoundEvent)ModSounds.RESIN_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                  this.level.gameEvent(GameEvent.BLOCK_PLACE, placed, Context.of(this.getBlockState()));
               });
            }
         }

         this.emitter = 100;
         this.emitterTarget = creaking.get().getBoundingBox().getCenter();
      }
   }

   private Optional<BlockPos> spreadResin(ServerLevel level) {
      RandomSource random = level.getRandom();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      ArrayDeque<Integer> depths = new ArrayDeque<>();
      Set<BlockPos> visited = new HashSet<>();
      queue.add(this.worldPosition);
      depths.add(0);
      visited.add(this.worldPosition);
      int count = 0;

      while (!queue.isEmpty() && count++ < 64) {
         BlockPos pos = queue.poll();
         int depth = depths.poll();
         if (level.getBlockState(pos).is(PALE_OAK_LOGS)) {
            for (Direction direction : Util.shuffledCopy(Direction.values(), random)) {
               BlockPos neighbourPos = pos.relative(direction);
               BlockState neighbour = level.getBlockState(neighbourPos);
               Direction opposite = direction.getOpposite();
               if (neighbour.isAir()) {
                  neighbour = ((ResinClumpBlock)ModBlocks.RESIN_CLUMP.get()).defaultBlockState();
               } else if (neighbour.is(Blocks.WATER) && neighbour.getFluidState().isSource()) {
                  neighbour = (BlockState)((ResinClumpBlock)ModBlocks.RESIN_CLUMP.get()).defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
               }

               if (neighbour.is(ModBlocks.RESIN_CLUMP.get()) && !MultifaceBlock.hasFace(neighbour, opposite)) {
                  level.setBlock(neighbourPos, (BlockState)neighbour.setValue(MultifaceBlock.getFaceProperty(opposite), true), 3);
                  return Optional.of(neighbourPos);
               }
            }
         }

         if (depth < 2) {
            for (Direction direction : Util.shuffledCopy(Direction.values(), random)) {
               BlockPos next = pos.relative(direction);
               if (level.getBlockState(next).is(PALE_OAK_LOGS) && visited.add(next)) {
                  queue.add(next);
                  depths.add(depth + 1);
               }
            }
         }
      }

      return Optional.empty();
   }

   private void emitParticles(ServerLevel level, int count, boolean towardsCreaking) {
      Optional<Creaking> creaking = this.getCreakingProtector();
      if (!creaking.isEmpty()) {
         int color = towardsCreaking ? 16545810 : 6250335;
         RandomSource random = level.getRandom();

         for (int i = 0; i < count; i++) {
            AABB box = creaking.get().getBoundingBox();
            Vec3 source = new Vec3(box.minX, box.minY, box.minZ)
               .add(random.nextDouble() * box.getXsize(), random.nextDouble() * box.getYsize(), random.nextDouble() * box.getZsize());
            Vec3 destination = Vec3.atLowerCornerOf(this.getBlockPos()).add(random.nextDouble(), random.nextDouble(), random.nextDouble());
            if (towardsCreaking) {
               Vec3 swap = source;
               source = destination;
               destination = swap;
            }

            level.sendParticles(new TrailParticleOption(destination, color, random.nextInt(40) + 10), source.x, source.y, source.z, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   public void removeProtector(@Nullable DamageSource source) {
      this.getCreakingProtector().ifPresent(creaking -> {
         if (source == null) {
            creaking.tearDown();
         } else {
            creaking.creakingDeathEffects(source);
            creaking.setTearingDown();
            creaking.setHealth(0.0F);
         }

         this.clearCreakingInfo();
      });
   }

   public boolean isProtector(Creaking creaking) {
      return this.getCreakingProtector().map(c -> c == creaking).orElse(false);
   }

   public int getAnalogOutputSignal() {
      return this.outputSignal;
   }

   private int computeAnalogOutputSignal() {
      if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
         double scaled = Math.clamp(this.distanceToCreaking(), 0.0, 32.0) / 32.0;
         return 15 - (int)Math.floor(scaled * 15.0);
      } else {
         return 0;
      }
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.hasUUID("creaking")) {
         this.setCreakingInfo(tag.getUUID("creaking"));
      } else {
         this.clearCreakingInfo();
      }
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      if (this.creakingInfo != null) {
         tag.putUUID("creaking", (UUID)this.creakingInfo.map(Entity::getUUID, uuid -> uuid));
      }
   }
}
