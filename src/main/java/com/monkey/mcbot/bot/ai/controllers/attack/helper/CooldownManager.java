package com.monkey.mcbot.bot.ai.controllers.attack.helper;

import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.ICooldownManager;

import java.util.Random;

public class CooldownManager implements ICooldownManager {

    private final Random random = new Random();
    private int attackCooldown = 0;

    @Override
    public void tick() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    @Override
    public boolean canAttack() {
        return attackCooldown <= 0;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.attackCooldown = cooldown;
    }

    @Override
    public void setRandomCooldown(int base, int range) {
        this.attackCooldown = base + random.nextInt(range);
    }

    @Override
    public int getCooldown() {
        return attackCooldown;
    }
}