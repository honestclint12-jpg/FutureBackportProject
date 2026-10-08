package com.futurebackport.fabric.platform;

import com.futurebackport.platform.network.PayloadHandler;
import com.futurebackport.platform.services.NetworkService;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Payload types and server receivers register immediately. Client receivers are kept until the client entrypoint
 * calls {@link FabricClientNetworking#registerReceivers()}, since the client networking API only exists there.
 */
public final class FabricNetworkService implements NetworkService {

    static final List<ClientReceiver<?>> CLIENT_RECEIVERS = new ArrayList<>();

    record ClientReceiver<P extends CustomPacketPayload>(CustomPacketPayload.Type<P> type, PayloadHandler<P> handler) {
    }

    @Override
    public <P extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler) {
        PayloadTypeRegistry.playS2C().register(type, codec);
        CLIENT_RECEIVERS.add(new ClientReceiver<>(type, handler));
    }

    @Override
    public <P extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler) {
        PayloadTypeRegistry.playC2S().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context.player()));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        FabricClientNetworking.sendToServer(payload);
    }
}
