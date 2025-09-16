package it.coralmc.sandbox.bot.ai.controllers.heal.helper;

import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealActionManager;
import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealStrategy;
import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealthChecker;
import net.minecraft.world.entity.player.Player;

public class HealStrategy implements IHealStrategy {

    private final IHealthChecker healthChecker;
    private final IHealExecutor healExecutor;
    private final IHealActionManager actionManager;

    private long lastExecuteHealCall = 0;
    private int consecutiveHealChecks = 0;
    private static final long EXECUTE_HEAL_INTERVAL = 50;
    private static final int MAX_CONSECUTIVE_CHECKS = 10;

    public HealStrategy(IHealthChecker healthChecker, IHealExecutor healExecutor, IHealActionManager actionManager) {
        this.healthChecker = healthChecker;
        this.healExecutor = healExecutor;
        this.actionManager = actionManager;
    }

    @Override
    public void executeHeal(Player bot) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastExecuteHealCall < EXECUTE_HEAL_INTERVAL) {
            consecutiveHealChecks++;

            if (consecutiveHealChecks > MAX_CONSECUTIVE_CHECKS) {
                if (actionManager instanceof HealActionManager) {
                    ((HealActionManager) actionManager).forceReset();
                }
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
    public boolean shouldHeal(Player bot) {
        if (actionManager.isHealing()) {
            return false;
        }

        if (!actionManager.canStartNewHeal()) {
            return false;
        }

        return healthChecker.needsHealing(bot);
    }
}