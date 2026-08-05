package com.monkey.ultimatebot.api.extension.brain;

/** Per-bot custom AI instance with deterministic lifecycle ownership. */
public interface BotBrainSession extends AutoCloseable {
    default void onStart() {}

    void tick(BrainTick tick);

    default void onTargetChanged(BrainTargetChange change) {}

    default void onDamage(BrainDamage damage) {}

    @Override
    default void close() {}
}
