package it.coralmc.sandbox.bot.ai.controllers.rotation.helper;

import it.coralmc.sandbox.bot.ai.controllers.rotation.helper.inter.IAngleNormalizer;

public class AngleNormalizer implements IAngleNormalizer {

    private static final float MAX_PITCH = 90f;
    private static final float MIN_PITCH = -90f;

    @Override
    public float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle > 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    @Override
    public float normalizePitch(float pitch) {
        return Math.max(MIN_PITCH, Math.min(MAX_PITCH, pitch));
    }
}