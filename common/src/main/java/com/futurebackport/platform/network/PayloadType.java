package com.futurebackport.platform.network;

import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Channel id, class and decoder for one payload (Forge's SimpleChannel dispatches on the class). */
public record PayloadType<P extends Payload>(ResourceLocation id, Class<P> payloadClass, Function<FriendlyByteBuf, P> reader) {
}
