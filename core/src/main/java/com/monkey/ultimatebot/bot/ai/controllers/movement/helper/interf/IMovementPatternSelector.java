package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.MovementPattern;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface IMovementPatternSelector {
    MovementPattern selectOptimalPattern(
            LivingEntity target,
            double targetDistance,
            Vector botPos,
            Vector targetPos,
            boolean isUnderFire,
            int consecutiveHits);

    void setMovementPattern(MovementPattern pattern);

    MovementPattern getCurrentPattern();

    boolean shouldChangePattern();
}
