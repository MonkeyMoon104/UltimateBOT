package com.monkey.ultimatebot.sdk.event;

import com.monkey.ultimatebot.sdk.model.response.BotSnapshotResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Forward-compatible remote event envelope. Unknown types remain available through {@link #type()}. */
public final class BotEventEnvelope {
    private final long id;
    private final int schemaVersion;
    private final String type;
    private final UUID eventId;
    private final long sequence;
    private final Instant occurredAt;
    private final UUID ownerUUID;
    private final UUID botUUID;
    private final String source;
    private final BotSnapshotResponse snapshot;
    private final Map<String, Object> payload;

    public BotEventEnvelope(
            long id,
            int schemaVersion,
            String type,
            UUID eventId,
            long sequence,
            Instant occurredAt,
            UUID ownerUUID,
            UUID botUUID,
            String source,
            BotSnapshotResponse snapshot,
            Map<String, Object> payload) {

        payload = payload == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
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

    public BotSnapshotResponse snapshot() {
        return snapshot;
    }

    public Map<String, Object> payload() {
        return payload;
    }

    /** Returns one payload value when it exists and has the requested runtime type. */
    public <T> Optional<T> payloadValue(String name, Class<T> valueType) {
        Object value = payload.get(java.util.Objects.requireNonNull(name, "name"));
        return java.util.Objects.requireNonNull(valueType, "valueType").isInstance(value)
                ? Optional.of(valueType.cast(value))
                : Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotEventEnvelope)) {
            return false;
        }
        BotEventEnvelope other = (BotEventEnvelope) obj;
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
        return "BotEventEnvelope[id=" + id + ", schemaVersion=" + schemaVersion + ", type=" + type + ", eventId="
                + eventId + ", sequence=" + sequence + ", occurredAt=" + occurredAt + ", ownerUUID=" + ownerUUID
                + ", botUUID=" + botUUID + ", source=" + source + ", snapshot=" + snapshot + ", payload=" + payload
                + "]";
    }
}
