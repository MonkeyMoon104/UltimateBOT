package it.coralmc.sandbox.bot.ai.controllers.attack.inter;

public interface ICooldownManager {

    void tick();

    boolean canAttack();

    void setCooldown(int cooldown);

    void setRandomCooldown(int base, int range);

    int getCooldown();
}