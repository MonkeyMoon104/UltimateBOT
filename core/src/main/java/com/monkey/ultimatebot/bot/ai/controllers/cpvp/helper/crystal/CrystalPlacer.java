package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.access.world.RayTraceAccess;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
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
        }

        try {
            ItemStack crystalStack = inventoryController.getItem(BotInventoryController.CRYSTAL_SLOT);
            if (!MaterialCatalog.is(crystalStack.getType(), "END_CRYSTAL")) return false;
            Location hit = new Location(
                    bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 1.0D, pos.getBlockZ() + 0.5D);
            boolean consumed = NMSBridgeManager.get()
                    .useItemOnBlock(
                            bot.asBukkitPlayer(),
                            crystalStack,
                            blockAt(pos),
                            BlockFace.UP,
                            hit,
                            EquipmentSlotKind.HAND);

            if (consumed) {
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
        Location target =
                new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
        Vector direction = target.toVector().subtract(eye.toVector());
        double distance = direction.length();
        if (distance < 1.0E-6D) return true;
        return RayTraceAccess.clearOrHits(bot.getWorld(), eye, direction, distance, blockAt(pos));
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
}
