package com.futurebackport.platform.network;

import net.minecraft.world.entity.player.Player;

/** Handles a received payload on the main thread. {@code player} is the sender on the server, the local player on the client. */
@FunctionalInterface
public interface PayloadHandler<P extends Payload> {

    void handle(P payload, Player player);
}
