package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import com.monkey.mcbot.bot.ai.ITrainingBot;

public interface ITotemUsageTracker {
    void onTotemUsed(ITrainingBot trainingBot);
}