package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IAngleNormalizer;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationApplier;

public class RotationApplier implements IRotationApplier {

    private final IAngleNormalizer angleNormalizer;

    public RotationApplier(IAngleNormalizer angleNormalizer) {
        this.angleNormalizer = angleNormalizer;
    }

    @Override
    public void applyRotation(ITrainingBot bot, float yaw, float pitch) {
        yaw = angleNormalizer.normalizeAngle(yaw);
        pitch = angleNormalizer.normalizePitch(pitch);

        bot.setRotation(yaw, pitch);
    }

    @Override
    public void resetRotation(ITrainingBot bot) {
        applyRotation(bot, 0f, 0f);
    }
}
