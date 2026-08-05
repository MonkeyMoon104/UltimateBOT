package com.monkey.ultimatebot.api.extension.combat;

import java.util.Objects;
import org.bukkit.entity.LivingEntity;

/** Immutable input delivered once per active combat tick. */
public record CombatModeTick(long sequence, LivingEntity target) {
    public CombatModeTick {
        if (sequence < 0L) {
            throw new IllegalArgumentException("sequence cannot be negative");
        }
        Objects.requireNonNull(target, "target");
    }
}
