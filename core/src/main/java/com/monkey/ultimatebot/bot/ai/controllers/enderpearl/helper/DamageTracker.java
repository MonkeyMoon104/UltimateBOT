package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IDamageTracker;

public class DamageTracker implements IDamageTracker {

    private static final long DAMAGE_REACTION_WINDOW = 2000;

    private float lastHealth;
    private long lastDamageTime = 0;
    private int damageComboCount = 0;
    private long comboStartTime = 0;
    private boolean wasRecentlyDamaged = false;

    public DamageTracker(ITrainingBot bot) {
        this.lastHealth = (float) bot.healthValue();
    }

    @Override
    public void onDamageReceived(ITrainingBot bot) {
        float currentHealth = (float) bot.healthValue();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastHealth) {
            wasRecentlyDamaged = true;
            lastDamageTime = currentTime;

            if (currentTime - comboStartTime < DAMAGE_REACTION_WINDOW) {
                damageComboCount++;
            } else {
                damageComboCount = 1;
                comboStartTime = currentTime;
            }
        }

        lastHealth = currentHealth;
    }

    @Override
    public void tick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > DAMAGE_REACTION_WINDOW) {
            wasRecentlyDamaged = false;
        }

        if (currentTime - comboStartTime > DAMAGE_REACTION_WINDOW * 2) {
            damageComboCount = 0;
        }
    }

    @Override
    public boolean wasRecentlyDamaged() {
        return wasRecentlyDamaged;
    }

    @Override
    public int getDamageComboCount() {
        return damageComboCount;
    }

    @Override
    public long getComboStartTime() {
        return comboStartTime;
    }

    @Override
    public void resetDamageState() {
        wasRecentlyDamaged = false;
        damageComboCount = 0;
    }
}
