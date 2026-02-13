package com.monkey.mcbot.bot.ai.controllers.rotation.helper;

import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IAngleNormalizer;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationApplier;
import net.minecraft.world.entity.player.Player;

public class RotationApplier implements IRotationApplier {

    private final IAngleNormalizer angleNormalizer;

    public RotationApplier(IAngleNormalizer angleNormalizer) {
        this.angleNormalizer = angleNormalizer;
    }

    @Override
    public void applyRotation(Player bot, float yaw, float pitch) {
        yaw = angleNormalizer.normalizeAngle(yaw);
        pitch = angleNormalizer.normalizePitch(pitch);

        bot.setYRot(yaw);
        bot.yHeadRot = yaw;
        bot.yBodyRot = yaw;
        bot.setXRot(pitch);
    }

    @Override
    public void resetRotation(Player bot) {
        applyRotation(bot, 0f, 0f);
    }
}