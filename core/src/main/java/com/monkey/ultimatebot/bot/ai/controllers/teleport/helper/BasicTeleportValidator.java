package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public class BasicTeleportValidator implements ITeleportValidator {

    @Override
    public boolean isSafePosition(ITrainingBot bot, BlockVector pos) {
        Block feet = blockAt(bot, pos);
        Block below = feet.getRelative(0, -1, 0);
        Block head = feet.getRelative(0, 1, 0);

        if (!below.getType().isSolid()) return false;

        if (!feet.isPassable()) return false;
        if (!head.isPassable()) return false;

        return true;
    }

    @Override
    public boolean isSuffocationDamage(ITrainingBot bot) {
        Block feet = bot.getLocation().getBlock();
        Block head = feet.getRelative(0, 1, 0);

        if (!feet.isPassable()) return true;
        if (!head.isPassable()) return true;

        return false;
    }

    private static Block blockAt(ITrainingBot bot, BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }
}
