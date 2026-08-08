package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;

public interface IRotationGetter {

    float getCurrentYaw(ITrainingBot bot);

    float getCurrentPitch(ITrainingBot bot);
}
