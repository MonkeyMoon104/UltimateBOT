package com.monkey.ultimatebot.common.model.bot;

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
        switch (this) {
            case PLAYERS:
                return MOBS;
            case MOBS:
                return PLAYERS_AND_MOBS;
            case PLAYERS_AND_MOBS:
                return PLAYERS;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
