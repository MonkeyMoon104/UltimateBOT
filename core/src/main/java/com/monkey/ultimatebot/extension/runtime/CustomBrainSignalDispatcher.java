package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.brain.BotBrainSession;
import com.monkey.ultimatebot.api.extension.brain.BrainDamage;
import com.monkey.ultimatebot.api.extension.brain.BrainTargetChange;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

final class CustomBrainSignalDispatcher {
    private final Supplier<@Nullable BotBrainSession> session;
    private final BiConsumer<Throwable, String> failure;

    CustomBrainSignalDispatcher(Supplier<@Nullable BotBrainSession> session, BiConsumer<Throwable, String> failure) {
        this.session = Objects.requireNonNull(session, "session");
        this.failure = Objects.requireNonNull(failure, "failure");
    }

    void targetChanged(@Nullable LivingEntity previous, @Nullable LivingEntity current) {
        BotBrainSession active = session.get();
        if (active != null) {
            invoke(() -> active.onTargetChanged(new BrainTargetChange(previous, current)), "target change");
        }
    }

    void damaged(EntityDamageEvent event) {
        EntityDamageEvent checked = Objects.requireNonNull(event, "event");
        BotBrainSession active = session.get();
        if (active == null) {
            return;
        }
        org.bukkit.entity.Entity source =
                checked instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;
        invoke(() -> active.onDamage(new BrainDamage(checked.getFinalDamage(), checked.getCause(), source)), "damage");
    }

    private void invoke(Runnable signal, String label) {
        try {
            signal.run();
        } catch (RuntimeException | LinkageError error) {
            failure.accept(error, label);
        }
    }
}
