package com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface ICombatStrategyExecutor {
    void executeCombatStrategy(Player target);

    void executeMeleeCombat(LivingEntity target);

    boolean executeAggressive(Player target, double distance);

    boolean executeDefensive(Player target, double distance);

    boolean executeRepositioning(Player target, double distance);

    boolean executeAnchorSetup(Player target, double distance);

    boolean executeCrystalSetup(Player target, double distance);

    boolean executeRetreating(Player target, double distance);

    Vector getStrafeDirection(Player target);

    void moveToTarget(Player target, double targetDistance);

    void basicFollowBehavior(Player target);
}
