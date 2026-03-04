package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

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
        ItemStack offhand = bot.asPlayer().getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.asPlayer().getItemBySlot(EquipmentSlot.MAINHAND);

        boolean hasOffhandTotem = offhand != null && !offhand.isEmpty() &&
                offhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
        boolean hasMainhandTotem = mainhand != null && !mainhand.isEmpty() &&
                mainhand.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);

        int currentEquippedTotems = (hasOffhandTotem ? 1 : 0) + (hasMainhandTotem ? 1 : 0);
        boolean currentCombatState = bot.isCombat();

        boolean combatStateChanged = previousCombatState != currentCombatState;

        if (combatStateChanged) {
            skipNextTotemTracking = true;
        }

        if (!skipNextTotemTracking &&
                !combatStateChanged &&
                previousEquippedTotems > currentEquippedTotems &&
                totemCount > 0) {

            int consumedTotems = previousEquippedTotems - currentEquippedTotems;
            if (totemCount != -1) {
                totemCount = Math.max(0, totemCount - consumedTotems);
                BotOptions options = bot.getBrainController().getBotOptions();
                if (options != null) {
                    options.setTotems(totemCount);

                    if (bot.getTargetPlayer() != null) {
                        bot.getPlugin().getBotManager()
                                .updateTotem(bot.getTargetPlayer().getUniqueId(), totemCount);
                    }
                }
            }
        }

        skipNextTotemTracking = false;
        previousEquippedTotems = currentEquippedTotems;
        previousCombatState = currentCombatState;

        bot.getBrainController().getBotAI().manageTotem();
    }

    public int getTotemCount() { return totemCount; }
    public void setTotemCount(int count) {
        this.totemCount = count;
        this.skipNextTotemTracking = true;
    }

    public void onCombatStateChanged() {
        this.skipNextTotemTracking = true;
    }
}
