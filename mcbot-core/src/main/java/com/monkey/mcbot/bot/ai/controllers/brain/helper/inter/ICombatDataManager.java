package com.monkey.mcbot.bot.ai.controllers.brain.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ICombatDataManager {
    void updateCombatData(Player target, boolean allowCombatActions);

    float getLastKnownHealth();

    int getConsecutiveDamageCount();

    long getLastDamageTime();

    Vec3 getTargetVelocity();

    Vec3 getLastTargetPosition();
}
