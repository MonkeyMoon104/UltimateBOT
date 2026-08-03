package com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter;

public interface ICooldownManager {

    void tick();

    boolean canAttack();

    void setCooldown(int cooldown);

    void setRandomCooldown(int base, int range);

    int getCooldown();
}
