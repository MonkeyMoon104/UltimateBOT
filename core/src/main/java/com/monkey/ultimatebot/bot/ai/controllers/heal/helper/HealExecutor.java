package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;

public class HealExecutor implements IHealExecutor {

    private final BotInventoryController inventoryController;

    public HealExecutor(BotInventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    @Override
    public void consumeGoldenApple(ITrainingBot bot) {
        if (!inventoryController.hasInfiniteResources()) return;

        inventoryController.switchToGoldenApple();

        try {
            inventoryController.startUsingMainHand();
        } catch (IllegalStateException exception) {
            bot.swingMainHand();
        }
    }
}
