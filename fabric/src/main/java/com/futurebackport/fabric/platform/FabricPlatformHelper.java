package com.futurebackport.fabric.platform;

import com.futurebackport.platform.services.PlatformHelper;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class FabricPlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public SpawnEggItem spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties) {
        // Entity types are registered before items here, so the type already exists.
        return new SpawnEggItem(type.get(), backgroundColor, highlightColor, properties);
    }

    @Override
    public void setFlammable(Block block, int encouragement, int flammability) {
        FlammableBlockRegistry.getDefaultInstance().add(block, flammability, encouragement);
    }

    @Override
    public void addPottedPlant(ResourceLocation plant, Supplier<? extends Block> pottedBlock) {
        // Nothing to do: vanilla's FlowerPotBlock constructor already maps the plant to its pot.
    }

    @Override
    public boolean canCropGrow(ServerLevel level, BlockPos pos, BlockState state, boolean defaultResult) {
        return defaultResult;
    }

    @Override
    public void onCropGrown(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @Override
    public float getBiomeTemperature(Biome biome) {
        return biome.getBaseTemperature();
    }

    @Override
    public float getBiomeDownfall(Biome biome) {
        return biome.climateSettings.downfall();
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuFactory<M> factory) {
        // Fabric sends the extra data as a typed object; carry the raw bytes and rebuild a buffer on the client.
        return new ExtendedScreenHandlerType<>(
                (containerId, inventory, data) -> factory.create(containerId, inventory, wrap(data, inventory)),
                ByteBufCodecs.BYTE_ARRAY);
    }

    private static RegistryFriendlyByteBuf wrap(byte[] data, Inventory inventory) {
        return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        extraData.accept(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        player.openMenu(new ExtendedScreenHandlerFactory<byte[]>() {
            @Override
            public byte[] getScreenOpeningData(ServerPlayer target) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return provider.createMenu(containerId, inventory, menuPlayer);
            }
        });
    }
}
