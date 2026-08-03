package com.monkey.ultimatebot.api.model.identity;

/**
 * Origin of a spawned bot.
 */
public enum BotSource {
    /** Bot created by core commands/workflows. */
    CORE,
    /** Bot created through public API calls. */
    API;

    public com.monkey.ultimatebot.common.model.BotSource toCommon() {
        return com.monkey.ultimatebot.common.model.BotSource.valueOf(name());
    }

    public static BotSource fromCommon(com.monkey.ultimatebot.common.model.BotSource source) {
        return valueOf(java.util.Objects.requireNonNull(source, "source").name());
    }
}
