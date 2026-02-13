package com.monkey.mcbot.bot.ai.controllers.rapvp.helper;

import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.rotation.BotRotationController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AnchorPlacer {

    private final Player bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;
    private final Level level;

    public AnchorPlacer(Player bot, BotInventoryController inventory, BotRotationController rotation, Level level) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
        this.level = level;
    }

    public boolean placeAnchor(BlockPos pos) {
        try {
            if (!isReachable(pos)) return false;
            if (!hasLineOfSight(pos)) {
                return false;
            }

            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || stack.getItem() != Items.RESPAWN_ANCHOR) return false;

            Direction bestFace = findBestPlacementFace(pos);
            if (bestFace == null) bestFace = Direction.UP;

            BlockPos adjacentPos = pos.relative(bestFace.getOpposite());

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(adjacentPos).relative(bestFace, 0.5),
                    bestFace,
                    adjacentPos,
                    false
            );

            stack.useOn(new net.minecraft.world.item.context.UseOnContext(bot, InteractionHand.MAIN_HAND, hitResult));
            rotation.lookAt(Vec3.atLowerCornerOf(pos));
            bot.swing(InteractionHand.MAIN_HAND);

            inventory.onItemUsed(BotInventoryController.ANCHOR_SLOT);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Direction findBestPlacementFace(BlockPos targetPos) {
        Vec3 botPos = bot.position();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = bot.level().getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN}) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = bot.level().getBlockState(adjacentPos);

            if (adjacentState.isSolid() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        return null;
    }

    private boolean hasLineOfSight(BlockPos pos) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

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