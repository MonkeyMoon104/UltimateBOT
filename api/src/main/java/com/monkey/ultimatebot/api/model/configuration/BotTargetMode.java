package com.monkey.ultimatebot.api.model.configuration;

/**
 * Selects which kinds of living targets a bot may attack.
 */
public enum BotTargetMode {
    PLAYERS(com.monkey.ultimatebot.common.model.BotTargetMode.PLAYERS),
    MOBS(com.monkey.ultimatebot.common.model.BotTargetMode.MOBS),
    PLAYERS_AND_MOBS(com.monkey.ultimatebot.common.model.BotTargetMode.PLAYERS_AND_MOBS);

    private final com.monkey.ultimatebot.common.model.BotTargetMode common;

    BotTargetMode(com.monkey.ultimatebot.common.model.BotTargetMode common) {
        this.common = common;
    }

    public boolean allowsPlayers() {
        return common.allowsPlayers();
    }

    public boolean allowsMobs() {
        return common.allowsMobs();
    }

    public BotTargetMode next() {
        return fromCommon(common.next());
    }

    public com.monkey.ultimatebot.common.model.BotTargetMode toCommon() {
        return common;
    }

    public static BotTargetMode fromCommon(com.monkey.ultimatebot.common.model.BotTargetMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
