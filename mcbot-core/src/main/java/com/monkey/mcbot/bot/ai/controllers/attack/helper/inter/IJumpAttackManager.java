package com.monkey.mcbot.bot.ai.controllers.attack.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;

public interface IJumpAttackManager {

    void handleJumpAttack(Player bot, LivingEntity target);

    void initiateJumpAttack(Player bot);

    boolean isInJumpAttack();

    void resetJumpState(Player bot);
}
