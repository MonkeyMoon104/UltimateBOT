package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper;

import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.IPositionCalculator;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PositionCalculator implements IPositionCalculator {

    private static final int PREDICT_TICKS = 8;

    private final ISafetyValidator safetyValidator;

    public PositionCalculator(ISafetyValidator safetyValidator) {
        this.safetyValidator = safetyValidator;
    }

    @Override
    public Vec3 calculateEmergencyEscape(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 awayDirection = botPos.subtract(targetPos).normalize();

        double distance = 6.0 + Math.random() * 3.0;
        double yOffset = Math.random() * 2.0;

        Vec3 escapePos = botPos.add(awayDirection.scale(distance)).add(0, yOffset, 0);
        BlockPos escapeBlock = BlockPos.containing(escapePos);

        if (safetyValidator.isSafeLandingSpot(escapeBlock)) {
            return escapePos;
        }

        return safetyValidator.findSafeLandingSpot(BlockPos.containing(botPos.add(awayDirection.scale(7))));
    }

    @Override
    public Vec3 calculateMeleeDisengage(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 awayDirection = botPos.subtract(targetPos).normalize();

        double distance = 6.0 + Math.random() * 3.0;
        double yBoost = 1.5 + Math.random() * 2.0;

        Vec3 disengagePos = botPos.add(awayDirection.scale(distance)).add(0, yBoost, 0);
        BlockPos disengageBlock = BlockPos.containing(disengagePos);

        if (safetyValidator.isSafeLandingSpot(disengageBlock)) {
            return disengagePos;
        }

        return safetyValidator.findSafeLandingSpot(disengageBlock);
    }

    @Override
    public Vec3 calculateLowGroundPosition(Player bot, Player target) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        int targetY = target.blockPosition().getY();
        int desiredY = targetY - 2 - (int)(Math.random() * 2);

        double radius = 4.0 + Math.random() * 2.0;
        double angle = Math.random() * 2 * Math.PI;

        Vec3 lowGroundPos = targetPos.add(
                Math.cos(angle) * radius,
                desiredY - targetPos.y,
                Math.sin(angle) * radius
        );

        BlockPos checkPos = BlockPos.containing(lowGroundPos);
        if (safetyValidator.isSafeLandingSpot(checkPos)) {
            return lowGroundPos;
        }

        return safetyValidator.findSafeLandingSpot(checkPos);
    }

    @Override
    public Vec3 calculateAnchorPosition(Player bot, Player target) {
        Vec3 targetPos = target.position();

        double distance = 5.0 + Math.random() * 3.0;
        double angle = Math.random() * 2 * Math.PI;

        Vec3 anchorPos = targetPos.add(
                Math.cos(angle) * distance,
                -1.0,
                Math.sin(angle) * distance
        );

        BlockPos checkPos = BlockPos.containing(anchorPos);
        if (safetyValidator.isSafeLandingSpot(checkPos)) {
            return anchorPos;
        }

        return safetyValidator.findSafeLandingSpot(checkPos);
    }

    @Override
    public Vec3 calculateAggressiveApproach(Player bot, Player target, Vec3 predictedTargetMovement) {
        Vec3 targetPos = target.position();
        Vec3 predictedPos = targetPos.add(predictedTargetMovement.scale(PREDICT_TICKS / 20.0));

        Vec3 botPos = bot.position();
        Vec3 toTarget = predictedPos.subtract(botPos).normalize();

        double approachDistance = 2.5 + Math.random() * 1.5;

        Vec3 approachPos = predictedPos.subtract(toTarget.scale(approachDistance));
        approachPos = approachPos.add(0, Math.random(), 0);

        BlockPos checkPos = BlockPos.containing(approachPos);
        if (safetyValidator.isSafeLandingSpot(checkPos)) {
            return approachPos;
        }

        return safetyValidator.findSafeLandingSpot(checkPos);
    }

    @Override
    public Vec3 calculateStandardEscape(Player bot, Player target) {
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        Vec3 baseDirection = botPos.subtract(targetPos).normalize();

        double angle = (Math.random() - 0.5) * Math.PI / 2;
        Vec3 escapeDirection = rotateVector(baseDirection, angle);

        double escapeDistance = 8.0 + Math.random() * 6.0;
        double yOffset = (Math.random() - 0.3) * 3.0;

        Vec3 escapePos = botPos.add(escapeDirection.scale(escapeDistance)).add(0, yOffset, 0);
        BlockPos checkPos = BlockPos.containing(escapePos);

        if (safetyValidator.isSafeLandingSpot(checkPos)) {
            return escapePos;
        }

        return safetyValidator.findSafeLandingSpot(checkPos);
    }

    private Vec3 rotateVector(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        );
    }
}