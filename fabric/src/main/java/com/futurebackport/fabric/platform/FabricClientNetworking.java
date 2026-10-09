package com.futurebackport.fabric.platform;

import com.futurebackport.platform.network.Payload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client-only half of {@link FabricNetworkService}. Only loaded on the client. */
public final class FabricClientNetworking {

    private FabricClientNetworking() {
    }

    public static void registerReceivers() {
        FabricNetworkService.CLIENT_RECEIVERS.forEach(FabricClientNetworking::register);
    }

    private static <P extends Payload> void register(FabricNetworkService.ClientReceiver<P> receiver) {
        ClientPlayNetworking.registerGlobalReceiver(receiver.type().id(), (client, listener, buf, sender) -> {
            P payload = receiver.type().reader().apply(buf);
            client.execute(() -> {
                if (client.player != null) {
                    receiver.handler().handle(payload, client.player);
                }
            });
        });
    }

    static void sendToServer(Payload payload) {
        ClientPlayNetworking.send(payload.type().id(), FabricNetworkService.encode(payload));
    }
}
