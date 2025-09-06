package it.coralmc.sandbox.bot.ai.controllers.totem.helper;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf.ITotemUsageTracker;

public class TotemUsageTracker implements ITotemUsageTracker {

    @Override
    public void onTotemUsed(TrainingBot trainingBot) {
        int totemCount = trainingBot.getTotemCount();

        if (totemCount > 0) {
            trainingBot.setTotemCount(totemCount - 1);
        }
    }
}