package com.monkey.ultimatebot.api.event.lifecycle;

import com.monkey.ultimatebot.api.UltimateBotAPI;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event fired when the UltimateBot API is fully initialized and registered.
 *
 * <p>Plugins that depend on UltimateBot can listen to this event to safely obtain
 * the API instance and perform initialization that requires bot services.</p>
 */
public final class UltimateBotReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UltimateBotAPI api;

    /**
     * Creates the event with a non-null API instance.
     *
     * @param api initialized UltimateBot API
     */
    public UltimateBotReadyEvent(UltimateBotAPI api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    /**
     * Returns the API instance associated with this ready event.
     *
     * @return ready API
     */
    public UltimateBotAPI getApi() {
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
