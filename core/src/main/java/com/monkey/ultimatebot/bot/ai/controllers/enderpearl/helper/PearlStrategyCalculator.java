package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import com.monkey.ultimatebot.access.entity.AttributeAccess;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
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
        double distance = bot.getLocation().distance(target.getLocation());
        double healthPercent = bot.getHealth() / AttributeAccess.maxHealthValue(bot);
        Vector botPos = bot.getLocation().toVector();
        Vector targetPos = target.getLocation().toVector();
        double yDiff = botPos.getY() - targetPos.getY();

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

        if (isOnGround(target) && distance > 4.6 && distance < 10.5 && yDiff > 0.8) {
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
        double distance = bot.getLocation().distance(target.getLocation());
        long currentTime = System.currentTimeMillis();

        switch (strategy) {
            case COMBO_ESCAPE:
                return currentTime - lastEmergencyPearl > EMERGENCY_PEARL_COOLDOWN;
            case ESCAPE:
                return (wasRecentlyDamaged && distance < 5.2) || bot.getHealth() < 7.0f;
            case MELEE_DISENGAGE:
                return distance < 3.0 && (wasRecentlyDamaged || bot.getHealth() < 10.0f);
            case REPOSITION_LOW:
                return repositionPearlCooldown <= 0
                        && distance > 4.5
                        && distance < 13.0
                        && bot.getLocation().getY() - target.getLocation().getY() > 2.3;
            case ANCHOR_POSITION:
                return isOnGround(target)
                        && distance > 4.2
                        && distance < 11.0
                        && bot.getLocation().getY() - target.getLocation().getY() > 0.6;
            case AGGRESSIVE_CLOSE:
                return aggressivePearlCooldown <= 0 && distance > 7.0 && distance < 14.5 && bot.getHealth() > 8.0f;
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    @Override
    public @Nullable Vector calculateTargetForStrategy(
            PearlStrategy strategy, Player bot, Player target, @Nullable Vector predictedTargetMovement) {
        switch (strategy) {
            case COMBO_ESCAPE:
            case ESCAPE:
                return positionCalculator.calculateEmergencyEscape(bot, target);
            case MELEE_DISENGAGE:
                return positionCalculator.calculateMeleeDisengage(bot, target);
            case REPOSITION_LOW:
                return positionCalculator.calculateLowGroundPosition(bot, target);
            case ANCHOR_POSITION:
                return positionCalculator.calculateAnchorPosition(bot, target);
            case AGGRESSIVE_CLOSE:
                return positionCalculator.calculateAggressiveApproach(bot, target, predictedTargetMovement);
        }
        throw new IllegalStateException("Unexpected switch value");
    }

    private static boolean isOnGround(Player player) {
        return Math.abs(player.getVelocity().getY()) < 1.0E-3D;
    }
}
