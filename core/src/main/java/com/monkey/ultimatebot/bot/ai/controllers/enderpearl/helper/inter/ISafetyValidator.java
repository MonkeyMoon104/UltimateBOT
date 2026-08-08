package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface ISafetyValidator {

    boolean isSafeLandingSpot(BlockVector pos);

    @Nullable Vector findSafeLandingSpot(BlockVector center);
}
