package it.coralmc.sandbox.bot.ai.controllers.attack.inter;

import net.minecraft.world.entity.player.Player;

public interface IAttackStrategy {

    void executeAttack(Player bot, Player target);
}