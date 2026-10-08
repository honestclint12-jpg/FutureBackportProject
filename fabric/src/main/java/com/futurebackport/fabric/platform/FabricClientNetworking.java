package com.futurebackport.fabric.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-only half of {@link FabricNetworkService}. Only loaded on the client. */
public final class FabricClientNetworking {

    private FabricClientNetworking() {
    }

    public static void registerReceivers() {
        FabricNetworkService.CLIENT_RECEIVERS.forEach(FabricClientNetworking::register);
    }

    private static <P extends CustomPacketPayload> void register(FabricNetworkService.ClientReceiver<P> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(receiver.type(), (payload, context) -> receiver.handler().handle(payload, context.player()));
    }

    static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
