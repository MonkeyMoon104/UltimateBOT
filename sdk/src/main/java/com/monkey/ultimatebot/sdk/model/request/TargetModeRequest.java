package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.BotTargetMode;
import java.util.Objects;

/** Request body used to change the categories attacked by a running bot. */
public final class TargetModeRequest {
    private final BotTargetMode targetMode;

    public TargetModeRequest(BotTargetMode targetMode) {


        Objects.requireNonNull(targetMode, "targetMode");
        this.targetMode = targetMode;
    }

    public BotTargetMode targetMode() {
        return targetMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TargetModeRequest)) {
            return false;
        }
        TargetModeRequest other = (TargetModeRequest) obj;
        return java.util.Objects.equals(targetMode, other.targetMode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(targetMode);
    }

    @Override
    public String toString() {
        return "TargetModeRequest[targetMode=" + targetMode + "]";
    }
}
