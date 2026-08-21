package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.combat.CombatMode;
import java.util.Objects;

/** Remote request for changing the active combat mode. */
public final class CombatModeRequest {
    private final CombatMode combatMode;

    public CombatModeRequest(CombatMode combatMode) {

        Objects.requireNonNull(combatMode, "combatMode");
        this.combatMode = combatMode;
    }

    public CombatMode combatMode() {
        return combatMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatModeRequest)) {
            return false;
        }
        CombatModeRequest other = (CombatModeRequest) obj;
        return java.util.Objects.equals(combatMode, other.combatMode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(combatMode);
    }

    @Override
    public String toString() {
        return "CombatModeRequest[combatMode=" + combatMode + "]";
    }
}
