package com.monkey.ultimatebot.api.extension.combat;

/** Factory and metadata provider for one custom combat mode. */
public interface CombatModeProvider {
    CombatModeDescriptor descriptor();

    /**
     * Creates one isolated session per bot.
     *
     * <p>Brain-backed modes normally keep this default because the linked full brain owns the tick.</p>
     */
    default CombatModeSession create(CombatModeRuntime runtime) {
        java.util.Objects.requireNonNull(runtime, "runtime");
        return CombatModeSession.passive();
    }
}
