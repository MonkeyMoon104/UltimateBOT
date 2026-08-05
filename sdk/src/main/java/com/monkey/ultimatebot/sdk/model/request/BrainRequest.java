package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BrainKey;
import java.util.Objects;

/** Remote request for assigning a registered custom brain. */
public record BrainRequest(BrainKey brain) {
    public BrainRequest {
        Objects.requireNonNull(brain, "brain");
    }
}
