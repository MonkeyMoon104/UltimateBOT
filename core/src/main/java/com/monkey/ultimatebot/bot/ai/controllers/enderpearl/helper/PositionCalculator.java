package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class PositionCalculator implements IPositionCalculator {

    private static final int PREDICT_TICKS = 8;
    private static final double[] ANGLES_DEG = {0, 25, -25, 45, -45, 70, -70, 110, -110, 180};

    private final ISafetyValidator safetyValidator;

    public PositionCalculator(ISafetyValidator safetyValidator) {
        this.safetyValidator = safetyValidator;
    }

    @Override
    public @Nullable Vector calculateEmergencyEscape(Player bot, Player target) {
        Vector botPos = bot.getLocation().toVector();
        Vector awayDirection = normalizeOrFallback(botPos.clone().subtract(target.getLocation().toVector()), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[] {7.0D, 8.0D, 9.2D},
                new double[] {0.4D, 0.9D, 1.3D},
                8.6D,
                12.5D,
                false,
                11.5D);

        return best != null
                ? best
                : fallbackSafePosition(bot, toBlockVector(botPos.clone().add(awayDirection.multiply(8.0D))));
    }

    @Override
    public @Nullable Vector calculateMeleeDisengage(Player bot, Player target) {
        Vector botPos = bot.getLocation().toVector();
        Vector awayDirection = normalizeOrFallback(botPos.clone().subtract(target.getLocation().toVector()), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[] {6.0D, 7.2D, 8.2D},
                new double[] {0.9D, 1.3D, 1.7D},
                7.0D,
                10.5D,
                false,
                10.0D);

        return best != null
                ? best
                : fallbackSafePosition(bot, toBlockVector(botPos.clone().add(awayDirection.multiply(7.0D))));
    }

    @Override
    public @Nullable Vector calculateLowGroundPosition(Player bot, Player target) {
        Vector targetPos = target.getLocation().toVector();
        Vector aroundDirection = normalizeOrFallback(bot.getLocation().toVector().subtract(targetPos), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                targetPos,
                aroundDirection,
                new double[] {3.6D, 4.4D, 5.0D},
                new double[] {-2.0D, -2.5D, -3.0D},
                4.4D,
                6.4D,
                true,
                12.0D);

        return best != null
                ? best
                : fallbackSafePosition(
                        bot,
                        toBlockVector(targetPos.clone().add(aroundDirection.multiply(4.2D)).add(new Vector(0.0D, -2.4D, 0.0D))));
    }

    @Override
    public @Nullable Vector calculateAnchorPosition(Player bot, Player target) {
        Vector predictedTarget = target.getLocation().toVector().add(target.getVelocity().multiply(PREDICT_TICKS / 20.0D));
        Vector aroundDirection = normalizeOrFallback(predictedTarget.clone().subtract(bot.getLocation().toVector()), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                predictedTarget,
                aroundDirection,
                new double[] {3.2D, 3.8D, 4.4D},
                new double[] {-0.9D, -1.2D, -1.6D},
                3.7D,
                5.3D,
                true,
                11.0D);

        return best != null
                ? best
                : fallbackSafePosition(
                        bot,
                        toBlockVector(predictedTarget.clone()
                                .add(aroundDirection.multiply(3.8D))
                                .add(new Vector(0.0D, -1.2D, 0.0D))));
    }

    @Override
    public @Nullable Vector calculateAggressiveApproach(
            Player bot, Player target, @Nullable Vector predictedTargetMovement) {
        Vector targetPos = target.getLocation().toVector();
        Vector movement = predictedTargetMovement == null ? new Vector() : predictedTargetMovement.clone();
        Vector predictedPos = targetPos.clone().add(movement.multiply(PREDICT_TICKS / 20.0D));

        Vector approachDirection = normalizeOrFallback(bot.getLocation().toVector().subtract(predictedPos), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                predictedPos,
                approachDirection,
                new double[] {2.1D, 2.7D, 3.2D},
                new double[] {0.0D, 0.4D, 0.7D},
                2.8D,
                4.8D,
                false,
                10.5D);

        return best != null
                ? best
                : fallbackSafePosition(
                        bot,
                        toBlockVector(predictedPos.clone()
                                .add(approachDirection.multiply(2.7D))
                                .add(new Vector(0.0D, 0.4D, 0.0D))));
    }

    @Override
    public @Nullable Vector calculateStandardEscape(Player bot, Player target) {
        Vector botPos = bot.getLocation().toVector();
        Vector awayDirection = normalizeOrFallback(botPos.clone().subtract(target.getLocation().toVector()), bot);
        Vector best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[] {8.0D, 9.5D, 11.0D},
                new double[] {0.2D, 0.8D, 1.4D},
                9.2D,
                13.5D,
                false,
                13.0D);

        return best != null
                ? best
                : fallbackSafePosition(bot, toBlockVector(botPos.clone().add(awayDirection.multiply(9.0D))));
    }

    private @Nullable Vector findBestCandidate(
            Player bot,
            Player target,
            Vector origin,
            Vector baseDirection,
            double[] distances,
            double[] yOffsets,
            double idealTargetDistance,
            double maxTargetDistance,
            boolean preferLowerThanTarget,
            double maxBotDistance) {
        Vector best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Vector targetPosition = target.getLocation().toVector();
        Vector botPosition = bot.getLocation().toVector();

        for (double distance : distances) {
            for (double angleDeg : ANGLES_DEG) {
                Vector direction = rotateVector(baseDirection, Math.toRadians(angleDeg));
                for (double yOffset : yOffsets) {
                    Vector candidate = origin.clone().add(direction.clone().multiply(distance)).add(new Vector(0.0D, yOffset, 0.0D));
                    BlockVector candidateBlock = toBlockVector(candidate);
                    if (!safetyValidator.isSafeLandingSpot(candidateBlock)) {
                        continue;
                    }

                    Vector center = centerOf(candidateBlock);
                    if (!hasThrowPath(bot, center)) {
                        continue;
                    }

                    double distanceToTarget = center.distance(targetPosition);
                    double distanceToBot = center.distance(botPosition);
                    if (distanceToTarget > maxTargetDistance || distanceToBot > maxBotDistance) {
                        continue;
                    }

                    double verticalFromTarget = center.getY() - targetPosition.getY();
                    double score = 0.0D;
                    score -= Math.abs(distanceToTarget - idealTargetDistance) * 2.2D;
                    score -= distanceToBot * 0.14D;

                    if (preferLowerThanTarget) {
                        score -= Math.max(0.0D, verticalFromTarget) * 3.6D;
                        score += Math.max(0.0D, -verticalFromTarget) * 0.85D;
                    } else {
                        score -= Math.max(0.0D, verticalFromTarget - 2.0D) * 1.8D;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        best = center;
                    }
                }
            }
        }

        return best;
    }

    private @Nullable Vector fallbackSafePosition(Player bot, BlockVector preferred) {
        if (safetyValidator.isSafeLandingSpot(preferred)) {
            Vector center = centerOf(preferred);
            if (hasThrowPath(bot, center)) {
                return center;
            }
        }

        Vector safe = safetyValidator.findSafeLandingSpot(preferred);
        if (safe != null && hasThrowPath(bot, safe)) {
            return safe;
        }
        return safe;
    }

    private boolean hasThrowPath(Player bot, Vector destination) {
        Vector eyes = bot.getEyeLocation().toVector();
        Vector delta = destination.clone().subtract(eyes);
        double distance = delta.length();
        if (distance < 1.0E-6D) {
            return true;
        }
        RayTraceResult result = bot.getWorld().rayTraceBlocks(
                bot.getEyeLocation(), delta.normalize(), distance, FluidCollisionMode.NEVER, true);
        return result == null || result.getHitBlock() == null;
    }

    private Vector normalizeOrFallback(Vector vector, Player bot) {
        if (vector.lengthSquared() > 1.0E-5D) {
            return vector.normalize();
        }
        Vector look = bot.getLocation().getDirection();
        if (look.lengthSquared() > 1.0E-5D) {
            return look.multiply(-1.0D).normalize();
        }
        return new Vector(1.0D, 0.0D, 0.0D);
    }

    private Vector rotateVector(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector(
                        vector.getX() * cos - vector.getZ() * sin,
                        vector.getY(),
                        vector.getX() * sin + vector.getZ() * cos)
                .normalize();
    }

    private static BlockVector toBlockVector(Vector value) {
        return new BlockVector(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static Vector centerOf(BlockVector value) {
        return new Vector(value.getBlockX() + 0.5D, value.getBlockY() + 0.5D, value.getBlockZ() + 0.5D);
    }
}
