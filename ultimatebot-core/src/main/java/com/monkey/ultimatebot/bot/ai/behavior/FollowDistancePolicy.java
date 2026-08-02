package com.monkey.ultimatebot.bot.ai.behavior;

final class FollowDistancePolicy {
    static final double STOP_DISTANCE = 2.9D;
    static final double RESUME_DISTANCE = 3.8D;

    private boolean advancing;

    boolean shouldAdvance(double horizontalDistance) {
        if (advancing && horizontalDistance <= STOP_DISTANCE) {
            advancing = false;
        } else if (!advancing && horizontalDistance >= RESUME_DISTANCE) {
            advancing = true;
        }
        return advancing;
    }

    void reset() {
        advancing = false;
    }
}
