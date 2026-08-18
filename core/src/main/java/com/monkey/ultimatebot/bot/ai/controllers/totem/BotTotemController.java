package com.monkey.ultimatebot.bot.ai.controllers.totem;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
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
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public class BotTotemController {
    private final ITrainingBot bot;

    private final ITotemInventoryManager inventoryManager;
    private final ITotemStateAnalyzer stateAnalyzer;
    private final ITotemStrategyHandler strategyHandler;
    private final ITotemNotificationManager notificationManager;
    private final ITotemUsageTracker usageTracker;

    public BotTotemController(ITrainingBot bot, UltimateBot plugin) {
        this.bot = Objects.requireNonNull(bot, "bot");

        this.inventoryManager = new TotemInventoryManager(bot, new EquipmentBroadcaster());
        this.stateAnalyzer = new TotemStateAnalyzer(inventoryManager);
        this.strategyHandler = new TotemStrategyHandler(inventoryManager);
        this.notificationManager = new TotemNotificationManager(Objects.requireNonNull(plugin, "plugin"));
        this.usageTracker = new TotemUsageTracker();
    }

    public void manageTotem() {
        if (reservesOffhandForCombatMode(bot)) {
            return;
        }

        EquipmentSlotKind offHandSlot = EquipmentSlotAccess.offHand();
        if (offHandSlot == null) {
            return;
        }
        ItemStack offhand = bot.getItem(offHandSlot);
        ItemStack mainhand = bot.getItem(EquipmentSlotKind.HAND);

        int totemCount = bot.getTotemCount();
        boolean isCombat = bot.isCombat();

        TotemState totemState = stateAnalyzer.getTotemState(totemCount);
        TotemEquipmentState equipmentState = stateAnalyzer.analyzeCurrentEquipment(offhand, mainhand);

                switch (totemState) {
            case UNLIMITED:
                strategyHandler.handleUnlimitedTotems(equipmentState, isCombat);
                                notificationManager.resetWarning();
                break;
            case NONE:
                strategyHandler.handleNoTotems(equipmentState);
                                handleNoTotemsWarning(bot);
                break;
            case ONE:
                strategyHandler.handleOneTotem(equipmentState, isCombat);
                                notificationManager.resetWarning();
                break;
            case MULTIPLE:
                strategyHandler.handleMultipleTotems(totemCount, equipmentState, isCombat);
                                notificationManager.resetWarning();
                break;
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
        usageTracker.onTotemUsed(bot);
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
