package it.coralmc.sandbox.bot.ai.controllers.heal.helper;

import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
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
            bot.startUsingItem(InteractionHand.MAIN_HAND);
        } catch (Exception e) {
            bot.swing(InteractionHand.MAIN_HAND);
        }
    }
}