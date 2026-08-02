package com.monkey.ultimatebot.api.event.combat;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.BotSnapshot;
import org.bukkit.event.HandlerList;

/** Fired after one or more totems have been consumed by a bot. */
public final class BotTotemUseEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final int consumed;
    private final int remaining;

    public BotTotemUseEvent(long sequence, BotSnapshot snapshot, int consumed, int remaining) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BUKKIT, snapshot);
        this.consumed = consumed;
        this.remaining = remaining;
    }

    public int getConsumed() {
        return consumed;
    }

    public int getRemaining() {
        return remaining;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
