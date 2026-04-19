package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PositionCalculator implements IPositionCalculator {

    private static final int PREDICT_TICKS = 8;
    private static final double[] ANGLES_DEG = {0, 25, -25, 45, -45, 70, -70, 110, -110, 180};

    private final ISafetyValidator safetyValidator;

    public PositionCalculator(ISafetyValidator safetyValidator) {
        this.safetyValidator = safetyValidator;
    }

    @Override
    public Vec3 calculateEmergencyEscape(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 awayDirection = normalizeOrFallback(botPos.subtract(target.position()), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[]{7.0D, 8.0D, 9.2D},
                new double[]{0.4D, 0.9D, 1.3D},
                8.6D,
                12.5D,
                false,
                11.5D
        );

        return best != null ? best : fallbackSafePosition(bot, BlockPos.containing(botPos.add(awayDirection.scale(8.0D))));
    }

    @Override
    public Vec3 calculateMeleeDisengage(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 awayDirection = normalizeOrFallback(botPos.subtract(target.position()), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[]{6.0D, 7.2D, 8.2D},
                new double[]{0.9D, 1.3D, 1.7D},
                7.0D,
                10.5D,
                false,
                10.0D
        );

        return best != null ? best : fallbackSafePosition(bot, BlockPos.containing(botPos.add(awayDirection.scale(7.0D))));
    }

    @Override
    public Vec3 calculateLowGroundPosition(Player bot, Player target) {
        Vec3 targetPos = target.position();
        Vec3 aroundDirection = normalizeOrFallback(bot.position().subtract(targetPos), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                targetPos,
                aroundDirection,
                new double[]{3.6D, 4.4D, 5.0D},
                new double[]{-2.0D, -2.5D, -3.0D},
                4.4D,
                6.4D,
                true,
                12.0D
        );

        if (best != null) {
            return best;
        }
        return fallbackSafePosition(bot, BlockPos.containing(targetPos.add(aroundDirection.scale(4.2D)).add(0.0D, -2.4D, 0.0D)));
    }

    @Override
    public Vec3 calculateAnchorPosition(Player bot, Player target) {
        Vec3 predictedTarget = target.position().add(target.getDeltaMovement().scale(PREDICT_TICKS / 20.0D));
        Vec3 aroundDirection = normalizeOrFallback(predictedTarget.subtract(bot.position()), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                predictedTarget,
                aroundDirection,
                new double[]{3.2D, 3.8D, 4.4D},
                new double[]{-0.9D, -1.2D, -1.6D},
                3.7D,
                5.3D,
                true,
                11.0D
        );

        if (best != null) {
            return best;
        }
        return fallbackSafePosition(bot, BlockPos.containing(predictedTarget.add(aroundDirection.scale(3.8D)).add(0.0D, -1.2D, 0.0D)));
    }

    @Override
    public Vec3 calculateAggressiveApproach(Player bot, Player target, Vec3 predictedTargetMovement) {
        Vec3 targetPos = target.position();
        Vec3 movement = predictedTargetMovement == null ? Vec3.ZERO : predictedTargetMovement;
        Vec3 predictedPos = targetPos.add(movement.scale(PREDICT_TICKS / 20.0D));

        Vec3 approachDirection = normalizeOrFallback(bot.position().subtract(predictedPos), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                predictedPos,
                approachDirection,
                new double[]{2.1D, 2.7D, 3.2D},
                new double[]{0.0D, 0.4D, 0.7D},
                2.8D,
                4.8D,
                false,
                10.5D
        );

        if (best != null) {
            return best;
        }
        return fallbackSafePosition(bot, BlockPos.containing(predictedPos.add(approachDirection.scale(2.7D)).add(0.0D, 0.4D, 0.0D)));
    }

    @Override
    public Vec3 calculateStandardEscape(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 awayDirection = normalizeOrFallback(botPos.subtract(target.position()), bot);
        Vec3 best = findBestCandidate(
                bot,
                target,
                botPos,
                awayDirection,
                new double[]{8.0D, 9.5D, 11.0D},
                new double[]{0.2D, 0.8D, 1.4D},
                9.2D,
                13.5D,
                false,
                13.0D
        );

        return best != null ? best : fallbackSafePosition(bot, BlockPos.containing(botPos.add(awayDirection.scale(9.0D))));
    }

    private Vec3 findBestCandidate(Player bot,
                                   Player target,
                                   Vec3 origin,
                                   Vec3 baseDirection,
                                   double[] distances,
                                   double[] yOffsets,
                                   double idealTargetDistance,
                                   double maxTargetDistance,
                                   boolean preferLowerThanTarget,
                                   double maxBotDistance) {
        Vec3 best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (double distance : distances) {
            for (double angleDeg : ANGLES_DEG) {
                Vec3 direction = rotateVector(baseDirection, Math.toRadians(angleDeg));
                for (double yOffset : yOffsets) {
                    Vec3 candidate = origin.add(direction.scale(distance)).add(0.0D, yOffset, 0.0D);
                    BlockPos candidateBlock = BlockPos.containing(candidate);
                    if (!safetyValidator.isSafeLandingSpot(candidateBlock)) {
                        continue;
                    }

                    Vec3 center = Vec3.atCenterOf(candidateBlock);
                    if (!hasThrowPath(bot, center)) {
                        continue;
                    }

                    double distanceToTarget = center.distanceTo(target.position());
                    double distanceToBot = center.distanceTo(bot.position());
                    if (distanceToTarget > maxTargetDistance || distanceToBot > maxBotDistance) {
                        continue;
                    }

                    double verticalFromTarget = center.y - target.position().y;
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

    private Vec3 fallbackSafePosition(Player bot, BlockPos preferred) {
        if (safetyValidator.isSafeLandingSpot(preferred)) {
            Vec3 center = Vec3.atCenterOf(preferred);
            if (hasThrowPath(bot, center)) {
                return center;
            }
        }

        Vec3 safe = safetyValidator.findSafeLandingSpot(preferred);
        if (safe != null && hasThrowPath(bot, safe)) {
            return safe;
        }
        return safe;
    }

    private boolean hasThrowPath(Player bot, Vec3 destination) {
        Vec3 eyes = bot.getEyePosition(1.0F);
        ClipContext context = new ClipContext(
                eyes,
                destination,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                bot
        );

        HitResult result = bot.level().clip(context);
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (result instanceof BlockHitResult blockHit) {
            BlockPos destinationBlock = BlockPos.containing(destination);
            BlockPos hitBlock = blockHit.getBlockPos();
            return hitBlock.equals(destinationBlock)
                    || hitBlock.equals(destinationBlock.below())
                    || hitBlock.equals(destinationBlock.above());
        }

        return false;
    }

    private Vec3 normalizeOrFallback(Vec3 vector, Player bot) {
        if (vector.lengthSqr() > 1.0E-5D) {
            return vector.normalize();
        }
        Vec3 look = bot.getLookAngle();
        if (look.lengthSqr() > 1.0E-5D) {
            return look.scale(-1.0D).normalize();
        }
        return new Vec3(1.0D, 0.0D, 0.0D);
    }

    private Vec3 rotateVector(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        ).normalize();
    }
}
