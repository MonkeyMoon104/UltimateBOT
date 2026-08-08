package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationGetter;

public class RotationGetter implements IRotationGetter {

    @Override
    public float getCurrentYaw(ITrainingBot bot) {
        return bot.getYaw();
    }

    @Override
    public float getCurrentPitch(ITrainingBot bot) {
        return bot.getPitch();
    }
}
