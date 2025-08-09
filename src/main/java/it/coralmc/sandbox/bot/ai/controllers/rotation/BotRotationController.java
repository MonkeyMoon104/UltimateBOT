package it.coralmc.sandbox.bot.ai.controllers.rotation;

import net.minecraft.world.entity.player.Player;

public class BotRotationController {

    private final Player bot;

    private static final float MAX_PITCH = 90f;
    private static final float MIN_PITCH = -90f;

    public BotRotationController(Player bot) {
        this.bot = bot;
    }

    public void updateRotation(Player target) {
        double dx = target.getX() - bot.getX();
        double dy = target.getEyeY() - bot.getEyeY();
        double dz = target.getZ() - bot.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = 0f;

        if (horizontalDistance > 0) {
            targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
            targetPitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, targetPitch));
        }

        setInstantRotation(targetYaw, targetPitch);
    }

    private void setInstantRotation(float yaw, float pitch) {
        yaw = normalizeAngle(yaw);

        pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, pitch));

        bot.setYRot(yaw);
        bot.yHeadRot = yaw;
        bot.yBodyRot = yaw;
        bot.setXRot(pitch);
    }

    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle > 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    public void setInstantRotation(Player target) {
        updateRotation(target);
    }

    public float getCurrentYaw() {
        return bot.getYRot();
    }

    public float getCurrentPitch() {
        return bot.getXRot();
    }

    public void setRotation(float yaw, float pitch) {
        setInstantRotation(yaw, pitch);
    }

    public void lookAt(double x, double y, double z) {
        double dx = x - bot.getX();
        double dy = y - bot.getEyeY();
        double dz = z - bot.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = 0f;

        if (horizontalDistance > 0) {
            pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
        }

        setInstantRotation(yaw, pitch);
    }

    public void resetRotation() {
        setInstantRotation(0f, 0f);
    }
}