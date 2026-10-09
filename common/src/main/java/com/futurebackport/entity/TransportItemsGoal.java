package com.futurebackport.entity;

import com.futurebackport.block.CopperChestBlock;
import com.futurebackport.registry.ModSounds;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TransportItemsGoal extends Goal {
   private static final int HORIZONTAL_SEARCH = 32;
   private static final int VERTICAL_SEARCH = 8;
   private static final int TARGET_INTERACTION_TIME = 60;
   private static final int VISITED_MEMORY_TIME = 6000;
   private static final int MAX_VISITED = 10;
   private static final int MAX_UNREACHABLE = 50;
   private static final int IDLE_COOLDOWN = 140;
   private static final int MAX_STACK = 16;
   private final CopperGolem golem;
   @Nullable
   private BlockPos target;
   private boolean interacting;
   private int ticksSinceReachingTarget;
   private int stuckTicks;
   private final Map<BlockPos, Long> visited = new HashMap<>();
   private final Map<BlockPos, Long> unreachable = new HashMap<>();

   public TransportItemsGoal(CopperGolem golem) {
      this.golem = golem;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      return this.golem.transportCooldown <= 0 && !this.golem.isLeashed() && this.golem.level() instanceof ServerLevel;
   }

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void stop() {
      if (this.interacting && this.target != null) {
         this.closeLid(this.target);
      }

      this.target = null;
      this.interacting = false;
      this.ticksSinceReachingTarget = 0;
      this.golem.setState(CopperGolem.State.IDLE);
      this.golem.getNavigation().stop();
   }

   public void tick() {
      ServerLevel level = (ServerLevel)this.golem.level();
      long now = level.getGameTime();
      this.visited.values().removeIf(t -> now - t > 6000L);
      this.unreachable.values().removeIf(t -> now - t > 6000L);
      if (this.target == null || !this.isWanted(level.getBlockState(this.target)) || containerAt(level, this.target) == null) {
         if (this.interacting && this.target != null) {
            this.closeLid(this.target);
         }

         this.target = this.findTarget(level);
         this.interacting = false;
         this.ticksSinceReachingTarget = 0;
         this.stuckTicks = 0;
         this.golem.setState(CopperGolem.State.IDLE);
         if (this.target == null) {
            this.cooldown();
            return;
         }

         this.visit(this.target, now);
         if (this.target == null) {
            return;
         }
      }

      if (!this.interacting) {
         if (this.isWithin(this.target, this.golem.getNavigation().isDone() ? 1.0 : 0.5)) {
            this.interacting = true;
            this.ticksSinceReachingTarget = 0;
            this.golem.getNavigation().stop();
         } else {
            this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.target));
            if (this.golem.getNavigation().isDone()
               && (!this.golem.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.0) || ++this.stuckTicks > 40)) {
               this.markUnreachable(this.target, now);
               this.target = null;
            }
         }
      } else if (!this.isWithin(this.target, 2.0)) {
         this.closeLid(this.target);
         this.interacting = false;
         this.golem.setState(CopperGolem.State.IDLE);
      } else {
         this.golem.getNavigation().stop();
         this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.target));
         Container container = containerAt(level, this.target);
         this.ticksSinceReachingTarget++;
         boolean picking = this.golem.getMainHandItem().isEmpty();
         boolean success = picking ? !container.isEmpty() : container.isEmpty() || this.hasItemMatchingHand(container);
         if (this.ticksSinceReachingTarget == 1) {
            this.openLid(this.target);
            this.golem
               .setState(
                  picking
                     ? (success ? CopperGolem.State.GETTING_ITEM : CopperGolem.State.GETTING_NO_ITEM)
                     : (success ? CopperGolem.State.DROPPING_ITEM : CopperGolem.State.DROPPING_NO_ITEM)
               );
         }

         if (this.ticksSinceReachingTarget == 9) {
            SoundEvent sound = picking
               ? (SoundEvent)(success ? ModSounds.COPPER_GOLEM_ITEM_GET : ModSounds.COPPER_GOLEM_ITEM_NO_GET).get()
               : (SoundEvent)(success ? ModSounds.COPPER_GOLEM_ITEM_DROP : ModSounds.COPPER_GOLEM_ITEM_NO_DROP).get();
            this.golem.playSound(sound);
         }

         if (this.ticksSinceReachingTarget >= 60) {
            this.closeLid(this.target);
            if (success) {
               if (picking) {
                  this.golem.setItemSlot(EquipmentSlot.MAINHAND, takeItems(container));
                  this.golem.setGuaranteedDrop(EquipmentSlot.MAINHAND);
                  this.visited.clear();
                  this.unreachable.clear();
               } else {
                  ItemStack left = this.putItems(container);
                  this.golem.setItemSlot(EquipmentSlot.MAINHAND, left);
                  if (left.isEmpty()) {
                     this.visited.clear();
                     this.unreachable.clear();
                  }
               }

               container.setChanged();
            }

            this.target = null;
            this.interacting = false;
            this.golem.setState(CopperGolem.State.IDLE);
         }
      }
   }

   private void cooldown() {
      this.golem.transportCooldown = 140;
      this.visited.clear();
      this.unreachable.clear();
   }

   private void visit(BlockPos pos, long now) {
      this.visited.put(pos, now);
      if (this.visited.size() > 10) {
         this.target = null;
         this.cooldown();
      }
   }

   private void markUnreachable(BlockPos pos, long now) {
      this.visited.remove(pos);
      this.unreachable.put(pos, now);
      if (this.unreachable.size() > 50) {
         this.cooldown();
      }
   }

   private boolean isWanted(BlockState state) {
      return this.golem.getMainHandItem().isEmpty() ? state.getBlock() instanceof CopperChestBlock : state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST);
   }

   private boolean isWithin(BlockPos pos, double distance) {
      Level level = this.golem.level();
      AABB shape = level.getBlockState(pos).getCollisionShape(level, pos).bounds().inflate(distance, 0.5, distance).move(pos);
      return shape.intersects(this.golem.getBoundingBox());
   }

   @Nullable
   private BlockPos findTarget(ServerLevel level) {
      AABB area = this.golem.getBoundingBox().inflate(32.0, 8.0, 32.0);
      BlockPos best = null;
      double bestDistance = Double.MAX_VALUE;
      ChunkPos center = this.golem.chunkPosition();
      int radius = Math.floorDiv(32, 16) + 1;

      for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
         for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
            if (chunk != null) {
               for (BlockEntity be : chunk.getBlockEntities().values()) {
                  if (be instanceof ChestBlockEntity) {
                     BlockPos pos = be.getBlockPos();
                     double distance = pos.distToCenterSqr(this.golem.position());
                     if (!(distance >= bestDistance)
                        && area.contains(pos.getX(), pos.getY(), pos.getZ())
                        && this.isWanted(be.getBlockState())
                        && !this.alreadyVisited(level, pos)
                        && containerAt(level, pos) != null) {
                        best = pos;
                        bestDistance = distance;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean alreadyVisited(ServerLevel level, BlockPos pos) {
      if (!this.visited.containsKey(pos) && !this.unreachable.containsKey(pos)) {
         BlockState state = level.getBlockState(pos);
         if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
            return this.visited.containsKey(other) || this.unreachable.containsKey(other);
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static Container containerAt(ServerLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.getBlock() instanceof ChestBlock chest ? ChestBlock.getContainer(chest, state, level, pos, false) : null;
   }

   private boolean hasItemMatchingHand(Container container) {
      ItemStack hand = this.golem.getMainHandItem();

      for (int i = 0; i < container.getContainerSize(); i++) {
         if (ItemStack.isSameItem(container.getItem(i), hand)) {
            return true;
         }
      }

      return false;
   }

   private static ItemStack takeItems(Container container) {
      for (int i = 0; i < container.getContainerSize(); i++) {
         ItemStack stack = container.getItem(i);
         if (!stack.isEmpty()) {
            return container.removeItem(i, Math.min(stack.getCount(), 16));
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack putItems(Container container) {
      ItemStack stack = this.golem.getMainHandItem();

      for (int i = 0; i < container.getContainerSize(); i++) {
         ItemStack slot = container.getItem(i);
         if (slot.isEmpty()) {
            container.setItem(i, stack);
            return ItemStack.EMPTY;
         }

         if (ItemStack.isSameItemSameTags(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
            int add = Math.min(slot.getMaxStackSize() - slot.getCount(), stack.getCount());
            slot.grow(add);
            stack.shrink(add);
            container.setItem(i, slot);
            if (stack.isEmpty()) {
               return ItemStack.EMPTY;
            }
         }
      }

      return stack;
   }

   private void openLid(BlockPos pos) {
      this.setLid(pos, true);
   }

   private void closeLid(BlockPos pos) {
      this.setLid(pos, false);
   }

   private void setLid(BlockPos pos, boolean open) {
      Level level = this.golem.level();
      BlockState state = level.getBlockState(pos);
      if (state.getBlock() instanceof ChestBlock) {
         if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            int players = ChestBlockEntity.getOpenCount(level, pos);
            if (players <= 0) {
               level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
               if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                  BlockPos other = pos.relative(ChestBlock.getConnectedDirection(state));
                  level.blockEvent(other, state.getBlock(), 1, open ? 1 : 0);
               }

               SoundEvent sound = state.getBlock() instanceof CopperChestBlock copper
                  ? CopperChestBlock.hingeSound(copper.getWeatherState(), open)
                  : (open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE);
               level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            }
         }
      }
   }
}
