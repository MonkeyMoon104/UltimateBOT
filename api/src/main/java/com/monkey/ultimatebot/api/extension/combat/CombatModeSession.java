package com.monkey.ultimatebot.api.extension.combat;

/** Per-bot stateful combat strategy owned and closed by UltimateBot. */
public interface CombatModeSession extends AutoCloseable {
    /** Returns a stateless session suitable for modes controlled by a full custom brain. */
    static CombatModeSession passive() {
        return new CombatModeSession() {
            @Override
            public void tick(CombatModeTick tick) {
                java.util.Objects.requireNonNull(tick, "tick");
            }

            @Override
            public boolean controlsNavigation() {
                return false;
            }
        };
    }

    default void onEnter() {}

    void tick(CombatModeTick tick);

    default boolean controlsNavigation() {
        return true;
    }

    default void onExit() {}

    @Override
    default void close() {}
}
