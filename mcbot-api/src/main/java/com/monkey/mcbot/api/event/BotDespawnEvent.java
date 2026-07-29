package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.event.HandlerList;
import java.util.Objects;

/** Fired after a bot has been removed from the runtime registry. */
public final class BotDespawnEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotDespawnReason reason;

    public BotDespawnEvent(long sequence, BotSnapshot snapshot, BotEventSource source, BotDespawnReason reason) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), source, snapshot);
        this.reason = Objects.requireNonNull(reason, "reason");
    }
    public BotDespawnReason getReason() { return reason; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
