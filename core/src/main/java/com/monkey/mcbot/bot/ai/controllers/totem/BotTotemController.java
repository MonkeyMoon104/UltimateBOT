package com.monkey.mcbot.bot.ai.controllers.totem;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.TrainingBot;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.*;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.interf.*;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.*;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.interf.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BotTotemController {
    private final Player bot;

    private final ITotemInventoryManager inventoryManager;
    private final ITotemStateAnalyzer stateAnalyzer;
    private final ITotemStrategyHandler strategyHandler;
    private final ITotemNotificationManager notificationManager;
    private final ITotemUsageTracker usageTracker;

    public BotTotemController(Player bot, SandboxTraining plugin) {
        this.bot = bot;

        this.inventoryManager = new TotemInventoryManager(bot);
        this.stateAnalyzer = new TotemStateAnalyzer(inventoryManager);
        this.strategyHandler = new TotemStrategyHandler(inventoryManager);
        this.notificationManager = new TotemNotificationManager(plugin);
        this.usageTracker = new TotemUsageTracker();
    }

    public void manageTotem() {
        if (!(bot instanceof TrainingBot trainingBot)) return;

        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.getItemBySlot(EquipmentSlot.MAINHAND);

        int totemCount = trainingBot.getTotemCount();
        boolean isCombat = trainingBot.isCombat();

        TotemState totemState = stateAnalyzer.getTotemState(totemCount);
        TotemEquipmentState equipmentState = stateAnalyzer.analyzeCurrentEquipment(offhand, mainhand);

        switch (totemState) {
            case UNLIMITED -> {
                strategyHandler.handleUnlimitedTotems(equipmentState, isCombat);
                notificationManager.resetWarning();
            }
            case NONE -> {
                strategyHandler.handleNoTotems(equipmentState);
                handleNoTotemsWarning(trainingBot);
            }
            case ONE -> {
                strategyHandler.handleOneTotem(equipmentState, isCombat);
                notificationManager.resetWarning();
            }
            case MULTIPLE -> {
                strategyHandler.handleMultipleTotems(totemCount, equipmentState, isCombat);
                notificationManager.resetWarning();
            }
        }
    }

    public void onTotemUsed() {
        if (bot instanceof TrainingBot trainingBot) {
            usageTracker.onTotemUsed(trainingBot);
        }
    }

    public int getEquippedTotemCount() {
        return inventoryManager.getEquippedTotemCount();
    }

    public void forceEquipTotems(int count) {
        inventoryManager.forceEquipTotems(count);
    }

    private void handleNoTotemsWarning(TrainingBot trainingBot) {
        if (!notificationManager.hasWarnedOutOfTotems()) {
            notificationManager.sendTotemWarning(trainingBot);
            notificationManager.setWarnedOutOfTotems(true);
        }
    }

    public ITotemInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ITotemStateAnalyzer getStateAnalyzer() {
        return stateAnalyzer;
    }

    public ITotemStrategyHandler getStrategyHandler() {
        return strategyHandler;
    }

    public ITotemNotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ITotemUsageTracker getUsageTracker() {
        return usageTracker;
    }
}