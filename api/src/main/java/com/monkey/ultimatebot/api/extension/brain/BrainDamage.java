package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

/** Immutable damage signal delivered directly to one custom brain session. */
public final class BrainDamage {
    private final double amount;
    private final EntityDamageEvent.DamageCause cause;
    private final @Nullable Entity source;

    public BrainDamage(double amount, EntityDamageEvent.DamageCause cause, @Nullable Entity source) {

        if (!Double.isFinite(amount) || amount < 0.0D) {
            throw new IllegalArgumentException("amount must be finite and non-negative");
        }
        java.util.Objects.requireNonNull(cause, "cause");
        this.amount = amount;
        this.cause = cause;
        this.source = source;
    }

    public double amount() {
        return amount;
    }

    public EntityDamageEvent.DamageCause cause() {
        return cause;
    }

    public @Nullable Entity source() {
        return source;
    }

    public Optional<org.bukkit.entity.Entity> damageSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainDamage)) {
            return false;
        }
        BrainDamage other = (BrainDamage) obj;
        return Double.compare(amount, other.amount) == 0
                && java.util.Objects.equals(cause, other.cause)
                && java.util.Objects.equals(source, other.source);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amount, cause, source);
    }

    @Override
    public String toString() {
        return "BrainDamage[amount=" + amount + ", cause=" + cause + ", source=" + source + "]";
    }
}
