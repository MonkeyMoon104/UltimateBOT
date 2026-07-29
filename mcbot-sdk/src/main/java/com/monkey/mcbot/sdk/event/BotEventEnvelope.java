package com.monkey.mcbot.sdk.event;

import com.monkey.mcbot.sdk.model.BotSnapshotResponse;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.LinkedHashMap;

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
        BotSnapshotResponse snapshot,
        Map<String, Object> payload
) {
    public BotEventEnvelope {
        payload = payload == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }
}
