package com.monkey.mcbot.bot.ai.controllers.heal;

import com.monkey.mcbot.bot.ai.controllers.heal.helper.HealActionManager;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.HealExecutor;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.HealStrategy;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.HealthChecker;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.inter.IHealStrategy;
import com.monkey.mcbot.bot.ai.controllers.heal.helper.inter.IHealthChecker;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import net.minecraft.world.entity.player.Player;

public class BotHealController {

    private final Player bot;
    private final BotInventoryController inventoryController;

    private final IHealthChecker healthChecker;
    private final IHealExecutor healExecutor;
    private final HealActionManager actionManager;
    private final IHealStrategy healStrategy;

    public BotHealController(Player bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.inventoryController = inventoryController;

        this.healthChecker = new HealthChecker();
        this.healExecutor = new HealExecutor(inventoryController);
        this.actionManager = new HealActionManager(healExecutor);
        this.healStrategy = new HealStrategy(healthChecker, actionManager);
    }

    public void handleDamageReceived() {
        healStrategy.executeHeal(bot);
    }

    public void forceHeal() {
        if (inventoryController.hasInfiniteResources() && actionManager.canStartNewHeal()) {
            actionManager.startHealAction(bot);
        }
    }

    public boolean shouldHeal() {
        return healStrategy.shouldHeal(bot);
    }

    public boolean isHealing() {
        return actionManager.isHealing();
    }

    public void setHealthThreshold(float threshold) {
        healthChecker.setHealthThreshold(threshold);
    }

    public float getHealthThreshold() {
        return healthChecker.getHealthThreshold();
    }

    public float getCurrentHealth() {
        return healthChecker.getCurrentHealth(bot);
    }

    public boolean hasGoldenApple() {
        return inventoryController.hasInfiniteResources();
    }

    public void resetHealState() {
        actionManager.resetHealAction();
    }

    public void applyEffect() {
        actionManager.applyGoldenAppleEffectsManually(bot);
    }

    public void updateHealAction() {
        actionManager.updateHealAction(bot);
    }
}
