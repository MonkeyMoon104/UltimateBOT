package it.coralmc.sandbox.bot.ai.controllers.heal.helper;

import it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter.IHealthChecker;
import net.minecraft.world.entity.player.Player;

public class HealthChecker implements IHealthChecker {

    private float healthThreshold = 10.0f;

    @Override
    public boolean needsHealing(Player bot) {
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
    public float getCurrentHealth(Player bot) {
        return bot.getHealth();
    }
}