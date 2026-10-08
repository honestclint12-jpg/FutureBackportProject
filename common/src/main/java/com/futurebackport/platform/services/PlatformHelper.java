package com.futurebackport.platform.services;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Small loader differences that don't deserve their own service. */
public interface PlatformHelper {

    /** "neoforge", "fabric" or "forge". */
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    /**
     * A spawn egg for an entity type that may not be registered yet when the item is created
     * (NeoForge registers items before entity types).
     */
    SpawnEggItem spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties);

    /** Fire spread: how readily a block catches fire and how quickly it burns away. Call during common setup. */
    void setFlammable(Block block, int encouragement, int flammability);

    /** Lets players put {@code plant} into an empty flower pot, turning it into {@code pottedBlock}. Call during common setup. */
    void addPottedPlant(ResourceLocation plant, Supplier<? extends Block> pottedBlock);

    /** Whether other mods allow a crop-like block to grow this tick (NeoForge's CropGrowEvent.Pre). */
    boolean canCropGrow(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult);

    /** Tells other mods a crop-like block grew (NeoForge's CropGrowEvent.Post). */
    void onCropGrown(ServerLevel level, BlockPos pos, BlockState state);

    /** A biome's temperature after other mods' biome changes (vanilla's base value on loaders without them). */
    float getBiomeTemperature(Biome biome);

    /** A biome's downfall after other mods' biome changes. */
    float getBiomeDownfall(Biome biome);

    /** A creative tab builder; loaders lay tabs out differently. */
    CreativeModeTab.Builder creativeTabBuilder();

    /** A menu type whose client-side menu is built from extra data sent by {@link #openMenu}. */
    <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFactory<M> factory);

    /** Opens a menu and sends extra data to the client's {@link MenuFactory}. */
    void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData);

    @FunctionalInterface
    interface MenuFactory<M extends AbstractContainerMenu> {
        M create(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData);
    }
}
