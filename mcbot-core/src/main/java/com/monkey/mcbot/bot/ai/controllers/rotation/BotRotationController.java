package com.monkey.mcbot.bot.ai.controllers.rotation;

import com.monkey.mcbot.bot.ai.controllers.rotation.helper.AngleNormalizer;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.RotationApplier;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.RotationCalculator;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.RotationGetter;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IAngleNormalizer;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationApplier;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationCalculator;
import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationGetter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BotRotationController {

    private final Player bot;

    private final IRotationCalculator rotationCalculator;
    private final IAngleNormalizer angleNormalizer;
    private final IRotationApplier rotationApplier;
    private final IRotationGetter rotationGetter;

    public BotRotationController(Player bot) {
        this.bot = bot;

        this.rotationCalculator = new RotationCalculator();
        this.angleNormalizer = new AngleNormalizer();
        this.rotationApplier = new RotationApplier(angleNormalizer);
        this.rotationGetter = new RotationGetter();
    }

    public void updateRotation(Player target) {
        float[] rotation = rotationCalculator.calculateRotationToTarget(bot, target);
        rotationApplier.applyRotation(bot, rotation[0], rotation[1]);
    }

    public void setInstantRotation(Player target) {
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

    public void lookAt(Vec3 targetPos) {
        float[] rotation = rotationCalculator.calculateRotationToPosition(bot, targetPos);
        rotationApplier.applyRotation(bot, rotation[0], rotation[1]);
    }

    public void resetRotation() {
        rotationApplier.resetRotation(bot);
    }
}