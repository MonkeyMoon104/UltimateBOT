package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class BesideTargetStrategy implements ITeleportStrategy {

    private final double offset;

    public BesideTargetStrategy(double offset) {
        this.offset = offset;
    }

    @Override
    public @Nullable Vector findTeleportPosition(ITrainingBot bot, @Nullable Player target) {
        if (target == null) return null;
        Vector pos = java.util.Objects.requireNonNull(target.getLocation()).toVector();
        return pos.add(new Vector(offset, 0, 0));
    }
}
