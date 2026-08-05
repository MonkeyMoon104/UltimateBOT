package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Immutable input delivered once per server tick to a full custom brain. */
public record BrainTick(
        long sequence,
        boolean follow,
        boolean combat,
        @Nullable LivingEntity target) {
    public BrainTick {
        if (sequence < 0L) {
            throw new IllegalArgumentException("sequence cannot be negative");
        }
    }

    public Optional<LivingEntity> selectedTarget() {
        return Optional.ofNullable(target);
    }
}
