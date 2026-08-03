package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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

    @Nullable Vec3 calculateTargetForStrategy(
            PearlStrategy strategy, Player bot, Player target, @Nullable Vec3 predictedTargetMovement);
}
