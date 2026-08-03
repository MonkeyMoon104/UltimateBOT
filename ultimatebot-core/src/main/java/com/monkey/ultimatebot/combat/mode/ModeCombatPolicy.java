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

    static boolean isIncomingPlayerAttack(
            double distance,
            double attackStrength,
            boolean blocking,
            boolean usingItem,
            double closingSpeed,
            double facingDot) {
        if (blocking || distance > 8.0D) {
            return false;
        }
        boolean aimedItemUse = usingItem && distance <= 8.0D && facingDot >= 0.35D;
        boolean immediateSwing =
                distance <= 3.55D && attackStrength >= 0.72D && closingSpeed >= 0.01D && facingDot >= 0.05D;
        boolean rushingAttack =
                distance <= 4.8D && attackStrength >= 0.45D && closingSpeed >= 0.035D && facingDot >= 0.25D;
        return aimedItemUse || immediateSwing || rushingAttack;
    }

    static boolean isSafeAxeOpening(
            double distance, double attackRange, boolean incomingAttack, boolean attackReady, int guardedTicks) {
        return distance <= attackRange && !incomingAttack && attackReady && guardedTicks >= 3;
    }

    static boolean shouldCounterShieldImpact(
            double distance, double attackRange, boolean attackReady, double aggression, double randomRoll) {
        double counterChance = 0.35D + Math.clamp(aggression, 0.0D, 1.0D) * 0.5D;
        return distance <= attackRange && attackReady && randomRoll < counterChance;
    }

    static boolean isBowFullyDrawn(int drawTicks) {
        return drawTicks >= 20;
    }

    static double projectileSpread(double accuracy) {
        return (1.0D - Math.clamp(accuracy, 0.0D, 1.0D)) * 0.24D;
    }
}
