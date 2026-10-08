package com.futurebackport.client;

import com.futurebackport.item.SpearItem;
import com.futurebackport.network.SpearJabPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "futurebackport",
   value = {Dist.CLIENT}
)
public final class SpearInput {
   private SpearInput() {
   }

   @SubscribeEvent
   static void onAttack(InteractionKeyMappingTriggered event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (event.isAttack() && player != null && player.getMainHandItem().getItem() instanceof SpearItem) {
         if (mc.hitResult == null || mc.hitResult.getType() != Type.BLOCK) {
            event.setCanceled(true);
            event.setSwingHand(false);
            if (!player.isSpectator() && !player.isUsingItem() && !(player.getAttackStrengthScale(0.5F) < 1.0F)) {
               PacketDistributor.sendToServer(new SpearJabPayload(), new CustomPacketPayload[0]);
               player.resetAttackStrengthTicker();
               player.swing(event.getHand());
            }
         }
      }
   }
}
