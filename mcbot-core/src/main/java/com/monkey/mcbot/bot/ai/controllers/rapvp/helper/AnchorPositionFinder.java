package com.monkey.mcbot.bot.ai.controllers.rapvp.helper;

import com.monkey.mcbot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.mcbot.bot.ai.rank.configs.RAPVPConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class AnchorPositionFinder {

    private final Player bot;
    private final Level level;
    private long lastSearchTime = 0;
    private RAPVPConfig config;

    public AnchorPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public Optional<BlockPos> findBestAnchorPos(Player target) {
        if (config == null) return Optional.empty();

        long now = System.currentTimeMillis();
        if (now - lastSearchTime < config.getAnchorSearchCooldownMillis()) {
            return Optional.empty();
        }
        lastSearchTime = now;

        Vec3 botPosition = bot.position();
        Vec3 currentTargetPos = target.position();

        Vec3 botForwardDirection = getBotForwardDirection();

        Vec3 predictedTargetPos = predictTargetPosition(target);
        BlockPos targetPos = BlockPos.containing(predictedTargetPos);

        BlockPos bestSafePos = null;
        double bestSafeScore = Double.NEGATIVE_INFINITY;
        BlockPos bestRiskyPos = null;
        double bestRiskyScore = Double.NEGATIVE_INFINITY;

        double maxDistanceSq = (double) config.getMaxDistance() * config.getMaxDistance();
        int horizontalRadius = Math.max(3, Math.min(config.getMaxDistance(), 8));
        int downY = -Math.max(2, horizontalRadius / 2);
        int upY = Math.max(1, horizontalRadius / 3);
        boolean hyperAggressive = config.getMaxDistance() >= 12 || config.getAnchorSearchCooldownMillis() == 0L;

        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = downY; dy <= upY; dy++) {
                    BlockPos check = targetPos.offset(dx, dy, dz);

                    if (check.equals(bot.blockPosition()) || check.equals(bot.blockPosition().below())) continue;

                    BlockState state = level.getBlockState(check);
                    BlockState below = level.getBlockState(check.below());

                    if (!state.canBeReplaced() || !below.isSolid()) continue;

                    Vec3 anchorPos = Vec3.atCenterOf(check);
                    double distSq = botPosition.distanceToSqr(anchorPos);
                    if (distSq > maxDistanceSq) continue;

                    if (!isReachable(check, maxDistanceSq)) continue;

                    if (!hyperAggressive && isAnchorBehindBot(botPosition, anchorPos, botForwardDirection)) {
                        continue;
                    }

                    double distanceToPredictedTarget = anchorPos.distanceTo(predictedTargetPos);
                    double distanceToCurrentTarget = anchorPos.distanceTo(currentTargetPos);
                    double maxAnchorDistanceToTarget = hyperAggressive ? 3.4D : 4.2D;
                    if (distanceToPredictedTarget > maxAnchorDistanceToTarget
                            && distanceToCurrentTarget > maxAnchorDistanceToTarget + 0.5D) {
                        continue;
                    }

                    if (Math.abs(anchorPos.y - currentTargetPos.y) > 3.0D) {
                        continue;
                    }

                    if (!hasLineOfSight(anchorPos, target.getEyePosition(1.0F))) {
                        continue;
                    }

                    double distanceToBot = Math.sqrt(distSq);
                    double score = evaluateAnchorScore(anchorPos, predictedTargetPos, currentTargetPos, botPosition, target, distanceToBot);
                    if (score <= 0.0D) {
                        continue;
                    }

                    if (distanceToBot >= config.getMinSafeDistance()) {
                        if (score > bestSafeScore) {
                            bestSafeScore = score;
                            bestSafePos = check;
                        }
                    } else if (score > bestRiskyScore) {
                        bestRiskyScore = score;
                        bestRiskyPos = check;
                    }
                }
            }
        }

        if (bestSafePos != null) return Optional.of(bestSafePos);
        return Optional.ofNullable(bestRiskyPos);
    }

    private Vec3 getBotForwardDirection() {
        float yaw = bot.getYRot();
        double yawRad = Math.toRadians(yaw);

        double x = -Math.sin(yawRad);
        double z = Math.cos(yawRad);

        return new Vec3(x, 0, z).normalize();
    }

    private boolean isAnchorBehindBot(Vec3 botPos, Vec3 anchorPos, Vec3 botForwardDirection) {
        Vec3 botToAnchor = anchorPos.subtract(botPos).normalize();

        double dotProduct = botForwardDirection.dot(botToAnchor);

        return dotProduct < -0.3;
    }

    private Vec3 predictTargetPosition(Player target) {
        Vec3 currentPos = target.position();
        Vec3 velocity = target.getDeltaMovement();

        if (velocity.horizontalDistance() > config.getMinMovement()) {
            Vec3 projected = currentPos.add(velocity.scale(config.getPredictionTicks()));
            Vec3 delta = projected.subtract(currentPos);
            double maxLead = Math.max(2.0D, config.getMaxDistance() * 0.9D);
            if (delta.length() > maxLead) {
                return currentPos.add(delta.normalize().scale(maxLead));
            }
            return projected;
        }

        return currentPos;
    }

    private boolean isLethalPosition(Vec3 anchorPos, Vec3 predictedTargetPos, Vec3 botPos, Player target) {
        double distanceToTarget = anchorPos.distanceTo(predictedTargetPos);
        if (distanceToTarget > 2.5) return false;

        BlockPos targetBlockPos = BlockPos.containing(predictedTargetPos);
        int solidBlocks = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos checkPos = targetBlockPos.offset(dx, 0, dz);
                if (level.getBlockState(checkPos).isSolid()) {
                    solidBlocks++;
                }
            }
        }

        boolean isTrapped = solidBlocks >= 3;
        boolean isOnOppositeSide = isOnOppositeSideOfTarget(botPos, predictedTargetPos, anchorPos);

        return isTrapped && isOnOppositeSide;
    }

    private boolean isTrappingPosition(Vec3 anchorPos, Vec3 predictedTargetPos, Vec3 currentTargetPos) {
        Vec3 targetMovement = predictedTargetPos.subtract(currentTargetPos);
        Vec3 anchorDirection = anchorPos.subtract(currentTargetPos).normalize();

        if (targetMovement.length() > config.getMinMovement()) {
            Vec3 movementDirection = targetMovement.normalize();
            double alignment = movementDirection.dot(anchorDirection);
            return alignment > 0.7;
        }

        return false;
    }

    private boolean isOnOppositeSideOfTarget(Vec3 botPos, Vec3 targetPos, Vec3 anchorPos) {
        Vec3 botToTarget = targetPos.subtract(botPos).normalize();
        Vec3 targetToAnchor = anchorPos.subtract(targetPos).normalize();
        double dotProduct = botToTarget.dot(targetToAnchor);

        return dotProduct > 0.4;
    }

    private boolean isReachable(BlockPos pos, double maxDistanceSq) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

        if (botEyes.distanceToSqr(targetPos) > maxDistanceSq) return false;

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                botEyes,
                targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );

        return level.clip(context).getType() != net.minecraft.world.phys.HitResult.Type.BLOCK;
    }

    private double evaluateAnchorScore(Vec3 anchorPos,
                                       Vec3 predictedTargetPos,
                                       Vec3 currentTargetPos,
                                       Vec3 botPos,
                                       Player target,
                                       double distanceToBot) {
        double targetDamage = ExplosionDamageEstimator.estimateAnchorDamage(level, anchorPos, target);
        double selfDamage = ExplosionDamageEstimator.estimateAnchorDamage(level, anchorPos, bot);
        if (targetDamage < 1.0D) {
            return -1.0D;
        }

        if (selfDamage >= bot.getHealth() - 1.0F) {
            return -1.0D;
        }

        double distanceToPredictedTarget = anchorPos.distanceTo(predictedTargetPos);
        double distanceToCurrentTarget = anchorPos.distanceTo(currentTargetPos);

        boolean oppositeSide = isOnOppositeSideOfTarget(botPos, predictedTargetPos, anchorPos);
        boolean lethal = isLethalPosition(anchorPos, predictedTargetPos, botPos, target);
        boolean trapping = isTrappingPosition(anchorPos, predictedTargetPos, currentTargetPos);

        double score = (targetDamage * 3.4D) - (selfDamage * 2.9D);
        score += Math.max(0.0D, 3.5D - distanceToPredictedTarget) * 0.9D;
        score += Math.max(0.0D, 3.0D - distanceToCurrentTarget) * 0.4D;
        score -= Math.max(0.0D, distanceToPredictedTarget - 2.6D) * 3.8D;
        score -= Math.max(0.0D, distanceToCurrentTarget - 3.0D) * 3.1D;

        if (oppositeSide) {
            score += 1.4D;
        }
        if (trapping) {
            score += 1.2D;
        }
        if (lethal) {
            score += 2.3D;
        }
        if (targetDamage > target.getHealth()) {
            score += 4.0D;
        }

        if (distanceToBot < config.getMinSafeDistance()) {
            score -= (config.getMinSafeDistance() - distanceToBot) * 2.8D;
        }

        return score;
    }

    private boolean hasLineOfSight(Vec3 start, Vec3 end) {
        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );
        return level.clip(context).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    public void setConfig(RAPVPConfig config) {
        this.config = config;
    }
}
