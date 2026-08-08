package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.util.BlockVector;

public interface ITeleportValidator {
    boolean isSafePosition(ITrainingBot bot, BlockVector pos);

    boolean isSuffocationDamage(ITrainingBot bot);
}
