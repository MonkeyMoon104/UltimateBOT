package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

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

public class CrystalPlacer {

    private final ITrainingBot bot;
    private final BotInventoryController inventoryController;

    public CrystalPlacer(ITrainingBot bot, BotInventoryController inventoryController) {
        this.bot = bot;
        this.inventoryController = inventoryController;
    }

    public boolean placeCrystal(BlockVector pos) {
        if (!inventoryController.hasItem(Material.END_CRYSTAL)) return false;
        if (!inventoryController.isHoldingCrystal()) {
            inventoryController.switchToCrystal();
            return false;
        }

        try {
            ItemStack crystalStack = inventoryController.getCurrentItem();
            if (crystalStack.getType() != Material.END_CRYSTAL) return false;
            Location hit = new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 1.0D, pos.getBlockZ() + 0.5D);
            boolean consumed = NMSBridgeManager.get()
                    .useItemOnBlock(bot.asBukkitPlayer(), crystalStack, blockAt(pos), BlockFace.UP, hit, EquipmentSlot.HAND);

            if (consumed) {
                bot.swingMainHand();
                inventoryController.onItemUsed(BotInventoryController.CRYSTAL_SLOT);
                return true;
            }
        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Crystal placement failed -> " + e.getMessage());
        }
        return false;
    }

    public boolean hasLineOfSight(BlockVector pos) {
        Location eye = bot.asBukkitPlayer().getEyeLocation();
        Location target = new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
        Vector direction = target.toVector().subtract(eye.toVector());
        double distance = direction.length();
        if (distance < 1.0E-6D) return true;
        RayTraceResult result = bot.getWorld().rayTraceBlocks(eye, direction.normalize(), distance, FluidCollisionMode.NEVER, true);
        return result == null || result.getHitBlock() == null || blockEquals(result.getHitBlock(), pos);
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private static boolean blockEquals(Block block, BlockVector pos) {
        return block.getX() == pos.getBlockX() && block.getY() == pos.getBlockY() && block.getZ() == pos.getBlockZ();
    }
}
