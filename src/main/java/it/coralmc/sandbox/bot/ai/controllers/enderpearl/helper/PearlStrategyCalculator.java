package it.coralmc.sandbox.bot.ai.controllers.enderpearl.helper;

import it.coralmc.sandbox.bot.ai.controllers.enderpearl.helper.inter.IPearlStrategyCalculator;
import it.coralmc.sandbox.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PearlStrategyCalculator implements IPearlStrategyCalculator {

    private static final long DAMAGE_REACTION_WINDOW = 2000;
    private static final long EMERGENCY_PEARL_COOLDOWN = 3000;
    private static final int PREDICT_TICKS = 8;

    private final IPositionCalculator positionCalculator;

    public PearlStrategyCalculator(IPositionCalculator positionCalculator) {
        this.positionCalculator = positionCalculator;
    }

    @Override
    public PearlStrategy determineOptimalStrategy(Player bot, Player target, boolean wasRecentlyDamaged, int damageComboCount, long comboStartTime, int repositionPearlCooldown, int aggressivePearlCooldown) {
        double distance = bot.distanceTo(target);
        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        long currentTime = System.currentTimeMillis();

        if (bot.position().y > target.position().y &&
                distance > 4.0 && distance < 15.0) {
            return PearlStrategy.REPOSITION_LOW;
        }

        if (healthPercent < 0.3f ||
                (damageComboCount >= 2 && currentTime - comboStartTime < 2000)) {
            return PearlStrategy.COMBO_ESCAPE;
        }


        if (wasRecentlyDamaged && distance < 4.0) {
            return PearlStrategy.ESCAPE;
        }

        if (distance < 2.5 && healthPercent < 0.6f) {
            return PearlStrategy.MELEE_DISENGAGE;
        }

        if (yDiff > 1.5 && distance > 4.0 && distance < 15.0 && repositionPearlCooldown <= 0) {
            return PearlStrategy.REPOSITION_LOW;
        }

        if (target.onGround() && distance > 5.0 && distance < 12.0 && yDiff > 0) {
            return PearlStrategy.ANCHOR_POSITION;
        }

        if (distance > 8.0 && healthPercent > 0.6f && aggressivePearlCooldown <= 0) {
            return PearlStrategy.AGGRESSIVE_CLOSE;
        }

        return PearlStrategy.ESCAPE;
    }

    @Override
    public boolean shouldUsePearlForStrategy(PearlStrategy strategy, Player bot, Player target, boolean wasRecentlyDamaged, long lastEmergencyPearl, int repositionPearlCooldown, int aggressivePearlCooldown) {
        double distance = bot.distanceTo(target);
        long currentTime = System.currentTimeMillis();

        switch (strategy) {
            case COMBO_ESCAPE:
                return currentTime - lastEmergencyPearl > EMERGENCY_PEARL_COOLDOWN;

            case ESCAPE:
                return wasRecentlyDamaged || bot.getHealth() < 8.0f;

            case MELEE_DISENGAGE:
                return distance < 3.0 && (wasRecentlyDamaged || bot.getHealth() < 10.0f);

            case REPOSITION_LOW:
                return repositionPearlCooldown <= 0 && distance > 4.0;

            case ANCHOR_POSITION:
                return target.onGround() && distance > 4.0;

            case AGGRESSIVE_CLOSE:
                return aggressivePearlCooldown <= 0 && distance > 6.0 && bot.getHealth() > 8.0f;

            default:
                return Math.random() < 0.4;
        }
    }

    @Override
    public Vec3 calculateTargetForStrategy(PearlStrategy strategy, Player bot, Player target, Vec3 predictedTargetMovement) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 predictedTargetPos = targetPos.add(predictedTargetMovement.scale(PREDICT_TICKS / 20.0));

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

            default:
                return positionCalculator.calculateStandardEscape(bot, target);
        }
    }
}