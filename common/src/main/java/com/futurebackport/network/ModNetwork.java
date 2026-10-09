package com.futurebackport.network;

import com.futurebackport.platform.Services;

/** Declares every custom packet. Called once during mod construction on every loader. */
public final class ModNetwork {

    private ModNetwork() {
    }

    public static void register() {
        Services.NETWORK.playToClient(FarmAnimalVariantPayload.TYPE, FarmAnimalVariantPayload::handle);
        Services.NETWORK.playToServer(SpearJabPayload.TYPE, SpearJabPayload::handle);
    }
}
