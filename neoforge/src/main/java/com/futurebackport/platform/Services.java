package com.futurebackport.platform;

import com.futurebackport.platform.services.PlatformHelper;
import com.futurebackport.platform.services.RegistrationFactory;
import java.util.ServiceLoader;

/**
 * Entry point from shared code to loader-specific code. Each loader module ships an implementation of every service
 * interface, listed in its {@code META-INF/services} files; shared code only ever talks to the interfaces.
 */
public final class Services {

    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
    public static final RegistrationFactory REGISTRATION = load(RegistrationFactory.class);

    private Services() {
    }

    public static <T> T load(Class<T> service) {
        return ServiceLoader.load(service, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation of " + service.getName() + " for this loader"));
    }
}
