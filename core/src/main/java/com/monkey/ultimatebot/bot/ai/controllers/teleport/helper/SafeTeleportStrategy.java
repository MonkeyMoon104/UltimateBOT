package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportValidator;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class SafeTeleportStrategy implements ITeleportStrategy {

    private final ITeleportValidator validator;

    public SafeTeleportStrategy(ITeleportValidator validator) {
        this.validator = validator;
    }

    @Override
    public @Nullable Vector findTeleportPosition(ITrainingBot bot, @Nullable Player target) {
        if (target == null) return null;
        Vector base = java.util.Objects.requireNonNull(target.getLocation()).toVector();
        Vector[] offsets = {
            base.clone().add(new Vector(2, 0, 0)),
            base.clone().add(new Vector(-2, 0, 0)),
            base.clone().add(new Vector(0, 0, 2)),
            base.clone().add(new Vector(0, 0, -2)),
            base.clone().add(new Vector(2, 0, 2)),
            base.clone().add(new Vector(-2, 0, -2))
        };

        for (Vector candidate : offsets) {
            BlockVector pos = blockAt(candidate);
            if (validator.isSafePosition(bot, pos)) {
                return centerOf(pos);
            }
        }

        return null;
    }

    private static BlockVector blockAt(Vector position) {
        return new BlockVector((int) Math.floor(position.getX()), (int) Math.floor(position.getY()), (int)
                Math.floor(position.getZ()));
    }

    private static Vector centerOf(BlockVector position) {
        return new Vector(position.getBlockX() + 0.5D, position.getBlockY() + 0.5D, position.getBlockZ() + 0.5D);
    }
}
