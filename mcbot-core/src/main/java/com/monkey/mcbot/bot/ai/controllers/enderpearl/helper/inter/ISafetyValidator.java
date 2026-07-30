package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface ISafetyValidator {

    boolean isSafeLandingSpot(BlockPos pos);

    Vec3 findSafeLandingSpot(BlockPos center);
}
