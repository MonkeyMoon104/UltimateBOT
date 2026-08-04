package com.monkey.ultimatebot.sdk.event;

import com.monkey.ultimatebot.sdk.model.response.BotSnapshotResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Forward-compatible remote event envelope. Unknown types remain available through {@link #type()}. */
public record BotEventEnvelope(
        long id,
        int schemaVersion,
        String type,
        UUID eventId,
        long sequence,
        Instant occurredAt,
        UUID ownerUUID,
        UUID botUUID,
        String source,
        @Nullable BotSnapshotResponse snapshot,
        Map<String, Object> payload) {
    public BotEventEnvelope {
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }

    /** Returns one payload value when it exists and has the requested runtime type. */
    public <T> Optional<T> payloadValue(String name, Class<T> valueType) {
        Object value = payload.get(java.util.Objects.requireNonNull(name, "name"));
        return java.util.Objects.requireNonNull(valueType, "valueType").isInstance(value)
                ? Optional.of(valueType.cast(value))
                : Optional.empty();
    }
}
