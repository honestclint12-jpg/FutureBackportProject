package com.futurebackport.forge.platform;

import com.futurebackport.platform.services.PlatformHelper;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkHooks;

public final class ForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public SpawnEggItem spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties) {
        return new ForgeSpawnEggItem(type, backgroundColor, highlightColor, properties);
    }

    @Override
    public void setFlammable(Block block, int encouragement, int flammability) {
        ((FireBlock) Blocks.FIRE).setFlammable(block, encouragement, flammability);
    }

    @Override
    public void addPottedPlant(ResourceLocation plant, Supplier<? extends Block> pottedBlock) {
        ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(plant, pottedBlock);
    }

    @Override
    public boolean canCropGrow(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult) {
        return ForgeHooks.onCropsGrowPre(level, pos, state, defaultResult);
    }

    @Override
    public void onCropGrown(ServerLevel level, BlockPos pos, BlockState state) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    @Override
    public float getBiomeTemperature(Biome biome) {
        return biome.getModifiedClimateSettings().temperature();
    }

    @Override
    public float getBiomeDownfall(Biome biome) {
        return biome.getModifiedClimateSettings().downfall();
    }

    @Override
    public Item musicDisc(int comparatorOutput, Supplier<SoundEvent> sound, Item.Properties properties, int lengthInSeconds) {
        // Forge's supplier constructor takes the length in ticks.
        return new RecordItem(comparatorOutput, sound, properties, lengthInSeconds * 20);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFactory<M> factory) {
        return IForgeMenuType.create(factory::create);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        NetworkHooks.openScreen(player, provider, extraData);
    }
}
