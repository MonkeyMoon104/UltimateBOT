package com.monkey.mcbot.bot.ai.controllers.rotation.helper;

import com.monkey.mcbot.bot.ai.controllers.rotation.helper.inter.IRotationCalculator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RotationCalculator implements IRotationCalculator {

    private static final float MAX_PITCH = 90f;
    private static final float MIN_PITCH = -90f;

    @Override
    public float[] calculateRotationToTarget(Player bot, Player target) {
        double dx = target.getX() - bot.getX();
        double dy = target.getEyeY() - bot.getEyeY();
        double dz = target.getZ() - bot.getZ();

        return calculateRotationFromDeltas(dx, dy, dz);
    }

    @Override
    public float[] calculateRotationToPosition(Player bot, double x, double y, double z) {
        double dx = x - bot.getX();
        double dy = y - bot.getEyeY();
        double dz = z - bot.getZ();

        return calculateRotationFromDeltas(dx, dy, dz);
    }

    @Override
    public float[] calculateRotationToPosition(Player bot, Vec3 targetPos) {
        return calculateRotationToPosition(bot, targetPos.x, targetPos.y, targetPos.z);
    }

    private float[] calculateRotationFromDeltas(double dx, double dy, double dz) {
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = 0f;

        if (horizontalDistance > 0) {
            targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
            targetPitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, targetPitch));
        }

        return new float[]{targetYaw, targetPitch};
    }
}