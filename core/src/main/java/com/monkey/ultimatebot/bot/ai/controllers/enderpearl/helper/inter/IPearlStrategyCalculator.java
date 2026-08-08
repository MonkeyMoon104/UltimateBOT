package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface IPearlStrategyCalculator {

    enum PearlStrategy {
        ESCAPE,
        REPOSITION_LOW,
        MELEE_DISENGAGE,
        ANCHOR_POSITION,
        AGGRESSIVE_CLOSE,
        COMBO_ESCAPE
    }

    PearlStrategy determineOptimalStrategy(
            Player bot,
            Player target,
            boolean wasRecentlyDamaged,
            int damageComboCount,
            long comboStartTime,
            int repositionPearlCooldown,
            int aggressivePearlCooldown);

    boolean shouldUsePearlForStrategy(
            PearlStrategy strategy,
            Player bot,
            Player target,
            boolean wasRecentlyDamaged,
            long lastEmergencyPearl,
            int repositionPearlCooldown,
            int aggressivePearlCooldown);

    @Nullable Vector calculateTargetForStrategy(
            PearlStrategy strategy, Player bot, Player target, @Nullable Vector predictedTargetMovement);
}
