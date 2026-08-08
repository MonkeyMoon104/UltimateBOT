package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class ObsidianPlacer {

    private final ITrainingBot bot;
    private final BotInventoryController inventoryController;

    public ObsidianPlacer(ITrainingBot bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.inventoryController = inventoryController;
    }

    public boolean placeObsidianAt(BlockVector pos) {
        try {
            ItemStack obsidianStack = inventoryController.getCurrentItem();
            if (obsidianStack.getType() != Material.OBSIDIAN) return false;
            BlockFace bestFace = findBestPlacementFace(pos);
            if (bestFace == null) return false;
            BlockVector adjacentPos = relative(pos, bestFace.getOppositeFace());
            Location hit = centerOf(adjacentPos).add(bestFace.getModX() * 0.5D, bestFace.getModY() * 0.5D, bestFace.getModZ() * 0.5D);
            boolean consumed = NMSBridgeManager.get()
                    .useItemOnBlock(bot.asBukkitPlayer(), obsidianStack, blockAt(adjacentPos), bestFace, hit, EquipmentSlot.HAND);
            if (consumed) {
                bot.swingMainHand();
                inventoryController.onItemUsed(BotInventoryController.OBSIDIAN_SLOT);
                return true;
            }
        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Obsidian placement failed -> " + e.getMessage());
        }
        return false;
    }

    private @Nullable BlockFace findBestPlacementFace(BlockVector targetPos) {
        for (BlockFace face : new BlockFace[] {BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST}) {
            BlockVector adjacentPos = relative(targetPos, face.getOppositeFace());
            Block adjacent = blockAt(adjacentPos);
            if (adjacent.getType().isSolid() && bot.getLocation().distance(centerOf(adjacentPos)) <= 6.5D) {
                return face;
            }
        }
        return null;
    }

    public boolean hasLineOfSight(BlockVector pos) {
        Location eye = bot.asBukkitPlayer().getEyeLocation();
        Location target = centerOf(pos);
        Vector direction = target.toVector().subtract(eye.toVector());
        double distance = direction.length();
        if (distance < 1.0E-6D) return true;
        RayTraceResult result = bot.getWorld().rayTraceBlocks(eye, direction.normalize(), distance, FluidCollisionMode.NEVER, true);
        return result == null || result.getHitBlock() == null || blockEquals(result.getHitBlock(), pos);
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

    private static boolean blockEquals(Block block, BlockVector pos) {
        return block.getX() == pos.getBlockX() && block.getY() == pos.getBlockY() && block.getZ() == pos.getBlockZ();
    }
}
