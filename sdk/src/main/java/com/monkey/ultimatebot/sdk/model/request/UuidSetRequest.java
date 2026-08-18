package com.monkey.ultimatebot.sdk.model.request;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/** Runtime request containing a complete UUID set replacement. */
public final class UuidSetRequest {
    private final Set<UUID> uuids;

    public UuidSetRequest(Set<UUID> uuids) {

        uuids = uuids == null
                ? Collections.emptySet()
                : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(uuids);
        this.uuids = uuids;
    }

    public Set<UUID> uuids() {
        return uuids;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UuidSetRequest)) {
            return false;
        }
        UuidSetRequest other = (UuidSetRequest) obj;
        return java.util.Objects.equals(uuids, other.uuids);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(uuids);
    }

    @Override
    public String toString() {
        return "UuidSetRequest[uuids=" + uuids + "]";
    }
}
