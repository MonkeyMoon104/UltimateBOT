package com.monkey.ultimatebot.sdk.model.request;

import java.util.Set;
import java.util.UUID;

/** Runtime request containing a complete UUID set replacement. */
public record UuidSetRequest(Set<UUID> uuids) {
    public UuidSetRequest {
        uuids = uuids == null ? Set.of() : Set.copyOf(uuids);
    }
}
