package com.monkey.mcbot.api.model;

/**
 * Origin of a spawned bot.
 */
public enum BotSource {
    /** Bot created by core commands/workflows. */
    CORE,
    /** Bot created through public API calls. */
    API;

    public com.monkey.mcbot.common.model.BotSource toCommon() {
        return com.monkey.mcbot.common.model.BotSource.valueOf(name());
    }

    public static BotSource fromCommon(com.monkey.mcbot.common.model.BotSource source) {
        return valueOf(java.util.Objects.requireNonNull(source, "source").name());
    }
}
