package com.monkey.ultimatebot.sdk.model;

/** Target categories supported by the remote bot API. */
public enum SdkBotTargetMode {
    PLAYERS(com.monkey.ultimatebot.common.model.BotTargetMode.PLAYERS),
    MOBS(com.monkey.ultimatebot.common.model.BotTargetMode.MOBS),
    PLAYERS_AND_MOBS(com.monkey.ultimatebot.common.model.BotTargetMode.PLAYERS_AND_MOBS);

    private final com.monkey.ultimatebot.common.model.BotTargetMode common;

    SdkBotTargetMode(com.monkey.ultimatebot.common.model.BotTargetMode common) {
        this.common = common;
    }

    public com.monkey.ultimatebot.common.model.BotTargetMode toCommon() {
        return common;
    }

    public static SdkBotTargetMode fromCommon(com.monkey.ultimatebot.common.model.BotTargetMode mode) {
        return valueOf(java.util.Objects.requireNonNull(mode, "mode").name());
    }
}
