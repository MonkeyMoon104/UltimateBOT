package com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IAttackStrategy {

    void executeAttack(Player bot, LivingEntity target);
}
