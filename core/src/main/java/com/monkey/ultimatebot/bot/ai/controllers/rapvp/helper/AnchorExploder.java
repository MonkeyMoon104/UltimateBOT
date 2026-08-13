package com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.combat.BotExplosionType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.combat.BotExplosionContext;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

public class AnchorExploder {

    private final ITrainingBot bot;
    private final BotInventoryController inventory;
    private final BotRotationController rotation;

    public AnchorExploder(ITrainingBot bot, BotInventoryController inventory, BotRotationController rotation) {
        this.bot = bot;
        this.inventory = inventory;
        this.rotation = rotation;
    }

    public boolean explodeAnchor(BlockVector anchorPos) {
        try {
            Block block = blockAt(anchorPos);
            if (!(block.getBlockData() instanceof RespawnAnchor)) {
                return false;
            }
            RespawnAnchor anchorData = (RespawnAnchor) block.getBlockData();
            if (anchorData.getCharges() <= 0) {
                return false;
            }
            inventory.switchToEmptySlot();
            rotation.lookAt(new Vector(anchorPos.getBlockX() + 0.5D, anchorPos.getBlockY() + 0.5D, anchorPos.getBlockZ() + 0.5D));
            return tryManualExplosion(anchorPos);
        } catch (Exception e) {
            UltimateBot.getInstance().getLogger().warning("Error while exploding anchor: " + e.getMessage());
            return false;
        }
    }

    private boolean tryManualExplosion(BlockVector anchorPos) {
        try {
            boolean blockDamage = shouldDamageBlocks();
            Location location = centerOf(anchorPos);
            return BotExplosionContext.execute(
                    bot,
                    BotExplosionType.RESPAWN_ANCHOR,
                    location,
                    blockDamage,
                    resolvedBlockDamage -> {
                        blockAt(anchorPos).setType(Material.AIR, false);
                        NMSBridgeManager.get().explode(location, bot.asBukkitPlayer(), 3.5F, resolvedBlockDamage);
                        bot.swingMainHand();
                        return true;
                    },
                    false);
        } catch (Exception e) {
            UltimateBot.getInstance().getLogger().warning("Error during manual explosion: " + e.getMessage());
            return false;
        }
    }

    private boolean shouldDamageBlocks() {
        return bot.getBrainController() != null
                && bot.getBrainController().getBotOptions().canExplosionDamageBlocks();
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private Location centerOf(BlockVector pos) {
        return new Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }
}
