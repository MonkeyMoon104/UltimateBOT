package com.monkey.ultimatebot.bot;

public enum BotType {
    SINGLE(com.monkey.ultimatebot.common.model.BotMode.SINGLE),
    EVENT(com.monkey.ultimatebot.common.model.BotMode.EVENT),
    ALLY(com.monkey.ultimatebot.common.model.BotMode.ALLY),
    TEAM_ALLY(com.monkey.ultimatebot.common.model.BotMode.TEAM_ALLY);

    private final com.monkey.ultimatebot.common.model.BotMode common;

    BotType(com.monkey.ultimatebot.common.model.BotMode common) {
        this.common = common;
    }

    public com.monkey.ultimatebot.common.model.BotMode toCommon() {
        return common;
    }

    public static BotType fromCommon(com.monkey.ultimatebot.common.model.BotMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
