package com.monkey.mcbot.api.event;

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
