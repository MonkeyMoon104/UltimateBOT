package it.coralmc.sandbox.bot.ai.controllers.rapvp.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class AnchorPositionFinder {

    private final Player bot;
    private final Level level;

    private static final double MIN_SAFE_DISTANCE = 4.0;
    private static final double PREDICTION_TICKS = 10.0;
    private static final double MIN_MOVEMENT_SPEED = 0.1;

    public AnchorPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public Optional<BlockPos> findBestAnchorPos(Player target) {
        Vec3 botPosition = bot.position();
        Vec3 currentTargetPos = target.position();

        Vec3 predictedTargetPos = predictTargetPosition(target);
        BlockPos targetPos = BlockPos.containing(predictedTargetPos);

        AtomicReference<BlockPos> lethaltPos = new AtomicReference<>(null);
        AtomicReference<BlockPos> smartPos = new AtomicReference<>(null);
        AtomicReference<BlockPos> safePos = new AtomicReference<>(null);
        AtomicReference<BlockPos> fallbackPos = new AtomicReference<>(null);
        double maxDistanceSq = 12 * 12;

        int[] offsets = {-3, -2, -1, 0, 1, 2, 3};
        int[] yOffsets = {-2, -1, 0, 1};

        IntStream.range(0, offsets.length).parallel().forEach(i -> {
            int dx = offsets[i];
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

                    double distanceToBot = Math.sqrt(distSq);
                    double distanceToPredictedTarget = anchorPos.distanceTo(predictedTargetPos);
                    double distanceToCurrentTarget = anchorPos.distanceTo(currentTargetPos);

                    boolean isOnOppositeSide = isOnOppositeSideOfTarget(botPosition, predictedTargetPos, anchorPos);
                    boolean isLethalPosition = isLethalPosition(anchorPos, predictedTargetPos, botPosition, target);
                    boolean isTrappingPosition = isTrappingPosition(anchorPos, predictedTargetPos, currentTargetPos);

                    synchronized (this) {
                        if (isLethalPosition && distanceToBot >= MIN_SAFE_DISTANCE) {
                            if (lethaltPos.get() == null ||
                                    distanceToPredictedTarget < Vec3.atCenterOf(lethaltPos.get()).distanceTo(predictedTargetPos)) {
                                lethaltPos.set(check);
                            }
                        }
                        else if (isOnOppositeSide && distanceToBot >= MIN_SAFE_DISTANCE &&
                                (isTrappingPosition || distanceToPredictedTarget <= 3.0)) {
                            if (smartPos.get() == null ||
                                    distanceToPredictedTarget < Vec3.atCenterOf(smartPos.get()).distanceTo(predictedTargetPos)) {
                                smartPos.set(check);
                            }
                        }
                        else if (distanceToBot >= MIN_SAFE_DISTANCE) {
                            if (safePos.get() == null || distSq < botPosition.distanceToSqr(Vec3.atCenterOf(safePos.get()))) {
                                safePos.set(check);
                            }
                        }
                        else if (fallbackPos.get() == null || distSq < botPosition.distanceToSqr(Vec3.atCenterOf(fallbackPos.get()))) {
                            fallbackPos.set(check);
                        }
                    }
                }
            }
        });

        if (lethaltPos.get() != null) return Optional.of(lethaltPos.get());
        if (smartPos.get() != null) return Optional.of(smartPos.get());
        if (safePos.get() != null) return Optional.of(safePos.get());
        return Optional.ofNullable(fallbackPos.get());
    }

    private Vec3 predictTargetPosition(Player target) {
        Vec3 currentPos = target.position();
        Vec3 velocity = target.getDeltaMovement();

        if (velocity.horizontalDistance() > MIN_MOVEMENT_SPEED) {
            return currentPos.add(velocity.scale(PREDICTION_TICKS));
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

        if (targetMovement.length() > MIN_MOVEMENT_SPEED) {
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
}