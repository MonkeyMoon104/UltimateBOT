package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Immutable target transition delivered without going through the global event bus. */
public final class BrainTargetChange {
    private final @Nullable LivingEntity previous;
    private final @Nullable LivingEntity current;

    public BrainTargetChange(@Nullable LivingEntity previous, @Nullable LivingEntity current) {
        this.previous = previous;
        this.current = current;
    }

    public @Nullable LivingEntity previous() {
        return previous;
    }

    public @Nullable LivingEntity current() {
        return current;
    }

    public Optional<LivingEntity> previousTarget() {
        return Optional.ofNullable(previous);
    }

    public Optional<LivingEntity> currentTarget() {
        return Optional.ofNullable(current);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainTargetChange)) {
            return false;
        }
        BrainTargetChange other = (BrainTargetChange) obj;
        return java.util.Objects.equals(previous, other.previous) && java.util.Objects.equals(current, other.current);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(previous, current);
    }

    @Override
    public String toString() {
        return "BrainTargetChange[previous=" + previous + ", current=" + current + "]";
    }
}
