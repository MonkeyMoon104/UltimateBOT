package it.coralmc.sandbox.bot.ai.controllers.rapvp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class AnchorPositionFinder {

    private final Player bot;
    private final Level level;

    public AnchorPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public Optional<BlockPos> findBestAnchorPos(Player target) {
        BlockPos targetPos = target.blockPosition();
        BlockPos botPos = bot.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos check = targetPos.offset(dx, dy, dz);

                    if (check.equals(botPos) || check.equals(botPos.below())) continue;

                    BlockState state = level.getBlockState(check);
                    BlockState below = level.getBlockState(check.below());

                    if (state.canBeReplaced() && below.isSolid() && isReachable(check)) {
                        double distToTarget = bot.position().distanceTo(Vec3.atCenterOf(check));
                        if (distToTarget < bestDistance) {
                            best = check;
                            bestDistance = distToTarget;
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(best);
    }

    private boolean isReachable(BlockPos pos) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

        double distance = botEyes.distanceTo(targetPos);
        if (distance > 12.0) return false;

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                botEyes,
                targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );

        net.minecraft.world.phys.BlockHitResult result = level.clip(context);

        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS ||
                result.getBlockPos().equals(pos);
    }
}