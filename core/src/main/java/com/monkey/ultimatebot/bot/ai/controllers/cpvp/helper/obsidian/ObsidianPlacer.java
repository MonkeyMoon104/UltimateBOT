package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ObsidianPlacer {

    private final Player bot;
    private final BotInventoryController inventoryController;
    private final Level level;

    public ObsidianPlacer(Player bot, BotInventoryController inventoryController, Level level) {
        this.bot = bot;
        this.inventoryController = inventoryController;
        this.level = level;
    }

    public boolean placeObsidianAt(BlockPos pos) {
        try {
            ItemStack obsidianStack = inventoryController.getCurrentItem();
            if (!Items.OBSIDIAN.equals(obsidianStack.getItem())) return false;

            Direction bestFace = findBestPlacementFace(pos);
            if (bestFace == null) return false;

            BlockPos adjacentPos = pos.relative(bestFace.getOpposite());

            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(adjacentPos).relative(bestFace, 0.5), bestFace, adjacentPos, false);

            InteractionResult result =
                    NMSBridgeManager.get().useItemOnBlock(bot, obsidianStack, hitResult, InteractionHand.MAIN_HAND);

            if (result.consumesAction()) {
                bot.swing(InteractionHand.MAIN_HAND);

                inventoryController.onItemUsed(BotInventoryController.OBSIDIAN_SLOT);

                return true;
            }

        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Obsidian placement failed -> " + e.getMessage());
        }

        return false;
    }

    private Direction findBestPlacementFace(BlockPos targetPos) {
        Vec3 botPos = bot.position();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = targetPos.relative(direction.getOpposite());
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.isSolidRender() && !adjacentState.isAir()) {
                double distance = botPos.distanceTo(Vec3.atCenterOf(adjacentPos));
                if (distance <= 6.5) return direction;
            }
        }

        return null;
    }

    public boolean hasLineOfSight(BlockPos pos) {
        Vec3 botEyes = bot.getEyePosition(1.0F);
        Vec3 targetPos = Vec3.atCenterOf(pos);

        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                botEyes,
                targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot);

        net.minecraft.world.phys.BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                || result.getBlockPos().equals(pos);
    }
}
