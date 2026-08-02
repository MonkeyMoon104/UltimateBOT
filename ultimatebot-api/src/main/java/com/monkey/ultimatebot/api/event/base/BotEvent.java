package com.monkey.ultimatebot.api.event.base;

import com.monkey.ultimatebot.api.model.BotSnapshot;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;

/** Common immutable context shared by every UltimateBot event. */
public abstract class BotEvent extends Event {

    private final UUID eventId;
    private final long sequence;
    private final Instant occurredAt;
    private final UUID ownerUUID;
    private final UUID botUUID;
    private final BotEventSource source;
    private final @Nullable BotSnapshot botSnapshot;

    protected BotEvent(
            long sequence,
            UUID ownerUUID,
            UUID botUUID,
            @Nullable BotEventSource source,
            @Nullable BotSnapshot botSnapshot) {
        this.eventId = UUID.randomUUID();
        this.sequence = sequence;
        this.occurredAt = Instant.now();
        this.ownerUUID = Objects.requireNonNull(ownerUUID, "ownerUUID");
        this.botUUID = Objects.requireNonNull(botUUID, "botUUID");
        this.source = source == null ? BotEventSource.SYSTEM : source;
        this.botSnapshot = botSnapshot;
    }

    public final UUID getEventId() {
        return eventId;
    }

    public final long getSequence() {
        return sequence;
    }

    public final Instant getOccurredAt() {
        return occurredAt;
    }

    public final UUID getOwnerUUID() {
        return ownerUUID;
    }

    public final UUID getBotUUID() {
        return botUUID;
    }

    public final BotEventSource getSource() {
        return source;
    }

    public final @Nullable BotSnapshot getBotSnapshot() {
        return botSnapshot;
    }
}
