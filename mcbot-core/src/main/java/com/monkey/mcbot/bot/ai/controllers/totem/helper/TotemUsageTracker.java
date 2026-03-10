package com.monkey.mcbot.bot.ai.controllers.totem.helper;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.totem.helper.interf.ITotemUsageTracker;

public class TotemUsageTracker implements ITotemUsageTracker {

    @Override
    public void onTotemUsed(ITrainingBot trainingBot) {
        int totemCount = trainingBot.getTotemCount();

        if (totemCount > 0) {
            trainingBot.setTotemCount(totemCount - 1);
        }
    }
}