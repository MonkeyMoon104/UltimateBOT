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

    public HealStrategy(IHealthChecker healthChecker, IHealExecutor healExecutor, IHealActionManager actionManager) {
        this.healthChecker = healthChecker;
        this.healExecutor = healExecutor;
        this.actionManager = actionManager;
    }

    @Override
    public void executeHeal(Player bot) {
        actionManager.updateHealAction(bot);

        if (actionManager.isHealing()) {
            return;
        }

        if (shouldHeal(bot)) {
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