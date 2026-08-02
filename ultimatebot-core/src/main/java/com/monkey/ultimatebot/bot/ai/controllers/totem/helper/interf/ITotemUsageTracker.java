package com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface ITotemUsageTracker {
    void onTotemUsed(ITrainingBot trainingBot);
}
