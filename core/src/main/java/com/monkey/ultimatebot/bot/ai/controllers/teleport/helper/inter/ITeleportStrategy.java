package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface ITeleportStrategy {
    @Nullable Vector findTeleportPosition(ITrainingBot bot, @Nullable Player target);
}
