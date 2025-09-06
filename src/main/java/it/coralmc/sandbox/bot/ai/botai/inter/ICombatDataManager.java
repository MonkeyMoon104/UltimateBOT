package it.coralmc.sandbox.bot.ai.botai.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ICombatDataManager {
    void updateCombatData(Player target);
    float getLastKnownHealth();
    int getConsecutiveDamageCount();
    long getLastDamageTime();
    Vec3 getTargetVelocity();
    Vec3 getLastTargetPosition();
}