package com.monkey.ultimatebot.api.extension.brain;

import java.util.Optional;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/** Immutable input delivered once per server tick to a full custom brain. */
public final class BrainTick {
    private final long sequence;
    private final boolean follow;
    private final boolean combat;
    private final @Nullable LivingEntity target;

    public BrainTick(long sequence, boolean follow, boolean combat, @Nullable LivingEntity target) {


        if (sequence < 0L) {
            throw new IllegalArgumentException("sequence cannot be negative");
        }
        this.sequence = sequence;
        this.follow = follow;
        this.combat = combat;
        this.target = target;
    }

    public long sequence() {
        return sequence;
    }
    public boolean follow() {
        return follow;
    }
    public boolean combat() {
        return combat;
    }
    public @Nullable LivingEntity target() {
        return target;
    }

    public Optional<LivingEntity> selectedTarget() {
        return Optional.ofNullable(target);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainTick)) {
            return false;
        }
        BrainTick other = (BrainTick) obj;
        return sequence == other.sequence && follow == other.follow && combat == other.combat && java.util.Objects.equals(target, other.target);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(sequence, follow, combat, target);
    }

    @Override
    public String toString() {
        return "BrainTick[sequence=" + sequence + ", follow=" + follow + ", combat=" + combat + ", target=" + target + "]";
    }
}
