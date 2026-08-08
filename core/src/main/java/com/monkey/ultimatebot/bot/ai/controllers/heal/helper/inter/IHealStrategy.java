package com.monkey.ultimatebot.bot.ai.controllers.heal.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IHealStrategy {

    void executeHeal(ITrainingBot bot);

    boolean shouldHeal(ITrainingBot bot);
}
