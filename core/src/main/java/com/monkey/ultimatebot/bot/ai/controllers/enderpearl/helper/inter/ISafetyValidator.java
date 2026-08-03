package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ISafetyValidator {

    boolean isSafeLandingSpot(BlockPos pos);

    @Nullable Vec3 findSafeLandingSpot(BlockPos center);
}
