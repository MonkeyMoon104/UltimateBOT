package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.compat.EntityCoordsAccess;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import com.monkey.ultimatebot.combat.mode.water.UnderwaterMotionPlanner;
import com.monkey.ultimatebot.common.model.CombatTuning;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class ModeMotionService {
    private final ITrainingBot bot;
    private final Player bukkitBot;
    private final BotMovementController movement;
    private final BotRotationController rotation;
    private final Supplier<CombatTuning> tuning;
    private final SplittableRandom random;

    ModeMotionService(
            ITrainingBot bot,
            Player bukkitBot,
            BotMovementController movement,
            BotRotationController rotation,
            Supplier<CombatTuning> tuning,
            SplittableRandom random) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.bukkitBot = Objects.requireNonNull(bukkitBot, "bukkitBot");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.tuning = Objects.requireNonNull(tuning, "tuning");
        this.random = Objects.requireNonNull(random, "random");
    }

    public void aimAt(LivingEntity target) {
        rotation.updateRotation(target);
    }

    public double distanceTo(LivingEntity target) {
        return botLocation().distance(entityLocation(target));
    }

    public double horizontalDistanceTo(LivingEntity target) {
        Vector delta = entityLocation(target).toVector().subtract(botLocation().toVector());
        return Math.hypot(delta.getX(), delta.getZ());
    }

    public double heightAbove(LivingEntity target) {
        return EntityCoordsAccess.getY(bukkitBot) - EntityCoordsAccess.getY(target);
    }

    public void approach(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, false);
    }

    public void retreat(LivingEntity target, double desiredDistance) {
        moveRelativeTo(target, desiredDistance, true);
    }

    public void strafe(LivingEntity target, double strength) {
        Vector direction = entityLocation(target).toVector().subtract(botLocation().toVector()).normalize();
        double side = random.nextBoolean() ? 1.0D : -1.0D;
        movement.moveToPosition(botLocation()
                .toVector()
                .add(new Vector(-direction.getZ(), 0.0D, direction.getX()).multiply(strength * side)));
    }

    public void steerVelocityTowards(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        Vector delta = entityLocation(target).toVector().subtract(botLocation().toVector());
        double horizontalLength = Math.hypot(delta.getX(), delta.getZ());
        if (horizontalLength < 0.001D) {
            bot.setBukkitVelocity(new Vector(0.0D, verticalSpeed, 0.0D));
            return;
        }
        bot.setBukkitVelocity(new Vector(
                delta.getX() / horizontalLength * horizontalSpeed,
                verticalSpeed,
                delta.getZ() / horizontalLength * horizontalSpeed));
    }

    public void swimTowards(LivingEntity target, double speed) {
        applySwimmingVelocity(UnderwaterMotionPlanner.pursue(
                bukkitBot.getEyeLocation().toVector(), target.getEyeLocation().toVector(), bukkitBot.getVelocity(), speed));
    }

    public void swimOrbit(LivingEntity target, double radialSpeed, double strafeSpeed, double strafeDirection) {
        applySwimmingVelocity(UnderwaterMotionPlanner.orbit(
                bukkitBot.getEyeLocation().toVector(),
                target.getEyeLocation().toVector(),
                bukkitBot.getVelocity(),
                radialSpeed,
                strafeSpeed,
                strafeDirection));
    }

    public void propelTowards(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        steerVelocityTowards(target, horizontalSpeed, verticalSpeed);
        movement.clearPath();
    }

    public void knockAway(LivingEntity target, double horizontalStrength, double verticalStrength) {
        Vector away = entityLocation(target).toVector().subtract(botLocation().toVector());
        double horizontalLength = Math.hypot(away.getX(), away.getZ());
        if (horizontalLength < 0.001D) {
            return;
        }
        target.setVelocity(new Vector(
                away.getX() / horizontalLength * horizontalStrength,
                verticalStrength,
                away.getZ() / horizontalLength * horizontalStrength));
    }

    public void stop() {
        movement.stopMovement();
    }

    public boolean isBotInWater() {
        return bukkitBot.isInWater();
    }

    public boolean isTargetInWater(LivingEntity target) {
        return target.isInWater();
    }

    @SuppressWarnings("deprecation")
    public void setSwimming(boolean swimming) {
        bukkitBot.setSprinting(swimming);
        bukkitBot.setSwimming(swimming);
    }

    public boolean hasVerticalClearance(LivingEntity entity, int clearanceBlocks) {
        double radius = Math.min(0.35D, entity.getBoundingBox().getWidthX() * 0.45D);
        double startY = entity.getBoundingBox().getMaxY() + 0.05D;
        double[] offsets = {-radius, radius};
        for (double xOffset : offsets) {
            for (double zOffset : offsets) {
                for (int height = 0; height < clearanceBlocks; height++) {
                    Location position = new Location(
                            entity.getWorld(), EntityCoordsAccess.getX(entity) + xOffset, startY + height, EntityCoordsAccess.getZ(entity) + zOffset);
                    if (position.getBlock().getType().isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean hasVerticalClearance(ITrainingBot entity, int clearanceBlocks) {
        return hasVerticalClearance(entity.asBukkitPlayer(), clearanceBlocks);
    }

    private void moveRelativeTo(LivingEntity target, double desiredDistance, boolean away) {
        Vector delta = entityLocation(target).toVector().subtract(botLocation().toVector());
        double horizontalDistance = Math.hypot(delta.getX(), delta.getZ());
        if (horizontalDistance < 0.001D) {
            movement.stopMovement();
            return;
        }
        double direction = away ? -1.0D : 1.0D;
        double travel = away
                ? Math.max(1.0D, desiredDistance - horizontalDistance)
                : Math.max(0.0D, horizontalDistance - desiredDistance);
        Vector destination = botLocation()
                .toVector()
                .add(new Vector(
                        delta.getX() / horizontalDistance * travel * direction,
                        0.0D,
                        delta.getZ() / horizontalDistance * travel * direction));
        movement.setMovementSpeed(tuning.get().movementSpeed());
        movement.moveToPosition(destination);
    }

    private Location botLocation() {
        return Objects.requireNonNull(bukkitBot.getLocation(), "bot location");
    }

    private static Location entityLocation(LivingEntity entity) {
        return Objects.requireNonNull(entity.getLocation(), "entity location");
    }

    public boolean isBotOnGround() {
        return bot.isOnGround();
    }

    public double botVerticalVelocity() {
        return bot.bukkitVelocity().getY();
    }

    public double botY() {
        return EntityCoordsAccess.getY(bukkitBot);
    }

    private void applySwimmingVelocity(Vector velocity) {
        movement.clearPath();
        bot.setBukkitVelocity(velocity);
    }
}
