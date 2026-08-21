package com.monkey.ultimatebot.bot;

public enum BotCreationSource {
    CORE(com.monkey.ultimatebot.common.model.bot.BotSource.CORE),
    API(com.monkey.ultimatebot.common.model.bot.BotSource.API);

    private final com.monkey.ultimatebot.common.model.bot.BotSource common;

    BotCreationSource(com.monkey.ultimatebot.common.model.bot.BotSource common) {
        this.common = common;
    }

    public com.monkey.ultimatebot.common.model.bot.BotSource toCommon() {
        return common;
    }

    public static BotCreationSource fromCommon(com.monkey.ultimatebot.common.model.bot.BotSource source) {
        return valueOf(java.util.Objects.requireNonNull(source, "source").name());
    }
}
