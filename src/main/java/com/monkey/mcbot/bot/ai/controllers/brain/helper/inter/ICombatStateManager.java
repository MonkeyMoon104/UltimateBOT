package com.monkey.mcbot.bot.ai.controllers.brain.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface ICombatStateManager {

    enum CombatState {
        AGGRESSIVE,
        DEFENSIVE,
        REPOSITIONING,
        ANCHOR_SETUP,
        CRYSTAL_SETUP,
        RETREATING
    }

    void updateCombatState(Player target);
    CombatState getCurrentState();
    void onStateChange(Player target);
    boolean shouldAttemptAnchor(Player target, long currentTime);
    boolean shouldReposition(Player target, double distance);
}