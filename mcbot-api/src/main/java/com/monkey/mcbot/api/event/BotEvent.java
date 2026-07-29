package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.event.Event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Common immutable context shared by every MinecraftBot event. */
public abstract class BotEvent extends Event {

    private final UUID eventId;
    private final long sequence;
    private final Instant occurredAt;
    private final UUID ownerUUID;
    private final UUID botUUID;
    private final BotEventSource source;
    private final BotSnapshot botSnapshot;

    protected BotEvent(long sequence, UUID ownerUUID, UUID botUUID,
                       BotEventSource source, BotSnapshot botSnapshot) {
        this.eventId = UUID.randomUUID();
        this.sequence = sequence;
        this.occurredAt = Instant.now();
        this.ownerUUID = Objects.requireNonNull(ownerUUID, "ownerUUID");
        this.botUUID = Objects.requireNonNull(botUUID, "botUUID");
        this.source = source == null ? BotEventSource.SYSTEM : source;
        this.botSnapshot = botSnapshot;
    }

    public final UUID getEventId() { return eventId; }
    public final long getSequence() { return sequence; }
    public final Instant getOccurredAt() { return occurredAt; }
    public final UUID getOwnerUUID() { return ownerUUID; }
    public final UUID getBotUUID() { return botUUID; }
    public final BotEventSource getSource() { return source; }
    public final BotSnapshot getBotSnapshot() { return botSnapshot; }
}
