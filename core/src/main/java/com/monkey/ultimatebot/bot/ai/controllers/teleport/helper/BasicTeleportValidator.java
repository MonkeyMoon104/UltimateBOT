package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import com.monkey.ultimatebot.access.block.BlockPassableAccess;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public class BasicTeleportValidator implements ITeleportValidator {

    @Override
    public boolean isSafePosition(ITrainingBot bot, BlockVector pos) {
        Block feet = blockAt(bot, pos);
        Block below = feet.getRelative(0, -1, 0);
        Block head = feet.getRelative(0, 1, 0);

        if (!below.getType().isSolid()) return false;

        if (!BlockPassableAccess.isPassable(feet)) return false;
        if (!BlockPassableAccess.isPassable(head)) return false;

        return true;
    }

    @Override
    public boolean isSuffocationDamage(ITrainingBot bot) {
        Block feet = bot.getLocation().getBlock();
        Block head = feet.getRelative(0, 1, 0);

        if (!BlockPassableAccess.isPassable(feet)) return true;
        if (!BlockPassableAccess.isPassable(head)) return true;

        return false;
    }

    private static Block blockAt(ITrainingBot bot, BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
}
