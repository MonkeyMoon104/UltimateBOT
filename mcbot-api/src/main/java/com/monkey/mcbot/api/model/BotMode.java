package com.monkey.mcbot.api.model;

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

    public com.monkey.mcbot.common.model.BotMode toCommon() {
        return com.monkey.mcbot.common.model.BotMode.valueOf(name());
    }

    public static BotMode fromCommon(com.monkey.mcbot.common.model.BotMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
