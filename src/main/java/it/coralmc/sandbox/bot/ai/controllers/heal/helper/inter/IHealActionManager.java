package it.coralmc.sandbox.bot.ai.controllers.heal.helper.inter;

import net.minecraft.world.entity.player.Player;

public interface IHealActionManager {

    void startHealAction(Player bot);

    void updateHealAction(Player bot);

    boolean isHealing();

    void resetHealAction();

    boolean canStartNewHeal();
    void applyGoldenAppleEffectsManually(Player bot);
}