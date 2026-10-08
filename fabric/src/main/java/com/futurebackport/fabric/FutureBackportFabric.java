package com.futurebackport.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureBackportFabric implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Future Backport");

    @Override
    public void onInitialize() {
        LOGGER.info("Future Backport for Fabric is a work in progress: no content is registered yet.");
    }
}
