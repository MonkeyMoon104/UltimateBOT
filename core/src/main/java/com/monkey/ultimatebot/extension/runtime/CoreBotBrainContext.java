package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.brain.BotBrainContext;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.minecraft.world.entity.player.Player;

public final class CoreBotBrainContext implements BotBrainContext {
    private final Player bot;
    private final CoreBotControl control;
    private final CoreNativeBotAccess nativeAccess;
    private final RandomGenerator random;

    public CoreBotBrainContext(
            Player bot, CoreBotControl control, CoreNativeBotAccess nativeAccess, RandomGenerator random) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.control = Objects.requireNonNull(control, "control");
        this.nativeAccess = Objects.requireNonNull(nativeAccess, "nativeAccess");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public UUID botUUID() {
        return bot.getUUID();
    }

    @Override
    public org.bukkit.entity.Player bot() {
        return (org.bukkit.entity.Player) bot.getBukkitEntity();
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
    public RandomGenerator random() {
        return random;
    }
}
