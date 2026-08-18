package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealthChecker;

public class HealthChecker implements IHealthChecker {

    private float healthThreshold = 5.0f;

    @Override
    public boolean needsHealing(ITrainingBot bot) {
        return getCurrentHealth(bot) <= healthThreshold;
    }

    @Override
    public float getHealthThreshold() {
        return healthThreshold;
    }

    @Override
    public void setHealthThreshold(float threshold) {
        this.healthThreshold = Math.max(0.0f, Math.min(20.0f, threshold));
    }

    @Override
    public float getCurrentHealth(ITrainingBot bot) {
        return (float) bot.healthValue();
    }
}
