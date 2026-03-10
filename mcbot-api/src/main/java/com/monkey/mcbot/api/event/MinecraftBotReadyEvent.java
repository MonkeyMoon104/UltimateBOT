package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.MinecraftBotAPI;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public final class MinecraftBotReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final MinecraftBotAPI api;

    public MinecraftBotReadyEvent(MinecraftBotAPI api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    public MinecraftBotAPI getApi() {
        return api;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
