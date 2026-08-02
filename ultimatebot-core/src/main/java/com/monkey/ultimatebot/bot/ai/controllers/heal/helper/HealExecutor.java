package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class HealExecutor implements IHealExecutor {

    private final BotInventoryController inventoryController;

    public HealExecutor(BotInventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    @Override
    public void consumeGoldenApple(Player bot) {
        if (!inventoryController.hasInfiniteResources()) return;

        inventoryController.switchToGoldenApple();

        try {
            inventoryController.startUsingItem(InteractionHand.MAIN_HAND);
        } catch (IllegalStateException exception) {
            bot.swing(InteractionHand.MAIN_HAND);
        }
    }
}
