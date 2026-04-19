package com.monkey.mcbot.bot.ai.controllers.rapvp.helper;

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

        BlockPos lethalPos = null;
        BlockPos smartPos = null;
        BlockPos safePos = null;
        BlockPos fallbackPos = null;
        double maxDistanceSq = 12 * 12;

        int[] offsets = {-3, -2, -1, 0, 1, 2, 3};
        int[] yOffsets = {-2, -1, 0, 1};

        for (int dx : offsets) {
            for (int dz : offsets) {
                for (int dy : yOffsets) {
                    BlockPos check = targetPos.offset(dx, dy, dz);

                    if (check.equals(bot.blockPosition()) || check.equals(bot.blockPosition().below())) continue;

                    BlockState state = level.getBlockState(check);
                    BlockState below = level.getBlockState(check.below());

                    if (!state.canBeReplaced() || !below.isSolid()) continue;

                    Vec3 anchorPos = Vec3.atCenterOf(check);
                    double distSq = botPosition.distanceToSqr(anchorPos);
                    if (distSq > maxDistanceSq) continue;

                    if (!isReachable(check)) continue;

                    if (isAnchorBehindBot(botPosition, anchorPos, botForwardDirection)) {
                        continue;
                    }

                    double distanceToBot = Math.sqrt(distSq);
                    double distanceToPredictedTarget = anchorPos.distanceTo(predictedTargetPos);
                    double distanceToCurrentTarget = anchorPos.distanceTo(currentTargetPos);

                    boolean isOnOppositeSide = isOnOppositeSideOfTarget(botPosition, predictedTargetPos, anchorPos);
                    boolean isLethalPosition = isLethalPosition(anchorPos, predictedTargetPos, botPosition, target);
                    boolean isTrappingPosition = isTrappingPosition(anchorPos, predictedTargetPos, currentTargetPos);

                    if (isLethalPosition && distanceToBot >= config.getMinSafeDistance()) {
                        if (lethalPos == null ||
                                distanceToPredictedTarget < Vec3.atCenterOf(lethalPos).distanceTo(predictedTargetPos)) {
                            lethalPos = check;
                        }
                    }
                    else if (isOnOppositeSide && distanceToBot >= config.getMinSafeDistance() &&
                            (isTrappingPosition || distanceToPredictedTarget <= 3.0)) {
                        if (smartPos == null ||
                                distanceToPredictedTarget < Vec3.atCenterOf(smartPos).distanceTo(predictedTargetPos)) {
                            smartPos = check;
                        }
                    }
                    else if (distanceToBot >= config.getMinSafeDistance()) {
                        if (safePos == null || distSq < botPosition.distanceToSqr(Vec3.atCenterOf(safePos))) {
                            safePos = check;
                        }
                    }
                    else if (fallbackPos == null || distSq < botPosition.distanceToSqr(Vec3.atCenterOf(fallbackPos))) {
                        fallbackPos = check;
                    }
                }
            }
        }

        if (lethalPos != null) return Optional.of(lethalPos);
        if (smartPos != null) return Optional.of(smartPos);
        if (safePos != null) return Optional.of(safePos);
        return Optional.ofNullable(fallbackPos);
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
            return currentPos.add(velocity.scale(config.getPredictionTicks()));
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

    private boolean isReachable(BlockPos pos) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

        if (botEyes.distanceToSqr(targetPos) > 12 * 12) return false;

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                botEyes,
                targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );

        return level.clip(context).getType() != net.minecraft.world.phys.HitResult.Type.BLOCK;
    }

    public void setConfig(RAPVPConfig config) {
        this.config = config;
    }
}
