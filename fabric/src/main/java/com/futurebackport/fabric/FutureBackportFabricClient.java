package com.futurebackport.fabric;

import com.futurebackport.client.SpearInput;
import com.futurebackport.fabric.platform.FabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.world.InteractionHand;

/**
 * Fabric client entry point. Entity, block entity and particle renderers are not wired up on Fabric yet: that needs
 * the client registration layer (the NeoForge side does it in FutureBackportClient).
 */
public class FutureBackportFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricClientNetworking.registerReceivers();
        // clickCount is 0 while the key is held (block breaking); the spear only reacts to presses.
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> clickCount > 0 && SpearInput.onAttackKey(InteractionHand.MAIN_HAND));
    }
}
