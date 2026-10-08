package com.futurebackport.mixin;

import com.futurebackport.entity.FarmAnimalVariant;
import com.futurebackport.item.VariantEggItem;
import com.futurebackport.registry.ModItems;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({Chicken.class})
public abstract class ChickenMixin {
   @Redirect(
      method = {"aiStep"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"
      )
   )
   private ItemEntity futurebackport$layVariantEgg(Chicken chicken, ItemLike item) {
      ItemLike egg = (ItemLike)(switch (FarmAnimalVariant.get(chicken)) {
         case WARM -> (VariantEggItem)ModItems.BROWN_EGG.get();
         case COLD -> (VariantEggItem)ModItems.BLUE_EGG.get();
         default -> item;
      });
      return chicken.spawnAtLocation(egg);
   }
}
