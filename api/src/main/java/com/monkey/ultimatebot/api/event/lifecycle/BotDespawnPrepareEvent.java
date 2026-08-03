package com.monkey.ultimatebot.api.event.lifecycle;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/** Fired before a voluntary bot removal. Forced shutdown, replacement and death are not vetoable. */
public final class BotDespawnPrepareEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotDespawnReason reason;
    private boolean cancelled;

    public BotDespawnPrepareEvent(long sequence, BotSnapshot snapshot, BotEventSource source, BotDespawnReason reason) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), source, snapshot);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public BotDespawnReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
