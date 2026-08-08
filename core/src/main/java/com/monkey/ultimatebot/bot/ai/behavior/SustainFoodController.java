package com.monkey.ultimatebot.bot.ai.behavior;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SustainFoodController {

    private static final int CONSUMPTION_DURATION_TICKS = 32;
    private static final int TRIGGER_FOOD_LEVEL = 6;

    private final ITrainingBot bot;
    private final BotOptions options;
    private final BotInventoryController inventoryController;

    private boolean foodSlotActive;
    private boolean eating;
    private int consumptionTicks;

    public SustainFoodController(ITrainingBot bot, BotOptions options, BotInventoryController inventoryController) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.options = Objects.requireNonNull(options, "options");
        this.inventoryController = Objects.requireNonNull(inventoryController, "inventoryController");
    }

    public boolean tick() {
        synchronizeFoodSlot();
        if (options.isHealing()) {
            resetEatingState();
            return false;
        }

        if (bot.isUsingItem() && bot.activeItemStack().getType() == Material.GOLDEN_APPLE) {
            inventoryController.releaseUsingItem();
            resetEatingState();
            inventoryController.switchToSword();
            return false;
        }

        if (eating) {
            consumptionTicks++;
            if (consumptionTicks >= CONSUMPTION_DURATION_TICKS || !bot.isUsingItem()) {
                applyFood();
                resetEatingState();
                inventoryController.switchToSword();
                return false;
            }
            return true;
        }

        if (bot.asBukkitPlayer().getFoodLevel() > TRIGGER_FOOD_LEVEL) {
            return false;
        }

        inventoryController.switchToSlot(BotInventoryController.GOLDEN_APPLE_SLOT);
        try {
            inventoryController.startUsingMainHand();
        } catch (IllegalStateException ignored) {
            applyFood();
            inventoryController.switchToSword();
            return false;
        }
        eating = true;
        consumptionTicks = 0;
        return true;
    }

    public void close() {
        inventoryController.releaseUsingItem();
        resetEatingState();
    }

    private void synchronizeFoodSlot() {
        if (options.isHealing()) {
            if (foodSlotActive) {
                inventoryController.setItem(
                        BotInventoryController.GOLDEN_APPLE_SLOT, new ItemStack(Material.GOLDEN_APPLE, 64));
                foodSlotActive = false;
            }
            return;
        }
        if (!foodSlotActive) {
            inventoryController.setItem(BotInventoryController.GOLDEN_APPLE_SLOT, new ItemStack(Material.COOKED_BEEF, 64));
            foodSlotActive = true;
        }
    }

    private void applyFood() {
        if (bot.asBukkitPlayer().getFoodLevel() < 20) {
            bot.asBukkitPlayer().setFoodLevel(Math.min(20, bot.asBukkitPlayer().getFoodLevel() + 8));
            bot.asBukkitPlayer().setSaturation(Math.min(20.0F, bot.asBukkitPlayer().getSaturation() + 0.8F));
        }
        inventoryController.releaseUsingItem();
    }

    private void resetEatingState() {
        eating = false;
        consumptionTicks = 0;
    }
}
