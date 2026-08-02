package com.monkey.ultimatebot.common.guard;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Java-only callbacks exposed by the plugin core to the guard addon. */
public record GuardBackendContext(
        Object pluginHandle, Predicate<UUID> managedBotPredicate, Consumer<Object> listenerRegistrar) {
    public GuardBackendContext {
        Objects.requireNonNull(pluginHandle, "pluginHandle");
        Objects.requireNonNull(managedBotPredicate, "managedBotPredicate");
        Objects.requireNonNull(listenerRegistrar, "listenerRegistrar");
    }
}
