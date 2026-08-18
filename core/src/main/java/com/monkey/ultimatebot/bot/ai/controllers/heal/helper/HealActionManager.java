package com.monkey.ultimatebot.bot.ai.controllers.heal.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealActionManager;
import com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter.IHealExecutor;
import com.monkey.ultimatebot.access.item.PotionEffectAccess;
import com.monkey.ultimatebot.access.item.PotionEffectTypeAccess;

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
    public void startHealAction(ITrainingBot bot) {
        if (!canStartNewHeal()) return;

        healExecutor.consumeGoldenApple(bot);
        isHealing = true;
        healingTicks = 0;
        lastHealTime = System.currentTimeMillis();
        lastHealAttempt = System.currentTimeMillis();
    }

    @Override
    public void updateHealAction(ITrainingBot bot) {
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
    public void applyGoldenAppleEffectsManually(ITrainingBot bot) {
        bot.asBukkitPlayer()
                .addPotionEffect(
                        PotionEffectAccess.of(PotionEffectTypeAccess.regeneration(), 100, 1, false, false, false));
        bot.asBukkitPlayer()
                .addPotionEffect(
                        PotionEffectAccess.of(PotionEffectTypeAccess.absorption(), 2400, 0, false, false, false));
        bot.setHealthValue(Math.min(bot.healthValue() + 4.0D, bot.maxHealthValue()));
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
