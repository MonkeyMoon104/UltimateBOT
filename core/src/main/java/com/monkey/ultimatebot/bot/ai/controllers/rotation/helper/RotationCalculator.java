package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter.IRotationCalculator;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RotationCalculator implements IRotationCalculator {

    private static final float MAX_PITCH = 90f;
    private static final float MIN_PITCH = -90f;

    @Override
    public float[] calculateRotationToTarget(ITrainingBot bot, LivingEntity target) {
        Vector botEye = bot.bukkitEyePosition();
        Location targetEye = target.getEyeLocation();
        double dx = targetEye.getX() - botEye.getX();
        double dy = targetEye.getY() - botEye.getY();
        double dz = targetEye.getZ() - botEye.getZ();

        return calculateRotationFromDeltas(dx, dy, dz);
    }

    @Override
    public float[] calculateRotationToPosition(ITrainingBot bot, double x, double y, double z) {
        Vector botEye = bot.bukkitEyePosition();
        double dx = x - botEye.getX();
        double dy = y - botEye.getY();
        double dz = z - botEye.getZ();

        return calculateRotationFromDeltas(dx, dy, dz);
    }

    @Override
    public float[] calculateRotationToPosition(ITrainingBot bot, Vector targetPos) {
        return calculateRotationToPosition(bot, targetPos.getX(), targetPos.getY(), targetPos.getZ());
    }

    private float[] calculateRotationFromDeltas(double dx, double dy, double dz) {
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = 0f;

        if (horizontalDistance > 0) {
            targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
            targetPitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, targetPitch));
        }

        return new float[] {targetYaw, targetPitch};
    }
}
