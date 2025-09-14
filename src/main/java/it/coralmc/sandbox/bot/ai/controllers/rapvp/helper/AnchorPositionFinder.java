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

    public AnchorPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public Optional<BlockPos> findBestAnchorPos(Player target) {
        BlockPos targetPos = target.blockPosition();
        BlockPos botPos = bot.blockPosition();
        Vec3 botPosition = bot.position();
        Vec3 targetPosition = target.position();

        AtomicReference<BlockPos> smartPos = new AtomicReference<>(null);
        AtomicReference<BlockPos> safePos = new AtomicReference<>(null);
        AtomicReference<BlockPos> fallbackPos = new AtomicReference<>(null);
        double maxDistanceSq = 12 * 12;

        int[] offsets = {-2, -1, 0, 1, 2};
        int[] yOffsets = {-1, 0, 1};

        IntStream.range(0, offsets.length).parallel().forEach(i -> {
            int dx = offsets[i];
            for (int dz : offsets) {
                for (int dy : yOffsets) {
                    BlockPos check = targetPos.offset(dx, dy, dz);

                    if (check.equals(botPos) || check.equals(botPos.below())) continue;

                    BlockState state = level.getBlockState(check);
                    BlockState below = level.getBlockState(check.below());

                    if (!state.canBeReplaced() || !below.isSolid()) continue;

                    Vec3 anchorPos = Vec3.atCenterOf(check);
                    double distSq = botPosition.distanceToSqr(anchorPos);
                    if (distSq > maxDistanceSq) continue;

                    if (!isReachable(check)) continue;

                    double distanceToBot = Math.sqrt(distSq);
                    boolean isOnOppositeSide = isOnOppositeSideOfTarget(botPosition, targetPosition, anchorPos);

                    synchronized (this) {
                        if (isOnOppositeSide && distanceToBot >= MIN_SAFE_DISTANCE) {
                            if (smartPos.get() == null || distSq < botPosition.distanceToSqr(Vec3.atCenterOf(smartPos.get()))) {
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

        if (smartPos.get() != null) return Optional.of(smartPos.get());
        if (safePos.get() != null) return Optional.of(safePos.get());
        return Optional.ofNullable(fallbackPos.get());
    }

    private boolean isOnOppositeSideOfTarget(Vec3 botPos, Vec3 targetPos, Vec3 anchorPos) {
        Vec3 botToTarget = targetPos.subtract(botPos).normalize();

        Vec3 targetToAnchor = anchorPos.subtract(targetPos).normalize();

        double dotProduct = botToTarget.dot(targetToAnchor);

        return dotProduct > 0.3;
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