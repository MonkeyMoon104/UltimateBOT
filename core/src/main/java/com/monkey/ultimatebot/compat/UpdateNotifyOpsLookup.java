package com.monkey.ultimatebot.compat;

import org.bukkit.entity.Player;

/**
 * Selects Adventure ({@code Player#sendMessage(Component)}) or Bungee chat. Modern is loaded only
 * after probing so Paper 1.16.4 never links {@code net.kyori.adventure}.
 */
final class UpdateNotifyOpsLookup {
    private static final UpdateNotifyOps INSTANCE = resolve();

    private UpdateNotifyOpsLookup() {}

    static UpdateNotifyOps get() {
        return INSTANCE;
    }

    private static UpdateNotifyOps resolve() {
        try {
            Class<?> component = Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.text.format.TextColor");
            Player.class.getMethod("sendMessage", component);
            return Class.forName("com.monkey.ultimatebot.compat.ModernUpdateNotifyOps")
                    .asSubclass(UpdateNotifyOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyUpdateNotifyOps();
        }
    }
}
