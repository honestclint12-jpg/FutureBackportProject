package com.futurebackport;

import com.futurebackport.client.DryFoliageColor;
import com.futurebackport.client.ModClientSetup;
import com.futurebackport.client.SpearInput;
import com.futurebackport.client.dev.Showcase;
import com.futurebackport.platform.client.ClientRegistrars;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ColorResolvers;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;



@Mod(
   value = "futurebackport",
   dist = {Dist.CLIENT}
)
@EventBusSubscriber(
   modid = "futurebackport",
   value = {Dist.CLIENT}
)
public class FutureBackportClient {
   public FutureBackportClient() {
      Showcase.register();
      NeoForge.EVENT_BUS.addListener((InteractionKeyMappingTriggered event) -> {
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
      });
   }

   @SubscribeEvent
   static void registerRenderers(RegisterRenderers event) {
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
   static void registerLayers(RegisterLayerDefinitions event) {
      ModClientSetup.layerDefinitions(event::registerLayerDefinition);
   }

   @SubscribeEvent
   static void registerClientExtensions(RegisterClientExtensionsEvent event) {
      ModClientSetup.itemRenderers((renderer, items) -> event.registerItem(new IClientItemExtensions() {
         private BlockEntityWithoutLevelRenderer instance;

         @Override
         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.instance == null) {
               this.instance = renderer.get();
            }
            return this.instance;
         }
      }, items));
   }

   @SubscribeEvent
   static void registerScreens(RegisterMenuScreensEvent event) {
      ModClientSetup.menuScreens(new ClientRegistrars.MenuScreens() {
         @Override
         public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClientRegistrars.ScreenFactory<M, S> factory) {
            event.<M, S>register((MenuType<M>) type, factory::create);
         }
      });
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
   static void registerColorResolvers(ColorResolvers event) {
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
