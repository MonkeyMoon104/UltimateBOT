package it.coralmc.sandbox.bot.ai.controllers.totem.interf;

import it.coralmc.sandbox.bot.ai.TrainingBot;

public interface ITotemNotificationManager {
    void sendTotemWarning(TrainingBot trainingBot);
    boolean hasWarnedOutOfTotems();
    void setWarnedOutOfTotems(boolean warned);
    void resetWarning();
}