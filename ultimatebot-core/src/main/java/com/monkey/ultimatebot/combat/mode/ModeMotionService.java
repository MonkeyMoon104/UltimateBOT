package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

final class ModeMotionService {
    private final Player bot;
    private final BotMovementController movement;
    private final BotRotationController rotation;
    private final Supplier<CombatTuning> tuning;
    private final RandomGenerator random;

    ModeMotionService(
            Player bot,
            BotMovementController movement,
            BotRotationController rotation,
            Supplier<CombatTuning> tuning,
            RandomGenerator random) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.tuning = Objects.requireNonNull(tuning, "tuning");
        this.random = Objects.requireNonNull(random, "random");
    }

    void aimAt(LivingEntity target) {
        rotation.updateRotation(target);
    }

    double distanceTo(LivingEntity target) {
        return bot.distanceTo(target);
    }

    double horizontalDistanceTo(LivingEntity target) {
        Vec3 delta = target.position().subtract(bot.position());
        return Math.hypot(delta.x, delta.z);
    }

    double heightAbove(LivingEntity target) {
        return bot.getY() - target.getY();
    }

    void approach(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, false);
    }

    void retreat(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, true);
    }

    void strafe(LivingEntity target, double strength) {
        Vec3 direction = target.position().subtract(bot.position()).normalize();
        double side = random.nextBoolean() ? 1.0D : -1.0D;
        movement.moveToPosition(bot.position().add(new Vec3(-direction.z, 0.0D, direction.x).scale(strength * side)));
    }

    void steerVelocityTowards(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        Vec3 delta = target.position().subtract(bot.position());
        double horizontalLength = Math.hypot(delta.x, delta.z);
        if (horizontalLength < 0.001D) {
            bot.setDeltaMovement(0.0D, verticalSpeed, 0.0D);
            return;
        }
        bot.setDeltaMovement(
                delta.x / horizontalLength * horizontalSpeed,
                verticalSpeed,
                delta.z / horizontalLength * horizontalSpeed);
        bot.hurtMarked = true;
    }

    void propelTowards(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        steerVelocityTowards(target, horizontalSpeed, verticalSpeed);
        movement.clearPath();
    }

    void stop() {
        movement.stopMovement();
    }

    boolean isBotInWater() {
        return bot.isInWater();
    }

    boolean isTargetInWater(LivingEntity target) {
        return target.isInWater();
    }

    void setSwimming(boolean swimming) {
        bot.setSwimming(swimming);
    }

    boolean hasVerticalClearance(LivingEntity entity, int clearanceBlocks) {
        double radius = Math.min(0.35D, entity.getBbWidth() * 0.45D);
        double startY = entity.getY() + entity.getBbHeight() + 0.05D;
        double[] offsets = {-radius, radius};
        for (double xOffset : offsets) {
            for (double zOffset : offsets) {
                BlockPos start = BlockPos.containing(entity.getX() + xOffset, startY, entity.getZ() + zOffset);
                for (int height = 0; height < clearanceBlocks; height++) {
                    BlockPos position = start.above(height);
                    if (!entity.level()
                            .getBlockState(position)
                            .getCollisionShape(entity.level(), position)
                            .isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void moveRelativeTo(LivingEntity target, double desiredDistance, boolean away) {
        Vec3 delta = target.position().subtract(bot.position());
        double horizontalDistance = Math.hypot(delta.x, delta.z);
        if (horizontalDistance < 0.001D) {
            movement.stopMovement();
            return;
        }
        double direction = away ? -1.0D : 1.0D;
        double travel = away
                ? Math.max(1.0D, desiredDistance - horizontalDistance)
                : Math.max(0.0D, horizontalDistance - desiredDistance);
        Vec3 destination = bot.position()
                .add(
                        delta.x / horizontalDistance * travel * direction,
                        0.0D,
                        delta.z / horizontalDistance * travel * direction);
        movement.setMovementSpeed(tuning.get().movementSpeed());
        movement.moveToPosition(destination);
    }
}
