package com.futurebackport.fabric;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.DryFoliageColor;
import com.futurebackport.client.ModClientSetup;
import com.futurebackport.client.SpearInput;
import com.futurebackport.fabric.platform.FabricClientNetworking;
import com.futurebackport.platform.client.ClientRegistrars;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Fabric client entry point: registers everything {@link ModClientSetup} declares with Fabric's client APIs. */
public class FutureBackportFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricClientNetworking.registerReceivers();
        // clickCount is 0 while the key is held (block breaking); the spear only reacts to presses.
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> clickCount > 0 && SpearInput.onAttackKey(InteractionHand.MAIN_HAND));

        for (WoodType woodType : ModClientSetup.SIGN_WOOD_TYPES) {
            addSignMaterials(woodType);
        }
        ModClientSetup.blockRenderLayers(BlockRenderLayerMap.INSTANCE::putBlock);
        ModClientSetup.layerDefinitions((layer, definition) -> EntityModelLayerRegistry.registerModelLayer(layer, definition::get));
        ModClientSetup.entityRenderers(new ClientRegistrars.EntityRenderers() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        }, new ClientRegistrars.BlockEntityRenderers() {
            @Override
            public <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                BlockEntityRenderers.register(type, provider);
            }
        });
        ModClientSetup.particles(new ClientRegistrars.Particles() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> provider) {
                ParticleFactoryRegistry.getInstance().register(type, provider::create);
            }
        });
        ModClientSetup.menuScreens(new ClientRegistrars.MenuScreens() {
            @Override
            @SuppressWarnings("unchecked")
            public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientRegistrars.ScreenFactory<M, S> factory) {
                MenuScreens.<M, S>register((MenuType<M>) type, factory::create);
            }
        });
        ModClientSetup.blockColors((color, blocks) -> ColorProviderRegistry.BLOCK.register(color, blocks));
        ModClientSetup.itemColors((color, items) -> ColorProviderRegistry.ITEM.register(color, items));
        ModClientSetup.itemRenderers((renderer, items) -> {
            LazyItemRenderer lazy = new LazyItemRenderer(renderer);
            for (Item item : items) {
                BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, context, pose, buffers, light, overlay) ->
                        lazy.get().renderByItem(stack, context, pose, buffers, light, overlay));
            }
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new DryFoliageReloadListener());
    }

    /** Vanilla only builds sign materials for its own wood types, without namespaces. */
    private static void addSignMaterials(WoodType woodType) {
        ResourceLocation name = new ResourceLocation(woodType.name());
        Sheets.SIGN_MATERIALS.putIfAbsent(woodType, new Material(Sheets.SIGN_SHEET,
                new ResourceLocation(name.getNamespace(), "entity/signs/" + name.getPath())));
        Sheets.HANGING_SIGN_MATERIALS.putIfAbsent(woodType, new Material(Sheets.SIGN_SHEET,
                new ResourceLocation(name.getNamespace(), "entity/signs/hanging/" + name.getPath())));
    }

    private static final class LazyItemRenderer {
        private final java.util.function.Supplier<? extends BlockEntityWithoutLevelRenderer> factory;
        private BlockEntityWithoutLevelRenderer renderer;

        LazyItemRenderer(java.util.function.Supplier<? extends BlockEntityWithoutLevelRenderer> factory) {
            this.factory = factory;
        }

        BlockEntityWithoutLevelRenderer get() {
            if (renderer == null) {
                renderer = factory.get();
            }
            return renderer;
        }
    }

    /** Fabric needs reload listeners to have an id; delegate to the shared listener. */
    private static final class DryFoliageReloadListener implements IdentifiableResourceReloadListener {
        private final DryFoliageColor.ReloadListener delegate = new DryFoliageColor.ReloadListener();

        @Override
        public ResourceLocation getFabricId() {
            return FutureBackport.id("dry_foliage_color");
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                              ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                              Executor prepareExecutor, Executor applyExecutor) {
            return delegate.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
        }
    }
}
