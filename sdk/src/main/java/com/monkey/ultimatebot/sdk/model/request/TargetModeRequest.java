package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BotTargetMode;
import java.util.Objects;

/** Request body used to change the categories attacked by a running bot. */
public record TargetModeRequest(BotTargetMode targetMode) {
    public TargetModeRequest {
        Objects.requireNonNull(targetMode, "targetMode");
    }
}
