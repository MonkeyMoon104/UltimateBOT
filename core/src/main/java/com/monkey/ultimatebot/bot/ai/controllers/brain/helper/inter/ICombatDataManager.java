package com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface ICombatDataManager {
    void updateCombatData(Player target, boolean allowCombatActions);

    float getLastKnownHealth();

    int getConsecutiveDamageCount();

    long getLastDamageTime();

    Vector getTargetVelocity();

    @Nullable Vector getLastTargetPosition();
}
