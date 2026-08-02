package com.monkey.ultimatebot.bot;

public enum BotCreationSource {
    CORE(com.monkey.ultimatebot.common.model.BotSource.CORE),
    API(com.monkey.ultimatebot.common.model.BotSource.API);

    private final com.monkey.ultimatebot.common.model.BotSource common;

    BotCreationSource(com.monkey.ultimatebot.common.model.BotSource common) {
        this.common = common;
    }

    public com.monkey.ultimatebot.common.model.BotSource toCommon() {
        return common;
    }

    public static BotCreationSource fromCommon(com.monkey.ultimatebot.common.model.BotSource source) {
        return valueOf(java.util.Objects.requireNonNull(source, "source").name());
    }
}
