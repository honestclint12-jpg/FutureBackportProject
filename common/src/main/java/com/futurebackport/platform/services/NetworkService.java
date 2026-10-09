package com.futurebackport.platform.services;

import com.futurebackport.platform.network.Payload;
import com.futurebackport.platform.network.PayloadHandler;
import com.futurebackport.platform.network.PayloadType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Custom packets. Declare every payload during mod construction; loaders register them at the right time.
 * Client-bound handlers must not touch client-only classes directly (they are declared on both sides).
 */
public interface NetworkService {

    <P extends Payload> void playToClient(PayloadType<P> type, PayloadHandler<P> handler);

    <P extends Payload> void playToServer(PayloadType<P> type, PayloadHandler<P> handler);

    void sendToPlayer(ServerPlayer player, Payload payload);

    void sendToPlayersTrackingEntity(Entity entity, Payload payload);

    /** Client only. */
    void sendToServer(Payload payload);
}
