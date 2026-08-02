package com.monkey.ultimatebot.combat.mode;

final class ModeCombatPolicy {
    private ModeCombatPolicy() {}

    static boolean canLaunchMace(double horizontalDistance, boolean onGround, boolean specialActionReady) {
        return horizontalDistance <= 4.5D && onGround && specialActionReady;
    }

    static boolean canFightInWater(boolean botInWater, boolean targetInWater) {
        return botInWater && targetInWater;
    }

    static boolean isCartOpportunity(
            double distance, double botY, double targetY, double targetHealthRatio, boolean periodicWindow) {
        return distance >= 3.0D
                && distance <= 7.0D
                && botY <= targetY + 0.55D
                && (targetHealthRatio <= 0.78D || periodicWindow);
    }

    static boolean shouldUsePotions(double healthRatio, double healingThreshold, boolean specialActionReady) {
        return healthRatio <= healingThreshold && specialActionReady;
    }

    static double projectileSpread(double accuracy) {
        return (1.0D - Math.clamp(accuracy, 0.0D, 1.0D)) * 0.24D;
    }
}
