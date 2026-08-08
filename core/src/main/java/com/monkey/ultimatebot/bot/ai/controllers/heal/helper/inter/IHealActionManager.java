package com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IHealActionManager {

    void startHealAction(ITrainingBot bot);

    void updateHealAction(ITrainingBot bot);

    boolean isHealing();

    void resetHealAction();

    boolean canStartNewHeal();

    void applyGoldenAppleEffectsManually(ITrainingBot bot);
}
