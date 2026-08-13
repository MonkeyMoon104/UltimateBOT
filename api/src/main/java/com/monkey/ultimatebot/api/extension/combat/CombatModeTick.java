package com.monkey.ultimatebot.api.extension.combat;

import java.util.Objects;
import org.bukkit.entity.LivingEntity;

/** Immutable input delivered once per active combat tick. */
public final class CombatModeTick {
    private final long sequence;
    private final LivingEntity target;

    public CombatModeTick(long sequence, LivingEntity target) {


        if (sequence < 0L) {
            throw new IllegalArgumentException("sequence cannot be negative");
        }
        Objects.requireNonNull(target, "target");
        this.sequence = sequence;
        this.target = target;
    }

    public long sequence() {
        return sequence;
    }
    public LivingEntity target() {
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatModeTick)) {
            return false;
        }
        CombatModeTick other = (CombatModeTick) obj;
        return sequence == other.sequence && java.util.Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(sequence, target);
    }

    @Override
    public String toString() {
        return "CombatModeTick[sequence=" + sequence + ", target=" + target + "]";
    }
}
