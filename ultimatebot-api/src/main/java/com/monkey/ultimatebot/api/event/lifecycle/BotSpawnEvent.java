package com.monkey.ultimatebot.api.event.lifecycle;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.BotSnapshot;
import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;

/** Fired after a bot is spawned and registered. */
public final class BotSpawnEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final HumanEntity bot;

    public BotSpawnEvent(long sequence, BotSnapshot snapshot, BotEventSource source, HumanEntity bot) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), source, snapshot);
        this.bot = Objects.requireNonNull(bot, "bot");
    }

    public HumanEntity getBot() {
        return bot;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
