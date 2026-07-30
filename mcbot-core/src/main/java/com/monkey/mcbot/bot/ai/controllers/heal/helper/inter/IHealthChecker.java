package com.monkey.mcbot.bot.ai.controllers.heal.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IHealthChecker {

    boolean needsHealing(Player bot);

    float getHealthThreshold();

    void setHealthThreshold(float threshold);

    float getCurrentHealth(Player bot);
}
