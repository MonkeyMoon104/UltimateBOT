package com.monkey.mcbot.bot.ai.controllers.attack.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;

public interface IAttackStrategy {

    void executeAttack(Player bot, LivingEntity target);
}
