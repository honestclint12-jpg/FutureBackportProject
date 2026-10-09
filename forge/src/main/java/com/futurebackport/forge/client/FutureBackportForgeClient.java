package com.futurebackport.forge.client;

import com.futurebackport.FutureBackport;
import com.futurebackport.client.DryFoliageColor;
import com.futurebackport.client.ModClientSetup;
import com.futurebackport.client.SpearInput;
import com.futurebackport.platform.client.ClientRegistrars;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client glue: forwards Forge's client registration events to the shared {@link ModClientSetup}. */
@Mod.EventBusSubscriber(modid = FutureBackport.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FutureBackportForgeClient {

    private FutureBackportForgeClient() {
    }

    /** Called from the mod constructor on the client. */
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered event) -> {
            if (event.isAttack() && SpearInput.onAttackKey(event.getHand())) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        });
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (WoodType woodType : ModClientSetup.SIGN_WOOD_TYPES) {
                Sheets.addWoodType(woodType);
            }
            ModClientSetup.blockRenderLayers(ItemBlockRenderTypes::setRenderLayer);
            ModClientSetup.menuScreens(new ClientRegistrars.MenuScreens() {
                @Override
                @SuppressWarnings("unchecked")
                public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientRegistrars.ScreenFactory<M, S> factory) {
                    MenuScreens.register((MenuType<M>) type, factory::create);
                }
            });
            ModClientSetup.itemRenderers(FutureBackportForgeClient::setItemRenderer);
        });
    }

    /**
     * Forge 1.20.1 only reads an item's client extensions from {@code Item.initializeClient}, which runs in the item's
     * constructor. The shared items are plain vanilla classes, so the extensions are set on them afterwards.
     */
    private static void setItemRenderer(java.util.function.Supplier<? extends BlockEntityWithoutLevelRenderer> renderer, Item... items) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer instance;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.instance == null) {
                    this.instance = renderer.get();
                }
                return this.instance;
            }
        };
        try {
            Field field = Item.class.getDeclaredField("renderProperties");
            field.setAccessible(true);
            for (Item item : items) {
                field.set(item, extensions);
            }
        } catch (ReflectiveOperationException e) {
            FutureBackport.LOGGER.error("Could not set the item renderer for {}", (Object) items, e);
        }
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModClientSetup.entityRenderers(new ClientRegistrars.EntityRenderers() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }, new ClientRegistrars.BlockEntityRenderers() {
            @Override
            public <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider) {
                event.registerBlockEntityRenderer(type, provider);
            }
        });
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModClientSetup.layerDefinitions(event::registerLayerDefinition);
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        ModClientSetup.particles(new ClientRegistrars.Particles() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> provider) {
                event.registerSpriteSet(type, provider);
            }
        });
    }

    @SubscribeEvent
    static void registerColorResolvers(RegisterColorHandlersEvent.ColorResolvers event) {
        event.register(DryFoliageColor.RESOLVER);
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        ModClientSetup.blockColors(event::register);
    }

    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModClientSetup.itemColors(event::register);
    }

    @SubscribeEvent
    static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new DryFoliageColor.ReloadListener());
    }
}
