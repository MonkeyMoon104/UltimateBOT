package it.coralmc.sandbox.bot.ai.controllers.attack.inter;

import net.minecraft.world.entity.player.Player;

public interface IAttackExecutor {

    void performCriticalAttack(Player bot, Player target);

    void performNormalAttack(Player bot, Player target);
}