package com.monkey.mcbot.bot.ai.controllers.attack.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IAttackStrategy {

    void executeAttack(Player bot, Player target);
}