package com.monkey.ultimatebot.access.update;

import org.bukkit.entity.Player;

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
            return Class.forName("com.monkey.ultimatebot.access.update.ModernUpdateNotifyOps")
                    .asSubclass(UpdateNotifyOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyUpdateNotifyOps();
        }
    }
}
