package com.monkey.ultimatebot.combat.mode.trident;

import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.access.block.BlockPassableAccess;
import com.monkey.ultimatebot.access.entity.EntityCoordsAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class TridentUtilityActions {
    private static final int MAX_WATER_DROP_SEARCH = 6;

    private @Nullable Location waterLocation;

    void reset() {
        waterLocation = null;
    }

    void selectWater(CombatModeContext context) {
        context.inventory().switchToSlot(TridentLoadout.WATER_SLOT);
    }

    boolean placeWater(CombatModeContext context) {
        Location origin = Objects.requireNonNull(context.bukkitBot().getLocation(), "bot location");
        Location placement = findSupportedWaterPlacement(origin);
        if (placement == null) {
            return false;
        }
        boolean placed =
                context.placeCombatBlock(placement, Material.WATER, Material.WATER_BUCKET, TridentLoadout.WATER_SLOT);
        if (placed) {
            waterLocation = placement;
            context.actions().swingMainHand();
        }
        return placed;
    }

    boolean isWaterPrepared() {
        return waterLocation != null;
    }

    double riptideVerticalVelocity(CombatModeContext context, LivingEntity target) {
        return Math.min(
                0.52D,
                Math.max(
                        0.12D,
                        (EntityCoordsAccess.getY(target) - context.motion().botY()) * 0.22D + 0.24D));
    }

    void restoreWater(CombatModeContext context) {
        if (waterLocation != null) {
            context.restoreCombatBlock(waterLocation);
            waterLocation = null;
        }
    }

    private static @Nullable Location findSupportedWaterPlacement(Location origin) {
        Block start = origin.getBlock();
        for (int dy = 0; dy <= MAX_WATER_DROP_SEARCH; dy++) {
            Block candidate = start.getRelative(0, -dy, 0);
            if (!BlockPassableAccess.isPassable(candidate) || candidate.isLiquid()) {
                continue;
            }
            Material below = candidate.getRelative(BlockFace.DOWN).getType();
            if (below.isSolid() || MaterialCatalog.is(below, "COBWEB")) {
                return candidate.getLocation();
            }
        }
        return null;
    }
}
