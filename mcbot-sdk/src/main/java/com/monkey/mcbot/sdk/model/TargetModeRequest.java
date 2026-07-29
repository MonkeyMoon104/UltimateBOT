package com.monkey.mcbot.sdk.model;

import java.util.Objects;

/** Request body used to change the categories attacked by a running bot. */
public record TargetModeRequest(SdkBotTargetMode targetMode) {
    public TargetModeRequest {
        Objects.requireNonNull(targetMode, "targetMode");
    }
}
