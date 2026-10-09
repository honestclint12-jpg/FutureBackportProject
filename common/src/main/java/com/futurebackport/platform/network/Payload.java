package com.futurebackport.platform.network;

import net.minecraft.network.FriendlyByteBuf;

/** A custom packet. 1.20.1 has no CustomPacketPayload, so payloads write themselves and name their {@link PayloadType}. */
public interface Payload {

    PayloadType<?> type();

    void write(FriendlyByteBuf buf);
}
