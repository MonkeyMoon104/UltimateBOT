package com.monkey.ultimatebot.api.event.lifecycle;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.BotSnapshot;
import java.util.Objects;
import org.bukkit.event.HandlerList;

/** Fired after a bot has been removed from the runtime registry. */
public final class BotDespawnEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotDespawnReason reason;

    public BotDespawnEvent(long sequence, BotSnapshot snapshot, BotEventSource source, BotDespawnReason reason) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), source, snapshot);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public BotDespawnReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
