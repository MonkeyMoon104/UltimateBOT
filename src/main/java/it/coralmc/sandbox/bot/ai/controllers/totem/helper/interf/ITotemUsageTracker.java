package it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf;

import it.coralmc.sandbox.bot.ai.TrainingBot;

public interface ITotemUsageTracker {
    void onTotemUsed(TrainingBot trainingBot);
}