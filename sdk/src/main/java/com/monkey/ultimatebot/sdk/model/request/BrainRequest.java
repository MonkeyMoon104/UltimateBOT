package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.brain.BrainKey;
import java.util.Objects;

/** Remote request for assigning a registered custom brain. */
public final class BrainRequest {
    private final BrainKey brain;

    public BrainRequest(BrainKey brain) {

        Objects.requireNonNull(brain, "brain");
        this.brain = brain;
    }

    public BrainKey brain() {
        return brain;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainRequest)) {
            return false;
        }
        BrainRequest other = (BrainRequest) obj;
        return java.util.Objects.equals(brain, other.brain);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(brain);
    }

    @Override
    public String toString() {
        return "BrainRequest[brain=" + brain + "]";
    }
}
