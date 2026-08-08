package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IRotationApplier {

    void applyRotation(ITrainingBot bot, float yaw, float pitch);

    void resetRotation(ITrainingBot bot);
}
