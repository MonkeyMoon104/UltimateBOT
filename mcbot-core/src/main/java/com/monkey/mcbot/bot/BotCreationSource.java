package com.monkey.mcbot.bot;

public enum BotCreationSource {
    CORE(com.monkey.mcbot.common.model.BotSource.CORE),
    API(com.monkey.mcbot.common.model.BotSource.API);

    private final com.monkey.mcbot.common.model.BotSource common;

    BotCreationSource(com.monkey.mcbot.common.model.BotSource common) {
        this.common = common;
    }

    public com.monkey.mcbot.common.model.BotSource toCommon() {
        return common;
    }

    public static BotCreationSource fromCommon(com.monkey.mcbot.common.model.BotSource source) {
        return valueOf(java.util.Objects.requireNonNull(source, "source").name());
    }
}
