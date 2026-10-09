package com.futurebackport.block;

import com.futurebackport.particle.TrailParticleOption;
import com.futurebackport.registry.ModBlocks;
import com.futurebackport.registry.ModSounds;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.Vec3;

public class EyeblossomBlock extends FlowerBlock {
   private final EyeblossomBlock.Type type;

   public EyeblossomBlock(EyeblossomBlock.Type type, Properties properties) {
      super(type.effect, type.effectDuration, properties);
      this.type = type;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.type.open && random.nextInt(700) == 0 && level.getBlockState(pos.below()).is(ModBlocks.PALE_MOSS_BLOCK.get())) {
         level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), (SoundEvent)ModSounds.EYEBLOSSOM_IDLE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, false);
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.tryChangingState(state, level, pos, random)) {
         level.playSound(null, pos, this.type.transform().longSwitchSound.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.randomTick(state, level, pos, random);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.tryChangingState(state, level, pos, random)) {
         level.playSound(null, pos, this.type.transform().shortSwitchSound.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      super.tick(state, level, pos, random);
   }

   static boolean shouldBeOpen(ServerLevel level, boolean current) {
      return level.dimensionType().hasFixedTime() ? current : !level.isDay();
   }

   private boolean tryChangingState(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (shouldBeOpen(level, this.type.open) == this.type.open) {
         return false;
      } else {
         EyeblossomBlock.Type newType = this.type.transform();
         level.setBlock(pos, newType.block().defaultBlockState(), 3);
         level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(state));
         newType.spawnTransformParticle(level, pos, random);
         BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3)).forEach(nearby -> {
            if (level.getBlockState(nearby) == state) {
               double distance = Math.sqrt(pos.distSqr(nearby));
               level.scheduleTick(nearby, state.getBlock(), random.nextIntBetweenInclusive((int)(distance * 5.0), (int)(distance * 10.0)));
            }
         });
         return true;
      }
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (!level.isClientSide() && level.getDifficulty() != Difficulty.PEACEFUL && entity instanceof Bee bee && !bee.hasEffect(MobEffects.POISON)) {
         bee.addEffect(new MobEffectInstance(MobEffects.POISON, 25));
      }
   }

   public static enum Type {
      OPEN(true, MobEffects.BLINDNESS, 11, ModSounds.EYEBLOSSOM_OPEN_LONG, ModSounds.EYEBLOSSOM_OPEN, 16545810),
      CLOSED(false, MobEffects.CONFUSION, 7, ModSounds.EYEBLOSSOM_CLOSE_LONG, ModSounds.EYEBLOSSOM_CLOSE, 6250335);

      final boolean open;
      final MobEffect effect;
      final int effectDuration;
      final Supplier<SoundEvent> longSwitchSound;
      final Supplier<SoundEvent> shortSwitchSound;
      final int particleColor;

      private Type(
         boolean open,
         MobEffect effect,
         int effectDuration,
         Supplier<SoundEvent> longSwitchSound,
         Supplier<SoundEvent> shortSwitchSound,
         int particleColor
      ) {
         this.open = open;
         this.effect = effect;
         this.effectDuration = effectDuration;
         this.longSwitchSound = longSwitchSound;
         this.shortSwitchSound = shortSwitchSound;
         this.particleColor = particleColor;
      }

      Block block() {
         return this.open ? (Block)ModBlocks.OPEN_EYEBLOSSOM.get() : (Block)ModBlocks.CLOSED_EYEBLOSSOM.get();
      }

      EyeblossomBlock.Type transform() {
         return this.open ? CLOSED : OPEN;
      }

      void spawnTransformParticle(ServerLevel level, BlockPos pos, RandomSource random) {
         Vec3 start = Vec3.atCenterOf(pos);
         double lifetime = 0.5 + random.nextDouble();
         Vec3 velocity = new Vec3(random.nextDouble() - 0.5, random.nextDouble() + 1.0, random.nextDouble() - 0.5);
         Vec3 target = start.add(velocity.scale(lifetime));
         level.sendParticles(new TrailParticleOption(target, this.particleColor, (int)(20.0 * lifetime)), start.x, start.y, start.z, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }
}
