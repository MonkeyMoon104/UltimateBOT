package com.monkey.mcbot.bot.ai.controllers.attack.helper.inter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IAttackExecutor {

    void performCriticalAttack(Player bot, LivingEntity target);

    void performNormalAttack(Player bot, LivingEntity target);
}
