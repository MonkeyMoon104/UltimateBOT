package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.brain.BotBrainContext;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.UUID;

public final class CoreBotBrainContext implements BotBrainContext {
    private final ITrainingBot bot;
    private final CoreBotControl control;
    private final CoreNativeBotAccess nativeAccess;
    private final SplittableRandom random;

    public CoreBotBrainContext(
            ITrainingBot bot, CoreBotControl control, CoreNativeBotAccess nativeAccess, SplittableRandom random) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.control = Objects.requireNonNull(control, "control");
        this.nativeAccess = Objects.requireNonNull(nativeAccess, "nativeAccess");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public UUID botUUID() {
        return bot.getUniqueId();
    }

    @Override
    public org.bukkit.entity.Player bot() {
        return bot.asBukkitPlayer();
    }

    @Override
    public CoreBotControl control() {
        return control;
    }

    @Override
    public CoreNativeBotAccess nativeAccess() {
        return nativeAccess;
    }

    @Override
    public SplittableRandom random() {
        return random;
    }
}
