package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import com.monkey.mcbot.bot.ai.TrainingBot;

public interface ITotemUsageTracker {
    void onTotemUsed(TrainingBot trainingBot);
}