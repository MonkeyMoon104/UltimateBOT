package com.monkey.ultimatebot.combat.mode;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

final class TridentUtilityActions {
    private @Nullable Location waterLocation;

    void reset() {
        waterLocation = null;
    }

    void selectWater(CombatModeContext context) {
        context.inventory().switchToSlot(TridentLoadout.WATER_SLOT);
    }

    boolean placeWater(CombatModeContext context) {
        Location placement = Objects.requireNonNull(context.bukkitBot().getLocation(), "bot location")
                .getBlock()
                .getLocation();
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
        return Math.clamp((target.getY() - context.bot().getY()) * 0.22D + 0.24D, 0.12D, 0.52D);
    }

    void restoreWater(CombatModeContext context) {
        if (waterLocation != null) {
            context.restoreCombatBlock(waterLocation);
            waterLocation = null;
        }
    }
}
