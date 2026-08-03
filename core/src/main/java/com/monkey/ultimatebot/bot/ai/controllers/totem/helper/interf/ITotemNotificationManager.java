package com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface ITotemNotificationManager {
    void sendTotemWarning(ITrainingBot trainingBot);

    boolean hasWarnedOutOfTotems();

    void setWarnedOutOfTotems(boolean warned);

    void resetWarning();
}
