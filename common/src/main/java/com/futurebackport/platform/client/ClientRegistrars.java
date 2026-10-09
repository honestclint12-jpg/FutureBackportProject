package com.futurebackport.platform.client;

import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Receivers for client registrations with generic signatures (lambdas can't be generic, so loaders implement these).
 * See {@link com.futurebackport.client.ModClientSetup}.
 */
public final class ClientRegistrars {

    private ClientRegistrars() {
    }

    public interface EntityRenderers {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public interface BlockEntityRenderers {
        <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider);
    }

    public interface Particles {
        <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> provider);
    }

    public interface MenuScreens {
        <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ScreenFactory<M, S> factory);
    }

    @FunctionalInterface
    public interface ScreenFactory<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> {
        S create(M menu, Inventory inventory, Component title);
    }

    /** Lazily created item renderer for items whose model uses {@code builtin/entity}. */
    public interface ItemRenderers {
        void register(Supplier<? extends net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer> renderer, net.minecraft.world.item.Item... items);
    }
}
