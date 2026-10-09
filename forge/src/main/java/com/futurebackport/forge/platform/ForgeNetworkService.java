package com.futurebackport.forge.platform;

import com.futurebackport.FutureBackport;
import com.futurebackport.platform.network.Payload;
import com.futurebackport.platform.network.PayloadHandler;
import com.futurebackport.platform.network.PayloadType;
import com.futurebackport.platform.services.NetworkService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** One SimpleChannel carries every payload; each payload type gets its own message index. */
public final class ForgeNetworkService implements NetworkService {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            FutureBackport.id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static final List<Runnable> DECLARATIONS = new ArrayList<>();
    private static int nextIndex;
    private static boolean registered;

    /** Registers the declared payloads (common setup). */
    public static synchronized void register() {
        if (!registered) {
            registered = true;
            DECLARATIONS.forEach(Runnable::run);
        }
    }

    @Override
    public <P extends Payload> void playToClient(PayloadType<P> type, PayloadHandler<P> handler) {
        declare(type, handler, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public <P extends Payload> void playToServer(PayloadType<P> type, PayloadHandler<P> handler) {
        declare(type, handler, NetworkDirection.PLAY_TO_SERVER);
    }

    private static synchronized <P extends Payload> void declare(PayloadType<P> type, PayloadHandler<P> handler, NetworkDirection direction) {
        DECLARATIONS.add(() -> CHANNEL.registerMessage(
                nextIndex++,
                type.payloadClass(),
                Payload::write,
                type.reader(),
                (payload, context) -> handle(payload, context, handler),
                Optional.of(direction)));
    }

    private static <P extends Payload> void handle(P payload, Supplier<NetworkEvent.Context> contextSupplier, PayloadHandler<P> handler) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getDirection() == NetworkDirection.PLAY_TO_SERVER ? context.getSender() : ClientPlayer.get();
            if (player != null) {
                handler.handle(payload, player);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, Payload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayersTrackingEntity(Entity entity, Payload payload) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    @Override
    public void sendToServer(Payload payload) {
        CHANNEL.sendToServer(payload);
    }

    /** Keeps the client class reference out of the server's class loading path. */
    private static final class ClientPlayer {
        static Player get() {
            return FMLEnvironment.dist.isClient() ? Minecraft.getInstance().player : null;
        }
    }
}
