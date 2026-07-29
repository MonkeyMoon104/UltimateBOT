package com.monkey.mcbot.api.event;

/** Reason why a managed bot left the runtime registry. */
public enum BotDespawnReason {
    MANUAL,
    REPLACED,
    OWNER_QUIT,
    OWNER_DEATH,
    WORLD_CHANGE,
    BOT_DEATH,
    API_REQUEST,
    PLUGIN_DISABLE,
    UNKNOWN
}
