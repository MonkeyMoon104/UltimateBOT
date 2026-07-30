package com.monkey.mcbot.bot;

public enum BotType {
    SINGLE(com.monkey.mcbot.common.model.BotMode.SINGLE),
    EVENT(com.monkey.mcbot.common.model.BotMode.EVENT),
    ALLY(com.monkey.mcbot.common.model.BotMode.ALLY),
    TEAM_ALLY(com.monkey.mcbot.common.model.BotMode.TEAM_ALLY);

    private final com.monkey.mcbot.common.model.BotMode common;

    BotType(com.monkey.mcbot.common.model.BotMode common) {
        this.common = common;
    }

    public com.monkey.mcbot.common.model.BotMode toCommon() {
        return common;
    }

    public static BotType fromCommon(com.monkey.mcbot.common.model.BotMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
