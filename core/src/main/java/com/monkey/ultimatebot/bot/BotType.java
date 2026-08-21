package com.monkey.ultimatebot.bot;

public enum BotType {
    SINGLE(com.monkey.ultimatebot.common.model.bot.BotMode.SINGLE),
    EVENT(com.monkey.ultimatebot.common.model.bot.BotMode.EVENT),
    ALLY(com.monkey.ultimatebot.common.model.bot.BotMode.ALLY),
    TEAM_ALLY(com.monkey.ultimatebot.common.model.bot.BotMode.TEAM_ALLY);

    private final com.monkey.ultimatebot.common.model.bot.BotMode common;

    BotType(com.monkey.ultimatebot.common.model.bot.BotMode common) {
        this.common = common;
    }

    public com.monkey.ultimatebot.common.model.bot.BotMode toCommon() {
        return common;
    }

    public static BotType fromCommon(com.monkey.ultimatebot.common.model.bot.BotMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
