package com.monkey.ultimatebot.sdk.model;

import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;

/** Remote request for changing the active combat mode. */
public record CombatModeRequest(CombatMode combatMode) {
    public CombatModeRequest {
        Objects.requireNonNull(combatMode, "combatMode");
    }
}
