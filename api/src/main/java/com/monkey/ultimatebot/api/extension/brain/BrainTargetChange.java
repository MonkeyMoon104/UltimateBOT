package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Immutable target transition delivered without going through the global event bus. */
public record BrainTargetChange(
        @Nullable LivingEntity previous, @Nullable LivingEntity current) {
    public Optional<LivingEntity> previousTarget() {
        return Optional.ofNullable(previous);
    }

    public Optional<LivingEntity> currentTarget() {
        return Optional.ofNullable(current);
    }
}
