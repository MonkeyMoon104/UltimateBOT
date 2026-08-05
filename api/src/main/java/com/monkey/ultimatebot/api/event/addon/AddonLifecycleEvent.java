package com.monkey.ultimatebot.api.event.addon;

import com.monkey.ultimatebot.api.addon.AddonSnapshot;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Fired when an external UltimateBot addon changes lifecycle state. */
public final class AddonLifecycleEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final AddonSnapshot addon;

    public AddonLifecycleEvent(AddonSnapshot addon) {
        this.addon = Objects.requireNonNull(addon, "addon");
    }

    public AddonSnapshot getAddon() {
        return addon;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
