package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PearlStrategyCalculator implements IPearlStrategyCalculator {

    private static final long EMERGENCY_PEARL_COOLDOWN = 3000;

    private final IPositionCalculator positionCalculator;

    public PearlStrategyCalculator(IPositionCalculator positionCalculator) {
        this.positionCalculator = positionCalculator;
    }

    @Override
    public PearlStrategy determineOptimalStrategy(
            Player bot,
            Player target,
            boolean wasRecentlyDamaged,
            int damageComboCount,
            long comboStartTime,
            int repositionPearlCooldown,
            int aggressivePearlCooldown) {
        double distance = bot.distanceTo(target);
        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        long currentTime = System.currentTimeMillis();

        if (healthPercent < 0.3f || (damageComboCount >= 2 && currentTime - comboStartTime < 2000)) {
            return PearlStrategy.COMBO_ESCAPE;
        }

        if (wasRecentlyDamaged && distance < 4.8) {
            return PearlStrategy.ESCAPE;
        }

        if (distance < 2.5 && healthPercent < 0.6f) {
            return PearlStrategy.MELEE_DISENGAGE;
        }

        if (yDiff > 2.8 && distance > 4.5 && distance < 12.5 && repositionPearlCooldown <= 0) {
            return PearlStrategy.REPOSITION_LOW;
        }

        if (target.onGround() && distance > 4.6 && distance < 10.5 && yDiff > 0.8) {
            return PearlStrategy.ANCHOR_POSITION;
        }

        if (distance > 9.0 && distance < 14.0 && healthPercent > 0.6f && aggressivePearlCooldown <= 0) {
            return PearlStrategy.AGGRESSIVE_CLOSE;
        }

        return PearlStrategy.ESCAPE;
    }

    @Override
    public boolean shouldUsePearlForStrategy(
            PearlStrategy strategy,
            Player bot,
            Player target,
            boolean wasRecentlyDamaged,
            long lastEmergencyPearl,
            int repositionPearlCooldown,
            int aggressivePearlCooldown) {
        double distance = bot.distanceTo(target);
        long currentTime = System.currentTimeMillis();

        return switch (strategy) {
            case COMBO_ESCAPE -> currentTime - lastEmergencyPearl > EMERGENCY_PEARL_COOLDOWN;
            case ESCAPE -> (wasRecentlyDamaged && distance < 5.2) || bot.getHealth() < 7.0f;
            case MELEE_DISENGAGE -> distance < 3.0 && (wasRecentlyDamaged || bot.getHealth() < 10.0f);
            case REPOSITION_LOW ->
                repositionPearlCooldown <= 0
                        && distance > 4.5
                        && distance < 13.0
                        && bot.position().y - target.position().y > 2.3;
            case ANCHOR_POSITION ->
                target.onGround() && distance > 4.2 && distance < 11.0 && bot.position().y - target.position().y > 0.6;
            case AGGRESSIVE_CLOSE ->
                aggressivePearlCooldown <= 0 && distance > 7.0 && distance < 14.5 && bot.getHealth() > 8.0f;
        };
    }

    @Override
    public Vec3 calculateTargetForStrategy(
            PearlStrategy strategy, Player bot, Player target, Vec3 predictedTargetMovement) {
        return switch (strategy) {
            case COMBO_ESCAPE, ESCAPE -> positionCalculator.calculateEmergencyEscape(bot, target);
            case MELEE_DISENGAGE -> positionCalculator.calculateMeleeDisengage(bot, target);
            case REPOSITION_LOW -> positionCalculator.calculateLowGroundPosition(bot, target);
            case ANCHOR_POSITION -> positionCalculator.calculateAnchorPosition(bot, target);
            case AGGRESSIVE_CLOSE ->
                positionCalculator.calculateAggressiveApproach(bot, target, predictedTargetMovement);
        };
    }
}
