package com.monkey.mcbot.api.event.action;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.event.base.BotEventSource;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import java.util.Objects;

/** Fired before a managed bot teleports. */
public final class BotTeleportEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Location from;
    private Location to;
    private final String cause;
    private boolean cancelled;

    public BotTeleportEvent(long sequence, BotSnapshot snapshot, Location from, Location to, String cause) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), BotEventSource.BUKKIT, snapshot);
        this.from = Objects.requireNonNull(from, "from").clone();
        this.to = Objects.requireNonNull(to, "to").clone();
        this.cause = cause == null ? "UNKNOWN" : cause;
    }
    public Location getFrom() { return from.clone(); }
    public Location getTo() { return to.clone(); }
    public void setTo(Location to) { this.to = Objects.requireNonNull(to, "to").clone(); }
    public String getCause() { return cause; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
