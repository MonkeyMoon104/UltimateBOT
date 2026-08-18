package com.monkey.ultimatebot.remote;

import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class RemoteBotEvent {
    private final long id;
    private final int schemaVersion;
    private final String type;
    private final UUID eventId;
    private final long sequence;
    private final Instant occurredAt;
    private final UUID ownerUUID;
    private final UUID botUUID;
    private final String source;
    private final @Nullable BotSnapshot snapshot;
    private final Map<String, Object> payload;

    public RemoteBotEvent(
            long id,
            int schemaVersion,
            String type,
            UUID eventId,
            long sequence,
            Instant occurredAt,
            UUID ownerUUID,
            UUID botUUID,
            String source,
            @Nullable BotSnapshot snapshot,
            Map<String, Object> payload) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.type = type;
        this.eventId = eventId;
        this.sequence = sequence;
        this.occurredAt = occurredAt;
        this.ownerUUID = ownerUUID;
        this.botUUID = botUUID;
        this.source = source;
        this.snapshot = snapshot;
        this.payload = payload;
    }

    public long id() {
        return id;
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public String type() {
        return type;
    }

    public UUID eventId() {
        return eventId;
    }

    public long sequence() {
        return sequence;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public UUID ownerUUID() {
        return ownerUUID;
    }

    public UUID botUUID() {
        return botUUID;
    }

    public String source() {
        return source;
    }

    public @Nullable BotSnapshot snapshot() {
        return snapshot;
    }

    public Map<String, Object> payload() {
        return payload;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RemoteBotEvent)) {
            return false;
        }
        RemoteBotEvent other = (RemoteBotEvent) obj;
        return id == other.id
                && schemaVersion == other.schemaVersion
                && java.util.Objects.equals(type, other.type)
                && java.util.Objects.equals(eventId, other.eventId)
                && sequence == other.sequence
                && java.util.Objects.equals(occurredAt, other.occurredAt)
                && java.util.Objects.equals(ownerUUID, other.ownerUUID)
                && java.util.Objects.equals(botUUID, other.botUUID)
                && java.util.Objects.equals(source, other.source)
                && java.util.Objects.equals(snapshot, other.snapshot)
                && java.util.Objects.equals(payload, other.payload);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id, schemaVersion, type, eventId, sequence, occurredAt, ownerUUID, botUUID, source, snapshot, payload);
    }

    @Override
    public String toString() {
        return "RemoteBotEvent[id=" + id + ", schemaVersion=" + schemaVersion + ", type=" + type + ", eventId="
                + eventId + ", sequence=" + sequence + ", occurredAt=" + occurredAt + ", ownerUUID=" + ownerUUID
                + ", botUUID=" + botUUID + ", source=" + source + ", snapshot=" + snapshot + ", payload=" + payload
                + "]";
    }
}
