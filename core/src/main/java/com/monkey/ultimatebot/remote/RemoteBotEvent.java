package com.monkey.ultimatebot.remote;

import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Stable, Bukkit-free payload written to the remote event stream. */
public record RemoteBotEvent(
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
        Map<String, Object> payload) {}
