package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SafetyValidator implements ISafetyValidator {

    private final Level level;

    public SafetyValidator(Level level) {
        this.level = level;
    }

    @Override
    public boolean isSafeLandingSpot(BlockPos pos) {
        BlockState stateAt = level.getBlockState(pos);
        BlockState stateAbove = level.getBlockState(pos.above());
        BlockState stateBelow = level.getBlockState(pos.below());

        if (!stateAt.isAir() || !stateAbove.isAir()) {
            return false;
        }

        if (stateBelow.isAir()) {
            return false;
        }

        if (stateBelow.getCollisionShape(level, pos.below()).isEmpty()) {
            return false;
        }

        if (!level.getFluidState(pos.below()).isEmpty()) return false;
        if (!level.getFluidState(pos).isEmpty()) return false;
        if (!level.getFluidState(pos.above()).isEmpty()) return false;

        if (Blocks.CACTUS.equals(stateBelow.getBlock())
                || Blocks.MAGMA_BLOCK.equals(stateBelow.getBlock())
                || Blocks.LAVA.equals(stateBelow.getBlock())
                || Blocks.WATER.equals(stateBelow.getBlock())) {
            return false;
        }

        if (!stateBelow.isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable Vec3 findSafeLandingSpot(BlockPos center) {
        for (int radius = 1; radius <= 3; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -1; y <= 2; y++) {
                        BlockPos checkPos = center.offset(x, y, z);
                        if (isSafeLandingSpot(checkPos)) {
                            return Vec3.atCenterOf(checkPos);
                        }
                    }
                }
            }
        }
        return null;
    }
}
