package com.monkey.ultimatebot.common.guard;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Java-only callbacks exposed by the plugin core to the guard addon. */
public final class GuardBackendContext {
    private final Object pluginHandle;
    private final Predicate<UUID> managedBotPredicate;
    private final Consumer<Object> listenerRegistrar;

    public GuardBackendContext(
            Object pluginHandle, Predicate<UUID> managedBotPredicate, Consumer<Object> listenerRegistrar) {
        this.pluginHandle = Objects.requireNonNull(pluginHandle, "pluginHandle");
        this.managedBotPredicate = Objects.requireNonNull(managedBotPredicate, "managedBotPredicate");
        this.listenerRegistrar = Objects.requireNonNull(listenerRegistrar, "listenerRegistrar");
    }

    public Object pluginHandle() {
        return pluginHandle;
    }

    public Predicate<UUID> managedBotPredicate() {
        return managedBotPredicate;
    }

    public Consumer<Object> listenerRegistrar() {
        return listenerRegistrar;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GuardBackendContext)) {
            return false;
        }
        GuardBackendContext other = (GuardBackendContext) obj;
        return pluginHandle.equals(other.pluginHandle)
                && managedBotPredicate.equals(other.managedBotPredicate)
                && listenerRegistrar.equals(other.listenerRegistrar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginHandle, managedBotPredicate, listenerRegistrar);
    }

    @Override
    public String toString() {
        return "GuardBackendContext[pluginHandle="
                + pluginHandle
                + ", managedBotPredicate="
                + managedBotPredicate
                + ", listenerRegistrar="
                + listenerRegistrar
                + ']';
    }
}
