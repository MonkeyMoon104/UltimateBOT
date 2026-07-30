package com.monkey.mcbot.bot.ai.controllers.heal.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IHealStrategy {

    void executeHeal(Player bot);

    boolean shouldHeal(Player bot);
}
