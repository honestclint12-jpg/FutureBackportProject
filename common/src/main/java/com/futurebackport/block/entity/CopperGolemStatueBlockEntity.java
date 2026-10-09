package com.futurebackport.block.entity;

import com.futurebackport.block.CopperGolemStatueBlock;
import com.futurebackport.entity.CopperGolem;
import com.futurebackport.registry.ModBlockEntities;
import com.futurebackport.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CopperGolemStatueBlockEntity extends BlockEntity {
   @Nullable
   private Component name;

   public CopperGolemStatueBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.COPPER_GOLEM_STATUE.get(), pos, state);
   }

   public void createStatue(CopperGolem golem) {
      this.name = golem.getCustomName();
      this.setChanged();
   }

   @Nullable
   public CopperGolem removeStatue(BlockState state) {
      CopperGolem golem = (CopperGolem)((EntityType)ModEntities.COPPER_GOLEM.get()).create(this.level);
      if (golem == null) {
         return null;
      } else {
         golem.setCustomName(this.name);
         BlockPos pos = this.getBlockPos();
         float yRot = ((Direction)state.getValue(CopperGolemStatueBlock.FACING)).toYRot();
         golem.moveTo(Vec3.atCenterOf(pos).x, pos.getY(), Vec3.atCenterOf(pos).z, yRot, 0.0F);
         golem.yHeadRot = yRot;
         golem.yBodyRot = yRot;
         golem.spawn(WeatherState.UNAFFECTED);
         return golem;
      }
   }

   protected void collectImplicitComponents(Builder components) {
      super.collectImplicitComponents(components);
      components.set(DataComponents.CUSTOM_NAME, this.name);
   }

   protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
      super.applyImplicitComponents(input);
      this.name = (Component)input.get(DataComponents.CUSTOM_NAME);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      if (this.name != null) {
         tag.putString("CustomName", Serializer.toJson(this.name, registries));
      }
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.name = tag.contains("CustomName") ? Serializer.fromJson(tag.getString("CustomName"), registries) : null;
   }
}
