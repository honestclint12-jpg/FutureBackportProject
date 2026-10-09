package com.futurebackport.fabric.platform;

import com.futurebackport.platform.network.Payload;
import com.futurebackport.platform.network.PayloadHandler;
import com.futurebackport.platform.network.PayloadType;
import com.futurebackport.platform.services.NetworkService;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Server receivers register immediately. Client receivers are kept until the client entrypoint calls
 * {@link FabricClientNetworking#registerReceivers()}, since the client networking API only exists there.
 */
public final class FabricNetworkService implements NetworkService {

    static final List<ClientReceiver<?>> CLIENT_RECEIVERS = new ArrayList<>();

    record ClientReceiver<P extends Payload>(PayloadType<P> type, PayloadHandler<P> handler) {
    }

    @Override
    public <P extends Payload> void playToClient(PayloadType<P> type, PayloadHandler<P> handler) {
        CLIENT_RECEIVERS.add(new ClientReceiver<>(type, handler));
    }

    @Override
    public <P extends Payload> void playToServer(PayloadType<P> type, PayloadHandler<P> handler) {
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, listener, buf, sender) -> {
            P payload = type.reader().apply(buf);
            server.execute(() -> handler.handle(payload, player));
        });
    }

    static FriendlyByteBuf encode(Payload payload) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buf);
        return buf;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Payload payload) {
        ServerPlayNetworking.send(player, payload.type().id(), encode(payload));
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, Payload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToServer(Payload payload) {
        FabricClientNetworking.sendToServer(payload);
    }
}
