package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import com.monkey.mcbot.bot.ai.TrainingBot;

public interface ITotemNotificationManager {
    void sendTotemWarning(TrainingBot trainingBot);
    boolean hasWarnedOutOfTotems();
    void setWarnedOutOfTotems(boolean warned);
    void resetWarning();
}