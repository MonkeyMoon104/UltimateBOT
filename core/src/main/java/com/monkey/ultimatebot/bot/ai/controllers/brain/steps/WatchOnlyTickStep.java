package com.monkey.ultimatebot.bot.ai.controllers.brain.steps;

import com.monkey.ultimatebot.bot.ai.BotAI;
import org.bukkit.entity.LivingEntity;

public final class WatchOnlyTickStep {

    private final BotAI botAI;

    public WatchOnlyTickStep(BotAI botAI) {
        this.botAI = botAI;
    }

    public void execute(LivingEntity selectedTarget) {
        botAI.getMovementController().clearPath();
        botAI.getRotationController().updateRotation(selectedTarget);
    }
}
