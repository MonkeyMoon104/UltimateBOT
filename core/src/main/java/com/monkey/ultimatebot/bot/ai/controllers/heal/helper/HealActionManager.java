package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealActionManager;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import net.minecraft.world.entity.player.Player;

public class HealActionManager implements IHealActionManager {

    private final IHealExecutor healExecutor;

    private boolean isHealing = false;
    private int healingTicks = 0;
    private long lastHealTime = 0;
    private long lastHealAttempt = 0;

    private static final int HEAL_DURATION_TICKS = 32;
    private static final long MIN_HEAL_COOLDOWN = 45000;
    private static final long MIN_HEAL_ATTEMPT_INTERVAL = 2000;

    public HealActionManager(IHealExecutor healExecutor) {
        this.healExecutor = healExecutor;
    }

    @Override
    public void startHealAction(Player bot) {
        if (!canStartNewHeal()) return;

        healExecutor.consumeGoldenApple(bot);
        isHealing = true;
        healingTicks = 0;
        lastHealTime = System.currentTimeMillis();
        lastHealAttempt = System.currentTimeMillis();
    }

    @Override
    public void updateHealAction(Player bot) {
        if (!isHealing) return;

        healingTicks++;

        if (healingTicks >= HEAL_DURATION_TICKS || !bot.isUsingItem()) {
            resetHealAction();
        }
    }

    @Override
    public boolean isHealing() {
        return isHealing;
    }

    @Override
    public void resetHealAction() {
        isHealing = false;
        healingTicks = 0;
    }

    @Override
    public boolean canStartNewHeal() {
        if (isHealing) return false;

        long currentTime = System.currentTimeMillis();

        boolean cooldownPassed = (currentTime - lastHealTime) >= MIN_HEAL_COOLDOWN;

        boolean attemptIntervalPassed = (currentTime - lastHealAttempt) >= MIN_HEAL_ATTEMPT_INTERVAL;

        if (!attemptIntervalPassed) {
            lastHealAttempt = currentTime;
        }

        return cooldownPassed && attemptIntervalPassed;
    }

    @Override
    public void applyGoldenAppleEffectsManually(Player bot) {
        try {
            net.minecraft.world.effect.MobEffectInstance regeneration =
                    new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1);

            net.minecraft.world.effect.MobEffectInstance absorption = new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 0);

            bot.addEffect(regeneration);
            bot.addEffect(absorption);

            float currentHealth = bot.getHealth();
            float newHealth = Math.min(currentHealth + 4.0f, bot.getMaxHealth());
            bot.setHealth(newHealth);

        } catch (Exception e) {
            float currentHealth = bot.getHealth();
            float newHealth = Math.min(currentHealth + 4.0f, bot.getMaxHealth());
            bot.setHealth(newHealth);
        }
    }

    public boolean isInCooldown() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastHealTime) < MIN_HEAL_COOLDOWN;
    }

    public void forceReset() {
        isHealing = false;
        healingTicks = 0;
        lastHealAttempt = System.currentTimeMillis() - MIN_HEAL_ATTEMPT_INTERVAL;
    }
}
