package com.monkey.ultimatebot.api.model;

/**
 * Supported spawn modes exposed by the public API.
 */
public enum BotMode {
    /** Single-owner bot; target is always forced to the owner. */
    SINGLE,
    /** Global event bot mode (mutually exclusive with other active modes). */
    EVENT,
    /** Single-owner ally mode. */
    ALLY,
    /** Multi-owner ally mode with shared ownership. */
    TEAM_ALLY;

    public com.monkey.ultimatebot.common.model.BotMode toCommon() {
        return com.monkey.ultimatebot.common.model.BotMode.valueOf(name());
    }

    public static BotMode fromCommon(com.monkey.ultimatebot.common.model.BotMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
