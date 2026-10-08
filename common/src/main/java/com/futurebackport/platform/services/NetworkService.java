package com.futurebackport.platform.services;

import com.futurebackport.platform.network.PayloadHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Custom packets. Declare every payload during mod construction; loaders register them at the right time.
 * Client-bound handlers must not touch client-only classes directly (they are declared on both sides).
 */
public interface NetworkService {

    <P extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler);

    <P extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload);

    /** Client only. */
    void sendToServer(CustomPacketPayload payload);
}
