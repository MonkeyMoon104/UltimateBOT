package com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IHealthChecker {

    boolean needsHealing(ITrainingBot bot);

    float getHealthThreshold();

    void setHealthThreshold(float threshold);

    float getCurrentHealth(ITrainingBot bot);
}
