package com.futurebackport.neoforge.platform;

import com.futurebackport.platform.network.PayloadHandler;
import com.futurebackport.platform.services.NetworkService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Collects payload declarations and registers them when NeoForge fires {@link RegisterPayloadHandlersEvent}. */
public final class NeoForgeNetworkService implements NetworkService {

    private static final String PROTOCOL_VERSION = "1";
    private static final List<Consumer<PayloadRegistrar>> DECLARATIONS = new ArrayList<>();

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        DECLARATIONS.forEach(declaration -> declaration.accept(registrar));
    }

    @Override
    public <P extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler) {
        DECLARATIONS.add(registrar -> registrar.playToClient(type, codec, (payload, context) -> handler.handle(payload, context.player())));
    }

    @Override
    public <P extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<P> type, StreamCodec<? super RegistryFriendlyByteBuf, P> codec, PayloadHandler<P> handler) {
        DECLARATIONS.add(registrar -> registrar.playToServer(type, codec, (payload, context) -> handler.handle(payload, context.player())));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
