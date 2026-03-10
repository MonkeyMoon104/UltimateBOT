package com.monkey.mcbot.bot.ai.controllers.movement.helper.interf;

import net.minecraft.world.phys.Vec3;

public interface ICombatStateManager {
    void setUnderFire(boolean underFire);
    void onDamageReceived();
    boolean isUnderFire();
    boolean hasRecentDamage();
    int getConsecutiveHits();
    void resetCombatState();
    Vec3 getTargetVelocity();
    void updateCombatData(net.minecraft.world.entity.player.Player target);
}