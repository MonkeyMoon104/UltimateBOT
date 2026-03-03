package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IDamageTracker;
import net.minecraft.world.entity.player.Player;

public class DamageTracker implements IDamageTracker {

    private static final long DAMAGE_REACTION_WINDOW = 2000;

    private float lastHealth;
    private long lastDamageTime = 0;
    private int damageComboCount = 0;
    private long comboStartTime = 0;
    private boolean wasRecentlyDamaged = false;

    public DamageTracker(Player bot) {
        this.lastHealth = bot.getHealth();
    }

    @Override
    public void onDamageReceived(Player bot) {
        float currentHealth = bot.getHealth();
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