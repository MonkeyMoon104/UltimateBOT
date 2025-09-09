package it.coralmc.sandbox.bot.ai.controllers.heal.helper;

import net.minecraft.world.entity.player.Player;

public interface IHealStrategy {

    void executeHeal(Player bot);

    boolean shouldHeal(Player bot);
}