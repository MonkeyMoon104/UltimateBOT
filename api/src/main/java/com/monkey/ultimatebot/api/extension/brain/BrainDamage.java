package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/** Immutable damage signal delivered directly to one custom brain session. */
public record BrainDamage(
        double amount,
        EntityDamageEvent.DamageCause cause,
        @Nullable Entity source) {
    public BrainDamage {
        if (!Double.isFinite(amount) || amount < 0.0D) {
            throw new IllegalArgumentException("amount must be finite and non-negative");
        }
        java.util.Objects.requireNonNull(cause, "cause");
    }

    public Optional<org.bukkit.entity.Entity> damageSource() {
        return Optional.ofNullable(source);
    }
}
