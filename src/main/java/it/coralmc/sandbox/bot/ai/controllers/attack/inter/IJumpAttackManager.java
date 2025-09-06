package it.coralmc.sandbox.bot.ai.controllers.attack.inter;

import net.minecraft.world.entity.player.Player;

public interface IJumpAttackManager {

    void handleJumpAttack(Player bot, Player target);

    void initiateJumpAttack(Player bot);

    boolean isInJumpAttack();

    void resetJumpState(Player bot);
}