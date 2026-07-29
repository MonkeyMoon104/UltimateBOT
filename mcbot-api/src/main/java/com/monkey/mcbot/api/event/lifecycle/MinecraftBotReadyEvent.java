package com.monkey.mcbot.api.event.lifecycle;

import com.monkey.mcbot.api.MinecraftBotAPI;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Bukkit event fired when the MinecraftBot API is fully initialized and registered.
 *
 * <p>Plugins that depend on MinecraftBot can listen to this event to safely obtain
 * the API instance and perform initialization that requires bot services.</p>
 */
public final class MinecraftBotReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MinecraftBotAPI api;

    /**
     * Creates the event with a non-null API instance.
     *
     * @param api initialized MinecraftBot API
     */
    public MinecraftBotReadyEvent(MinecraftBotAPI api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    /**
     * Returns the API instance associated with this ready event.
     *
     * @return ready API
     */
    public MinecraftBotAPI getApi() {
        return api;
    }

    /**
     * Bukkit handler accessor.
     *
     * @return handlers for this event type
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Static Bukkit handler accessor.
     *
     * @return handlers for this event type
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
