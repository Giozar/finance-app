package com.giozar04.bootstrap;

import java.util.HashMap;
import java.util.Map;

/** Punto de composición de los casos de uso consumidos por las vistas. */
public final class ClientUseCases {
    private static final Map<Class<?>, Object> REGISTERED = new HashMap<>();

    private ClientUseCases() { }

    public static <T> void register(Class<T> port, T useCase) {
        REGISTERED.put(port, port.cast(useCase));
    }

    public static <T> T get(Class<T> port) {
        T useCase = port.cast(REGISTERED.get(port));
        if (useCase == null) {
            throw new IllegalStateException("Caso de uso no inicializado: " + port.getName());
        }
        return useCase;
    }
}
