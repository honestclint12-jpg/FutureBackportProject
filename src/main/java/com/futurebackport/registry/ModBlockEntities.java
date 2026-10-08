package com.futurebackport.registry;

import com.futurebackport.block.entity.CopperChestBlockEntity;
import com.futurebackport.block.entity.CopperGolemStatueBlockEntity;
import com.futurebackport.block.entity.CreakingHeartBlockEntity;
import com.futurebackport.block.entity.ShelfBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "futurebackport");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreakingHeartBlockEntity>> CREAKING_HEART = BLOCK_ENTITIES.register(
      "creaking_heart", () -> Builder.of(CreakingHeartBlockEntity::new, new Block[]{(Block)ModBlocks.CREAKING_HEART.get()}).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopperGolemStatueBlockEntity>> COPPER_GOLEM_STATUE = BLOCK_ENTITIES.register(
      "copper_golem_statue",
      () -> Builder.of(CopperGolemStatueBlockEntity::new, ModBlocks.COPPER_GOLEM_STATUE.all().stream().map(DeferredHolder::get).toArray(Block[]::new))
         .build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CopperChestBlockEntity>> COPPER_CHEST = BLOCK_ENTITIES.register(
      "copper_chest",
      () -> Builder.of(CopperChestBlockEntity::new, ModBlocks.COPPER_CHEST.all().stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null)
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShelfBlockEntity>> SHELF = BLOCK_ENTITIES.register(
      "shelf", () -> Builder.of(ShelfBlockEntity::new, ModBlocks.SHELVES.values().stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null)
   );
}
