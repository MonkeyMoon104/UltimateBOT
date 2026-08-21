package com.monkey.ultimatebot.bot.ai.services;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.inventory.ItemStack;

public class TotemTrackerService {

    private final ITrainingBot bot;
    private int totemCount = -1;
    private int previousEquippedTotems = 0;
    private boolean skipNextTotemTracking = false;
    private boolean previousCombatState = false;

    public TotemTrackerService(ITrainingBot bot) {
        this.bot = bot;
    }

    public void onTick() {
        EquipmentSlotKind offHandSlot = EquipmentSlotAccess.offHand();
        ItemStack offhand = offHandSlot == null ? ItemStackAccess.empty() : bot.getItem(offHandSlot);
        ItemStack mainhand = bot.getItem(EquipmentSlotKind.HAND);

        boolean hasOffhandTotem = isTotem(offhand);
        boolean hasMainhandTotem = isTotem(mainhand);

        int currentEquippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);
        boolean currentCombatState = bot.isCombat();

        boolean combatStateChanged = previousCombatState != currentCombatState;

        if (combatStateChanged) {
            skipNextTotemTracking = true;
        }

        if (!skipNextTotemTracking
                && !combatStateChanged
                && previousEquippedTotems > currentEquippedTotems
                && totemCount > 0) {

            int consumedTotems = previousEquippedTotems - currentEquippedTotems;
            if (totemCount != -1) {
                totemCount = Math.max(0, totemCount - consumedTotems);
                BotOptions options = bot.getBrainController().getBotOptions();
                if (options != null) {
                    options.setTotems(totemCount);

                    if (bot.getTargetPlayer() != null) {
                        bot.getPlugin()
                                .getBotManager()
                                .updateTotem(bot.getTargetPlayer().getUniqueId(), totemCount);
                    }
                }
                java.util.UUID ownerUUID = bot.getPlugin().getBotRegistry().getOwnerUUIDByBotUUID(bot.getUniqueId());
                if (ownerUUID != null) {
                    com.monkey.ultimatebot.api.model.runtime.BotSnapshot snapshot =
                            bot.getPlugin().getBotEventDispatcher().snapshot(ownerUUID, bot);
                    if (snapshot != null) {
                        bot.getPlugin()
                                .getBotEventDispatcher()
                                .publish(new com.monkey.ultimatebot.api.event.combat.BotTotemUseEvent(
                                        bot.getPlugin().getBotEventDispatcher().nextSequence(bot.getUniqueId()),
                                        snapshot,
                                        consumedTotems,
                                        totemCount));
                    }
                }
            }
        }

        skipNextTotemTracking = false;
        previousEquippedTotems = currentEquippedTotems;
        previousCombatState = currentCombatState;

        bot.getBrainController().getBotAI().manageTotem();
    }

    private boolean isTotem(ItemStack itemStack) {
        return itemStack != null
                && !ItemStackAccess.isEmpty(itemStack)
                && MaterialCatalog.is(itemStack.getType(), "TOTEM_OF_UNDYING");
    }

    public int getTotemCount() {
        return totemCount;
    }

    public void setTotemCount(int count) {
        this.totemCount = count;
        this.skipNextTotemTracking = true;
    }

    public void onCombatStateChanged() {
        this.skipNextTotemTracking = true;
    }
}
