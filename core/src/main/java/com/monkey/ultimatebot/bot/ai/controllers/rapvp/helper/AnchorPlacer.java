package com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class AnchorPlacer {

    private final ITrainingBot bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;

    public AnchorPlacer(ITrainingBot bot, BotInventoryController inventory, BotRotationController rotation) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
    }

    public boolean placeAnchor(BlockVector pos) {
        try {
            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || !MaterialCatalog.is(stack.getType(), "RESPAWN_ANCHOR")) return false;
            BlockFace bestFace = findBestPlacementFace(pos);
            if (bestFace == null) bestFace = BlockFace.UP;
            BlockVector adjacentPos = relative(pos, bestFace.getOppositeFace());
            Location hit = centerOf(adjacentPos).add(bestFace.getModX() * 0.5D, bestFace.getModY() * 0.5D, bestFace.getModZ() * 0.5D);
            boolean consumed = NMSBridgeManager.get()
                    .useItemOnBlock(bot.asBukkitPlayer(), stack, blockAt(adjacentPos), bestFace, hit, EquipmentSlotKind.HAND);
            if (consumed) {
                rotation.lookAt(new Vector(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()));
                bot.swingMainHand();
                inventory.onItemUsed(BotInventoryController.ANCHOR_SLOT);
                return true;
            }
            return false;
        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Anchor placement failed -> " + e.getMessage());
            return false;
        }
    }

    private @Nullable BlockFace findBestPlacementFace(BlockVector targetPos) {
        for (BlockFace face : new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN}) {
            BlockVector adjacentPos = relative(targetPos, face.getOppositeFace());
            Block adjacent = blockAt(adjacentPos);
            if (adjacent.getType().isSolid() && bot.getLocation().distance(centerOf(adjacentPos)) <= 6.5D) {
                return face;
            }
        }
        return null;
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private Location centerOf(BlockVector pos) {
        return new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }

    private static BlockVector relative(BlockVector pos, BlockFace face) {
        return new BlockVector(
                pos.getBlockX() + face.getModX(), pos.getBlockY() + face.getModY(), pos.getBlockZ() + face.getModZ());
    }
}
