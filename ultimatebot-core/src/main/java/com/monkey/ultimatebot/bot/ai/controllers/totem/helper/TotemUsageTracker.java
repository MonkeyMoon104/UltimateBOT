package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemUsageTracker;

public class TotemUsageTracker implements ITotemUsageTracker {

    @Override
    public void onTotemUsed(ITrainingBot trainingBot) {
        int totemCount = trainingBot.getTotemCount();

        if (totemCount > 0) {
            trainingBot.setTotemCount(totemCount - 1);
        }
    }
}
