package it.coralmc.sandbox.bot.ai.controllers.brain.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ICombatStrategyExecutor {
    void executeCombatStrategy(Player target);
    boolean executeAggressive(Player target, double distance);
    boolean executeDefensive(Player target, double distance);
    boolean executeRepositioning(Player target, double distance);
    boolean executeAnchorSetup(Player target, double distance);
    boolean executeCrystalSetup(Player target, double distance);
    boolean executeRetreating(Player target, double distance);
    Vec3 getStrafeDirection(Player target);
    void moveToTarget(Player target, double targetDistance);
    void basicFollowBehavior(Player target);
}