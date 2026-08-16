package com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.compat.RespawnAnchorAccess;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class AnchorCharger {

    private final ITrainingBot bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;

    public AnchorCharger(ITrainingBot bot, BotInventoryController inventory, BotRotationController rotation) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
    }

    public boolean chargeAnchor(BlockVector anchorPos) {
        try {
            ItemStack stack = inventory.getCurrentItem();
            if (stack == null || stack.getType() != Material.GLOWSTONE) return false;
            Block block = blockAt(anchorPos);
            if (!RespawnAnchorAccess.chargeToMaximum(block)) {
                return false;
            }
            NMSBridgeManager.get()
                    .playSound(centerOf(anchorPos), "block.respawn_anchor.charge", "blocks", 1.0F, 1.0F);
            rotation.lookAt(new Vector(anchorPos.getBlockX(), anchorPos.getBlockY(), anchorPos.getBlockZ()));
            bot.swingMainHand();
            inventory.onItemUsed(BotInventoryController.GLOW_SLOT);
            return true;
        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Anchor charge failed -> " + e.getMessage());
            return false;
        }
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private Location centerOf(BlockVector pos) {
        return new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }
}
