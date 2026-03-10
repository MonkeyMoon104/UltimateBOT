package com.monkey.mcbot.api.model;

/**
 * Origin of a spawned bot.
 */
public enum BotSource {
    /** Bot created by core commands/workflows. */
    CORE,
    /** Bot created through public API calls. */
    API
}
