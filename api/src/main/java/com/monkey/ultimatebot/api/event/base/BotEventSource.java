package com.monkey.ultimatebot.api.event.base;

/** Identifies the component that caused a bot event. */
public enum BotEventSource {
    GUI,
    COMMAND,
    API,
    REMOTE_API,
    BOT_AI,
    BUKKIT,
    PLUGIN,
    SYSTEM
}
