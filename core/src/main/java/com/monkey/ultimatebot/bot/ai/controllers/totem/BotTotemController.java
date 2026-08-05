package com.monkey.ultimatebot.bot.ai.controllers.totem;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.EquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemEquipmentState;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemNotificationManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemState;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemStateAnalyzer;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemStrategyHandler;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.TotemUsageTracker;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemNotificationManager;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStateAnalyzer;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemStrategyHandler;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemUsageTracker;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
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

    public BotTotemController(Player bot, UltimateBot plugin) {
        this.bot = Objects.requireNonNull(bot, "bot");

        this.inventoryManager = new TotemInventoryManager(bot, new EquipmentBroadcaster());
        this.stateAnalyzer = new TotemStateAnalyzer(inventoryManager);
        this.strategyHandler = new TotemStrategyHandler(inventoryManager);
        this.notificationManager = new TotemNotificationManager(Objects.requireNonNull(plugin, "plugin"));
        this.usageTracker = new TotemUsageTracker();
    }

    public void manageTotem() {
        if (!(bot instanceof ITrainingBot trainingBot)) return;
        if (reservesOffhandForCombatMode(trainingBot)) {
            return;
        }

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

    private boolean reservesOffhandForCombatMode(ITrainingBot trainingBot) {
        if (!trainingBot.isCombat()
                || trainingBot.getBrainController() == null
                || trainingBot.getBrainController().getBotOptions() == null) {
            return false;
        }
        CombatMode mode = trainingBot.getBrainController().getBotOptions().getCombatMode();
        return mode.equals(CombatMode.AXE_SHIELD) || mode.equals(CombatMode.UHC);
    }

    public void onTotemUsed() {
        if (bot instanceof ITrainingBot trainingBot) {
            usageTracker.onTotemUsed(trainingBot);
        }
    }

    public int getEquippedTotemCount() {
        return inventoryManager.getEquippedTotemCount();
    }

    public void forceEquipTotems(int count) {
        inventoryManager.forceEquipTotems(count);
    }

    private void handleNoTotemsWarning(ITrainingBot trainingBot) {
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
