package com.monkey.ultimatebot.common.model;

/** Platform-independent categories of living entities a bot may target. */
public enum BotTargetMode {
    PLAYERS(true, false),
    MOBS(false, true),
    PLAYERS_AND_MOBS(true, true);

    private final boolean players;
    private final boolean mobs;

    BotTargetMode(boolean players, boolean mobs) {
        this.players = players;
        this.mobs = mobs;
    }

    public boolean allowsPlayers() {
        return players;
    }

    public boolean allowsMobs() {
        return mobs;
    }

    public BotTargetMode next() {
        return switch (this) {
            case PLAYERS -> MOBS;
            case MOBS -> PLAYERS_AND_MOBS;
            case PLAYERS_AND_MOBS -> PLAYERS;
        };
    }
}
