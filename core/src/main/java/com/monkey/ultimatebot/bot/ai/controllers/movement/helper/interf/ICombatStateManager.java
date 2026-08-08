package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface ICombatStateManager {
    void setUnderFire(boolean underFire);

    void onDamageReceived();

    boolean isUnderFire();

    boolean hasRecentDamage();

    int getConsecutiveHits();

    void resetCombatState();

    Vector getTargetVelocity();

    void updateCombatData(LivingEntity target);
}
