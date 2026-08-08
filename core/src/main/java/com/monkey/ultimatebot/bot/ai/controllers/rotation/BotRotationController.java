package com.monkey.ultimatebot.bot.ai.controllers.rotation;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.AngleNormalizer;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.RotationApplier;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.RotationCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.RotationGetter;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IAngleNormalizer;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationApplier;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationCalculator;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationGetter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class BotRotationController {

    private final ITrainingBot bot;

    private final IRotationCalculator rotationCalculator;
    private final IAngleNormalizer angleNormalizer;
    private final IRotationApplier rotationApplier;
    private final IRotationGetter rotationGetter;

    public BotRotationController(ITrainingBot bot) {
        this.bot = bot;

        this.rotationCalculator = new RotationCalculator();
        this.angleNormalizer = new AngleNormalizer();
        this.rotationApplier = new RotationApplier(angleNormalizer);
        this.rotationGetter = new RotationGetter();
    }

    public void updateRotation(LivingEntity target) {
        float[] rotation = rotationCalculator.calculateRotationToTarget(bot, target);
        rotationApplier.applyRotation(bot, rotation[0], rotation[1]);
    }

    public void setInstantRotation(LivingEntity target) {
        updateRotation(target);
    }

    public float getCurrentYaw() {
        return rotationGetter.getCurrentYaw(bot);
    }

    public float getCurrentPitch() {
        return rotationGetter.getCurrentPitch(bot);
    }

    public void setRotation(float yaw, float pitch) {
        rotationApplier.applyRotation(bot, yaw, pitch);
    }

    public void lookAt(double x, double y, double z) {
        float[] rotation = rotationCalculator.calculateRotationToPosition(bot, x, y, z);
        rotationApplier.applyRotation(bot, rotation[0], rotation[1]);
    }

    public void lookAt(Vector targetPos) {
        float[] rotation = rotationCalculator.calculateRotationToPosition(bot, targetPos);
        rotationApplier.applyRotation(bot, rotation[0], rotation[1]);
    }

    public void resetRotation() {
        rotationApplier.resetRotation(bot);
    }
}
