package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealthChecker;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public class HealStrategy implements IHealStrategy {

    private final IHealthChecker healthChecker;
    private final HealActionManager actionManager;

    private long lastExecuteHealCall = 0;
    private int consecutiveHealChecks = 0;
    private static final long EXECUTE_HEAL_INTERVAL = 50;
    private static final int MAX_CONSECUTIVE_CHECKS = 10;

    public HealStrategy(IHealthChecker healthChecker, HealActionManager actionManager) {
        this.healthChecker = healthChecker;
        this.actionManager = actionManager;
    }

    @Override
    public void executeHeal(ITrainingBot bot) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastExecuteHealCall < EXECUTE_HEAL_INTERVAL) {
            consecutiveHealChecks++;

            if (consecutiveHealChecks > MAX_CONSECUTIVE_CHECKS) {
                actionManager.forceReset();
                consecutiveHealChecks = 0;
            }
            return;
        }

        lastExecuteHealCall = currentTime;
        consecutiveHealChecks = 0;

        actionManager.updateHealAction(bot);

        if (actionManager.isHealing()) {
            return;
        }

        if (shouldHeal(bot) && actionManager.canStartNewHeal()) {
            actionManager.startHealAction(bot);
        }
    }

    @Override
    public boolean shouldHeal(ITrainingBot bot) {
        if (actionManager.isHealing()) {
            return false;
        }

        if (!actionManager.canStartNewHeal()) {
            return false;
        }

        return healthChecker.needsHealing(bot);
    }
}
