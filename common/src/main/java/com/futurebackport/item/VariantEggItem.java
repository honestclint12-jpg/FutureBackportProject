package com.futurebackport.item;

import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.entity.VariantThrownEgg;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class VariantEggItem extends EggItem {
   private final FarmAnimalVariant variant;

   public VariantEggItem(FarmAnimalVariant variant, Properties properties) {
      super(properties);
      this.variant = variant;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      level.playSound(
         null,
         player.getX(),
         player.getY(),
         player.getZ(),
         SoundEvents.EGG_THROW,
         SoundSource.PLAYERS,
         0.5F,
         0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
      );
      if (!level.isClientSide()) {
         VariantThrownEgg egg = new VariantThrownEgg(level, player, this.variant, this);
         egg.setItem(stack);
         egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
         level.addFreshEntity(egg);
      }

      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }

   /** Dispensers fire variant eggs like vanilla eggs (1.20.1 has no ProjectileItem). */
   public static final DispenseItemBehavior DISPENSE_BEHAVIOR = new AbstractProjectileDispenseBehavior() {
      @Override
      protected Projectile getProjectile(Level level, Position pos, ItemStack stack) {
         return ((VariantEggItem)stack.getItem()).asProjectile(level, pos, stack, null);
      }
   };

   public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
      VariantThrownEgg egg = new VariantThrownEgg(level, pos.x(), pos.y(), pos.z(), this.variant, this);
      egg.setItem(stack);
      return egg;
   }
}
